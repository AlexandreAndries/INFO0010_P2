import java.nio.ByteBuffer;
import java.util.Base64;



public class Response {
    private final int HEADER_LENGTH = 12;
    private final int MAX_HTTP_ENCODED_LENGTH = 60000;

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
        if (this.answer == null) //if thera is no answer
            this.header[7] |= 1 << 0; //ANCOUNT set to one because of the assignement

        answer();

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
            ttl[3] |= 1 << 4;
            ttl[2] |= 1 << 1; ttl[2] |= 1 << 2; ttl[2] |= 1 << 3;


            //Request HHTP if not valid we return null; because no answer.
            String request = RequestHTTP.Request(this.query);
            if (request == null)
                return null;

            //We get the request encoded and we look at the size of the encoded request if it is > 60000 we keep the 60000 first
            byte[] temp = Base64.getEncoder().encode(request.getBytes());
            byte[] encodedUrl = new byte[temp.length > MAX_HTTP_ENCODED_LENGTH ? 10 : temp.length];

            for (int i = 0; i < encodedUrl.length; i++)
                encodedUrl[i] = temp[i];





            return null;
        } catch (Exception e) {
            System.out.println("caca 1");
            return null;
        }
    }

    public byte[] getResponse() {
        return this.response;
    }


}
