import java.nio.ByteBuffer;
import java.util.Base64;



public class Response {
    private final int HEADER_LENGTH = 12;
    private final int MAX_HTTP_ENCODED_LENGTH = 60000;

    private boolean isTruncated;
 
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
        // asert rcode != 0
        switch (rcode) {
            case 1:
                this.header[3] |= 1 << 0;
                break;

            case 2:
                this.header[3] |= 1 << 1;
                break;

            case 3:
                this.header[3] |= 1 << 0;
                this.header[3] |= 1 << 1;
                break;

            case 4:
                this.header[3] |= 1 << 2;
                break;

            case 5:
                this.header[3] |= 1 << 2;
                this.header[3] |= 1 << 0;

            default:
                break;
        }
    }

    public Response(Query query) {
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = answer();
        

        this.header[2] |= 1 << 7; //QR bit set to 1
        if (this.answer == null){ //if thera is no answer
            setRCODE(3);

            ByteBuffer buffer = ByteBuffer.allocate(this.HEADER_LENGTH + this.question.length);
            buffer.put(this.header);
            buffer.put(this.question);

            this.response = buffer.array();

            System.out.println("pas de reponse");
            return;
        }

        this.header[7] |= 1 << 0; //ANCOUNT set to one because of the assignement
        if(this.isTruncated){
            this.header[1] |= 1 << 6; //TR bit
            setRCODE(3);
        }

        ByteBuffer buffer = ByteBuffer.allocate(this.HEADER_LENGTH + this.question.length + answer.length);
        buffer.put(this.header);
        buffer.put(this.question);
        buffer.put(this.answer);

        this.response = buffer.array();

        return;
    }

    public Response(Query query, int rCode) {
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = null;

        this.header[2] |= 1 << 7; //QR bit set to 1

        setRCODE(rCode);

        ByteBuffer buffer = ByteBuffer.allocate(this.HEADER_LENGTH + this.question.length);
        buffer.put(this.header);
        buffer.put(this.question);

        this.response = buffer.array();

        //for (int i = 0; i < this.response.length; i++)
        //    System.out.println(String.format("%8s", Integer.toBinaryString(this.response[i] & 0xFF)).replace(' ', '0'));
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
            byte[] rType = new byte[2], rClass = new byte[2], ttl = new byte[4];

            //Set the type to TXT and the class to IN
            rType[1] |= 1 << 4;
            rClass[1] |= 1 << 0;

            //Set the Time To Live to 3600 (default)
            ttl[3] |= 1 << 4;
            ttl[2] |= 1 << 1; ttl[2] |= 1 << 2; ttl[2] |= 1 << 3;


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
            }else{
                encodedUrl = new byte[temp.length];
            }

            //Filling of the encodedUrl
            for (int i = 0; i < encodedUrl.length; i++)
                encodedUrl[i] = temp[i];


            //Change the encoded url into TXT RDATA format
            byte[] rData = txtRData(encodedUrl);
            
            ByteBuffer lengthRData = ByteBuffer.allocate(2);

            
            
            
            

            return new byte[3];

        } catch (Exception e) {
            return null;
        }
    }

    public byte[] getResponse() {
        return this.response;
    }


}
