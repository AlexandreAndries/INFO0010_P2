package DnsTunneling;

public class Query {
    private final int HEADER_LENGTH = 12;

    byte[] header;
    byte[] question;
    byte[] query;

    /**
     * This constructor instanciate a Query
     * @param query the bytes of the query
     */
    public Query(byte[] query){
        this.query = query;
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
        byte[] bytesUrlDecoded = Base32.decode(urlEncoded.replace("=", ""));

        //Translate each bytes of the url into character
        for (int i = 0; i < bytesUrlDecoded.length; i++)
            urlDecoded += (char) bytesUrlDecoded[i];

        return urlDecoded;
    }

    /**
     * Get the domain name of the question of the dns query
     * @return the domain name
     */
    public String getOwnedDomainName() {
        String ownedDomainName = new String();
        int urlLength = (int) this.question[0]; //we get the length of the encoded url
        int index = urlLength + 1; //we set the index on the length byte of the domain name
        int lengthLabel = (int) this.question[index], indexLabel = 0; //initialize the length of the current label of the domain and the index of each labels

        //we go through the question until we have the 0 length byte
        while((int) this.question[index] != 0) {

            //we put the current byte as a char in the string
            ownedDomainName += (char) this.question[index];

            //if the indexLabel is equal to the current lengthLabel
            //we put a "." in the string and
            //set lengthLabel to the next label length and
            //reset the indexLabel to 0
            if (indexLabel == lengthLabel && (int) this.question[index+1] != 0){
                ownedDomainName += ".";
                indexLabel = 0;
                lengthLabel = (int) this.question[index + 1];

            } else
                indexLabel++;

            index++;
        }

        return ownedDomainName;
    }

    public byte[] getHeader() {
        return this.header;
    }

    public byte[] getQuestion() {
        return this.question;
    }
}
