package DnsTunneling;
import java.nio.ByteBuffer;
import java.util.Base64;



public class Response {
    private final int HEADER_LENGTH = 12;
    private final int MAX_HTTP_ENCODED_LENGTH = 60000;

    private boolean isTruncated = false;
    private int RCODE = 0 ;

    private byte[] header;
    private byte[] question;
    private byte[] answer;
    private byte[] response;

    private Query query;

    /**
     * Set the error code of the dns answer
     * @param rcode the error code
     */
    private void setRCODE(int rcode) {
        switch (rcode) {
            case 1:
                this.RCODE = 1 ;
                this.header[3] |= 1 << 0;
                break;

            case 2:
                this.RCODE = 2 ;
                this.header[3] |= 1 << 1;
                break;

            case 3:
                this.RCODE = 3 ;
                this.header[3] |= 1 << 0;
                this.header[3] |= 1 << 1;
                break;

            case 4:
                this.RCODE = 4 ;
                this.header[3] |= 1 << 2;
                break;

            case 5:
                this.RCODE = 5 ;
                this.header[3] |= 1 << 2;
                this.header[3] |= 1 << 0;

            default:
                this.RCODE = 0 ;
                break;
        }
    }

    private static byte[] intAsbyteArr(int nbr) {
        byte[] bytes = new byte[2];

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(nbr);

        bytes[0] = buffer.array()[2]; bytes[1] = buffer.array()[3];

        return bytes;
    }

    /**
     *
     * @param query
     */
    public Response(Query query) {
        //Initialize the fields of a Response
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = answer();

        this.header[2] |= 1 << 7; //QR bit set to 1

        if (this.answer == null){ //if there is no answer
            //Get the number of bytes of the response
            int lengthResponse = this.HEADER_LENGTH + this.question.length;

            //Set the error code to Name Error
            setRCODE(3);

            //Allocate buffer for the dns response without answer
            ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 2);
            buffer.put(intAsbyteArr(lengthResponse));
            buffer.put(this.header);
            buffer.put(this.question);

            this.response = buffer.array();
        } else {

            //Get the number of bytes of the response
            int lengthResponse = this.HEADER_LENGTH + this.question.length + this.answer.length;

            this.header[7] |= 1 << 0; //ANCOUNT set to one because of the assignement

            //If the answer is truncated we set the error to NameError
            if(this.isTruncated){
                this.header[2] |= 1 << 6; //TR bit
                setRCODE(3);
            }

            //Allocate buffer for the dns response
            ByteBuffer response = ByteBuffer.allocate(lengthResponse + 2);
            response.put(intAsbyteArr(lengthResponse));
            response.put(this.header);
            response.put(this.question);
            response.put(this.answer);

            this.response = response.array();
        }
    }

    public Response(Query query, int rCode) {
        //Initialize the fields of a Response
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = null;

        //Get the number of bytes of the response
        int lengthResponse = this.HEADER_LENGTH + this.question.length;

        this.header[2] |= 1 << 7; //QR bit set to 1

        //Set the error with the error code given
        setRCODE(rCode);

        //Allocate buffer for the dns response without answer
        ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 2);
        buffer.put(intAsbyteArr(lengthResponse));
        buffer.put(this.header);
        buffer.put(this.question);

        this.response = buffer.array();
    }

    private static byte[] txtRData(byte[] request){
        int nbrLengthBytes = (request.length / 255) + (request.length % 255 == 0 ? 0 : 1), index = 0, indexRequest = 0;
        byte[] txtRData = new byte[request.length + nbrLengthBytes];

        while (nbrLengthBytes != 0) {
            int length = (nbrLengthBytes > 1 ? 255 : request.length % 255);
            txtRData[index++] = (byte) length;

            int i = indexRequest;
            while (indexRequest < i + length)
                txtRData[index++] = request[indexRequest++];


            nbrLengthBytes--;
        }

        return txtRData;
    }

    private byte[] answer() {
        try {
            //Create the name section of the Resource record (-4 because of the other section of the question that are not the Name)
            byte[] questionName = new byte[this.question.length - 4];
            for (int i = 0; i < questionName.length; i++)
                questionName[i] = this.question[i];

            //Initialize the type, class, ttl and rdlength sections of the Resource record
            byte[] rType = new byte[2], rClass = new byte[2], ttl = new byte[4], rDLength = new byte[2];

            //Set the type to TXT and the class to IN
            rType[1] |= 1 << 4;
            rClass[1] |= 1 << 0;

            //Set the Time To Live to 3600 (default)
            ttl[3] |= 1 << 4; //First 8 bits
            ttl[2] |= 1 << 1; ttl[2] |= 1 << 2; ttl[2] |= 1 << 3; //Last 8 bits


            //Request HHTP if not valid we return null; because no answer.
            String request = RequestHTTP.Request(this.query);
            if (request == null)
                return null;

            //We get the request encoded and we look at the size of the encoded request if it is > 60000 we keep the 60000 first
            byte[] temp = Base64.getEncoder().encode(request.getBytes());

            //Initialize the encoded byte url and see if it is truncated
            byte[] encodedUrl;
            if (temp.length > MAX_HTTP_ENCODED_LENGTH) {
                encodedUrl = new byte[MAX_HTTP_ENCODED_LENGTH];
                this.isTruncated = true;
            }else
                encodedUrl = new byte[temp.length];


            //Filling of the encodedUrl
            for (int i = 0; i < encodedUrl.length; i++)
                encodedUrl[i] = temp[i];

            //Change the encoded url into TXT RDATA format
            byte[] rData = txtRData(encodedUrl);

            //Set the RDLENGTH to the RDATA length
            rDLength = intAsbyteArr(rData.length);

            ByteBuffer response = ByteBuffer.allocate(questionName.length + 10 + rData.length);
            response.put(questionName);
            response.put(rType);
            response.put(rClass);
            response.put(ttl);
            response.put(rDLength);
            response.put(rData);

            return response.array();

        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getResponse() {
        return this.response;
    }

    public void printQuestion(String hostAddress) {
        Query query = this.query ;
        String QTYPE = new String();
        switch ((int)query.getQTYPE()) {
            case 1:
                QTYPE = "A";
                break;
            case 16:
                QTYPE = "TXT";
                break;
            default:
                throw new RuntimeException("ERROR: QTYPE not supported\n");
        }

        System.out.println(
            "Question (CL=" +
            hostAddress +
            ", NAME=" +
            query.getQuestionUrl() + "." + query.getOwnedDomainName() +
            ", TYPE=" +
            QTYPE +
            ") => " +
            this.RCODE
        );
    }

}
