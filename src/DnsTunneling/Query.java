package DnsTunneling;

/**
 * \file Query.java
 *
 *
 * \brief INFO0010 Projet 2
 * \author Andries Alexandre s196948
 * \author Rotheudt Thomas s191895
 * \version 0.1
 * \date 11/12/2022
 *
 */

 /** Class description :
  *
  * Query class is used to represent the query to send over the network.
  * A Query object is built according to the RFC1035 standards.
  *
  */
import java.nio.* ;

// class Query
public class Query {
    /*------------------------------------------------------------------------*/
    /*- Variables ------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Class Constants
    private final int HEADER_LENGTH = 12; // Length in bytes of header section
    // Class Variables
    byte[] header;                        // Byte array corresp. to header section
    byte[] question;                      // Byte array corresp. to question section
    byte[] query;                         // Byte array corresp. to entire query
    /*------------------------------------------------------------------------*/
    /*- Constructor ----------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
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
    } // Query object constuctor
    /*------------------------------------------------------------------------*/
    /*- Public Methods -------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn tring getQuestionUrl()
     * \brief Get the decoded url in the question name of the dns query
     *
     * \return  the decoded url
     *
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
    /*------------------------------------------------------------------------*/
    /**
     * \fn String getOwnedDomainName()
     * \brief Get the domain name of the question of the dns query
     *
     * \return   the domain name
     *
     */
    public String getOwnedDomainName() {
        String ownedDomainName = new String();
        int urlLength = (int) this.question[0]; // get the length of the encoded url
        int index = urlLength + 1; // set the index on the length byte of the domain name
        int lengthLabel = (int) this.question[index], indexLabel = 0; // initialize the length of the current label of the domain and the index of each labels

        // go through the question until we have the 0 length byte
        while((int) this.question[index] != 0) {

            // put the current byte as a char in the string
            ownedDomainName += (char) this.question[index];

            // if the indexLabel is equal to the current lengthLabel,
            // put a "." in the string and
            // set lengthLabel to the next label length and
            // reset the indexLabel to 0
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
    /*------------------------------------------------------------------------*/
    /**
     * \fn boolean checkFormatErrors()
     * \brief Checks the query for format errors
     *        (size of question, error codes, etc.)
     *
     * \return  - true if there is an error
     *          - false if not
     *
     */
    public boolean checkFormatErrors() {
        byte QEND = (byte) query[query.length-5];

        if (QEND != 0
                  || question.length <= 5
                  || (((header[2] & 0x80) >> 7) != 0 )
                  || (((header[3] & 0x70) >> 4) != 0 )) {
            return true ;
        } else {
            return false ;
        }
    }
    /*------------------------------------------------------------------------*/
    /*- Private Static Methods -----------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn short toShort(byte[] array)
     * \brief Convert byte array of size 2 to its short value
     *
     * \return  short value of byte array
     *
     */
    private static short toShort(byte[] array) {
        // a Short is written on 2 bytes
        ByteBuffer tmpBB = ByteBuffer.wrap(array);
        return tmpBB.getShort() ;
    }
    /*------------------------------------------------------------------------*/
    /*- Getters --------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Returns value of QDCOUNT
    public short getQDCOUNT() {
        byte[] QDCOUNT = {(byte) this.header[4], (byte) this.header[5]};

        return toShort(QDCOUNT);
    }
    /*------------------------------------------------------------------------*/
    // Returns value of QTYPE
    public short getQTYPE() {
        byte[] query = this.query ;
        int queryLength = query.length ;
        byte[] QTYPE = {(byte) query[queryLength-2], (byte) query[query.length-1]};

        return toShort(QTYPE);
    }
    /*------------------------------------------------------------------------*/
    // Returns Header (byte array)
    public byte[] getHeader() {
        return this.header;
    }
    /*------------------------------------------------------------------------*/
    // Returns Question (byte array)
    public byte[] getQuestion() {
        return this.question;
    }
}
/*----------------------------------------------------------------------------*/
/*- END OF CLASS -------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
