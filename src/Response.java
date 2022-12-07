import java.nio.ByteBuffer;

public class Response {
    private final int HEADER_LENGTH = 12;

    byte[] header;
    byte[] question;
    byte[] answer;
    byte[] response;

    /**
     * Set the error code of the dns answer
     * @param rcode the error code
     */
    private void setRCODE(int rcode) {
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
        this.header = query.getHeader();
        this.question = query.getQuestion();

        this.header[2] |= 1 << 7; //QR bit set to 1
        this.header[7] |= 1 << 0; //ANCOUNT set to one because of the assignement
            
        //écrire answer
        
        
        for (int i = 0; i < this.response.length; i++) 
            System.out.println(String.format("%8s", Integer.toBinaryString(this.response[i] & 0xFF)).replace(' ', '0'));
        
    }

    public Response(Query query, int rCode) {
        this.header = query.getHeader();
        this.question = query.getQuestion();

        this.header[2] |= 1 << 7; //QR bit set to 1

        setRCODE(rCode);

        this.answer = null;

        ByteBuffer buffer = ByteBuffer.allocate(this.HEADER_LENGTH + this.question.length);
        buffer.put(this.header);
        buffer.put(this.question);

        this.response = buffer.array();
        
        for (int i = 0; i < this.response.length; i++) 
            System.out.println(String.format("%8s", Integer.toBinaryString(this.response[i] & 0xFF)).replace(' ', '0'));
        
    }

    
}
