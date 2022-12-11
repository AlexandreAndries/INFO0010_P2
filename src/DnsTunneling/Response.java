package DnsTunneling;

/**
 * \file Response.java
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
  * Response class is used to represent the answer to send to the client
  * A response object is built according to the RFC1035 standards.
  *
  */
import java.nio.ByteBuffer;
import java.util.Base64;

// class Response
public class Response {
    /*------------------------------------------------------------------------*/
    /*- Variables ------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Class Constants
    private final int HEADER_LENGTH = 12;               // Length in bytes of header section
    private final int MAX_HTTP_ENCODED_LENGTH = 60000;  // Maximum HTTP encoded length
    // Class Variables
    private boolean isTruncated = false;                // Truncation state variable
    private int RCODE ;                                 // Error Code value variable

    private byte[] header;                              // Byte array corresp. to header section
    private byte[] question;                            // Byte array corresp. to question section
    private byte[] answer;                              // Byte array corresp. to answer section
    private byte[] response;                            // Byte array corresp. to entire response

    private Query query;                                // query object of according query
    /*------------------------------------------------------------------------*/
    /*- Constructor ----------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Constructor for no-error response
    public Response(Query query) {
        //Initialize the fields of a Response
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = answer();

        this.header[2] |= 1 << 7; // QR bit set to 1

        if (this.answer == null){ // if there is no answer,
            // Get the number of bytes of the response
            int lengthResponse = this.HEADER_LENGTH + this.question.length;

            // Set the error code to Name Error
            setRCODE(3);

            // Allocate buffer for the dns response without answer
            ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 2);
            buffer.put(intAsbyteArr(lengthResponse));
            buffer.put(this.header);
            buffer.put(this.question);

            this.response = buffer.array();
        } else {

            // Get the number of bytes of the response
            int lengthResponse = this.HEADER_LENGTH + this.question.length + this.answer.length;

            this.header[7] |= 1 << 0; //ANCOUNT set to one because of the assignement

            // If the answer is truncated we set the error to NameError
            if(this.isTruncated){
                this.header[2] |= 1 << 6; //TR bit
                setRCODE(3);
            }

            // Allocate buffer for the dns response
            ByteBuffer response = ByteBuffer.allocate(lengthResponse + 2);
            response.put(intAsbyteArr(lengthResponse));
            response.put(this.header);
            response.put(this.question);
            response.put(this.answer);

            this.response = response.array();
        }
    }// Response object constuctor
    /*------------------------------------------------------------------------*/
    // Constructor for no-error response
    public Response(Query query, int rCode) {
        // Initialize the fields of a Response
        this.query = query;
        this.header = query.getHeader();
        this.question = query.getQuestion();
        this.answer = null;

        // Get the number of bytes of the response
        int lengthResponse = this.HEADER_LENGTH + this.question.length;

        this.header[2] |= 1 << 7; //QR bit set to 1

        // Set the error with the error code given
        setRCODE(rCode);

        // Allocate buffer for the dns response without answer
        ByteBuffer buffer = ByteBuffer.allocate(lengthResponse + 2);
        buffer.put(intAsbyteArr(lengthResponse));
        buffer.put(this.header);
        buffer.put(this.question);

        this.response = buffer.array();
    }// Response object constuctor
    /*------------------------------------------------------------------------*/
    /*- Public Methods -------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn void printQuestion(String hostAddress)
     * \brief Prints client and error/success related informations on stdout
     *        whenever a client connects to the server.
     *
     * \param hostAddress, string containing the IP of the client.
     *
     */
    public void printQuestion(String hostAddress) {

        // Check question type of query
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

        // Print on stdout
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
    /*------------------------------------------------------------------------*/
    /*- Private Static Methods -----------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn void setRCODE(int rcode)
     * \brief Set the error code of the dns answer
     *
     * \param rcode, value of the error code RCODE of the response.
     *
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
                this.header[3] |= 1 << 0;
                this.header[3] |= 1 << 2;
                break;

            default:
                this.RCODE = 0 ;
                break;
        }
    }
    /*------------------------------------------------------------------------*/
    /**
     * \fn byte[] intAsbyteArr(int nbr)
     * \brief Converts int val to corresponding byte array
     *
     * \param nbr, int val to convert to corresponding byte array
     *
     * \return  corresponding bytes
     */
    private byte[] intAsbyteArr(int nbr) {
        byte[] bytes = new byte[2];

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(nbr);

        bytes[0] = buffer.array()[2]; bytes[1] = buffer.array()[3];

        return bytes;
    }
    /*------------------------------------------------------------------------*/
    /**
     * \fn byte[] txtRData(byte[] request)
     * \brief Converts an HTTP request (byte array) into the corresponding
     *        char String.
     *
     * \param request, byte array containing the HTTP request.
     *
     * \return the translated HTTP request in char String as a byte array
     */
    private byte[] txtRData(byte[] request){
        // Determine the length (number of bytes) of each word
        int nbrLengthBytes = (request.length / 255) + (request.length % 255 == 0 ? 0 : 1), index = 0, indexRequest = 0;

        // Initialize the byte array accordingly
        byte[] txtRData = new byte[request.length + nbrLengthBytes];

        // Loop over each byte to fill the txtRData byte array with corresponding
        // bytes
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
    /*------------------------------------------------------------------------*/
    /**
     * \fn byte[] answer()
     * \brief Builds the ressource record section of the DNS response.
     *
     * \return byte array containing the ressrouce record response section.
     */
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
    /*------------------------------------------------------------------------*/
    /*- Getters --------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Returns Response (byte array)
    public byte[] getResponse() {
        return this.response;
    }
}
/*----------------------------------------------------------------------------*/
/*- END OF CLASS -------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
