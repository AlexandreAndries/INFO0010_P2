public class Query {
    private final int HEADER_LENGTH = 12;

    byte[] header;
    byte[] question;

    /**
     * This constructor instanciate a Query
     * @param query the bytes of the query
     */
    Query(byte[] query){
        this.header = new byte[HEADER_LENGTH];
        this.question = new byte[query.length - HEADER_LENGTH];

        //Header filling
        for (int i = 0; i < HEADER_LENGTH; i++)
            this.header[i] = query[i];
    
        //Question filling
        for (int i = HEADER_LENGTH, j = 0; i < query.length; i++, j++)
            this.question[j] = query[i];
    }

    /**
     * Get the url decoded in the question name of the dns query
     * @return the url decoded
     */
    public String getQuestionUrl(){
        String urlEncoded = new String();
        String urlDecoded = new String();

        //Get the length of the url encoded
        int lengthURL = (int) this.question[0];

        //Translate each bytes of the url into character
        for (int i = 1; i <= lengthURL; i++)
            urlEncoded += (char) this.question[i];

        //Get the bytes of the url decoded
        byte[] bytesUrlDecoded = Base32.decode(urlEncoded);

        //Translate each bytes of the url into character
        for (int i = 0; i < bytesUrlDecoded.length; i++) 
            urlDecoded += (char) bytesUrlDecoded[i];

        return urlDecoded;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getQuestion() {
        return question;
    }

    @Override
    public String toString() {
        String s = "Header:\n";

        for (int i = 0; i < this.header.length; i++) {
            s += String.format("%8s", Integer.toBinaryString(this.header[i] & 0xFF)).replace(' ', '0') + "\n";
        }

        s += "\nQuestion:\n";

        for (int i = 0; i < this.question.length; i++) {
            s += String.format("%8s", Integer.toBinaryString(this.question[i] & 0xFF)).replace(' ', '0') + "\n";
        }
        return s;
    }
}
