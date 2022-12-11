/**
 * \file ClientRequest.java
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
  * ClientRequest represents a client connection. Each thread of the main
  * program creates a new ClientRequest to receive a query, parse it,
  * and respond appropriately.
  *
  */
import java.net.*;
import java.io.*;

import DnsTunneling.*;

// class ClientRequest
public class ClientRequest implements Runnable {
    /*------------------------------------------------------------------------*/
    /*- Variables ------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    // Class Constants
    private final int RCODE_FORMAT = 1 ;            // FORMAT ERROR Code
    private final int RCODE_SERVERFAILURE = 2 ;     // SERVER FAILURE ERROR Code
    private final int RCODE_NAME = 3 ;              // NAME ERROR Code
    private final int RCODE_NIMPLEMENTED = 4 ;      // NOT IMPLEMENTED ERROR Code
    private final int RCODE_REFUSED = 5 ;           // REFUSED ERROR Code
    private final short TXT = 16 ;                  // TXT QTYPE Value
    // Class Variables
    private Socket clientSocket = null;             // Socket for the client
    private String[] args ;                         // Program inputs
    /*------------------------------------------------------------------------*/
    /*- Constructor ----------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public ClientRequest(Socket socket, String[] args) {
        this.clientSocket = socket;
        this.args = args;
    } // ClientRequest object constuctor
    /*------------------------------------------------------------------------*/
    /*- Public Methods -------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn void run()
     * \brief Run() method for the client thread. Manages the whole process
     *        of accepting a query, parsing it, respond accordingly to
     *        possible errors, and if no errors are encountered : forward the
     *        HTTP request and send back the response to the client.
     *
     */
    public void run() {
        try {
            InputStream in = this.clientSocket.getInputStream();
            OutputStream out = this.clientSocket.getOutputStream();

            //Reading the length of the query
            byte[] lengthBuffer = new byte[2];
            in.read(lengthBuffer);
            int length = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

            //Reading the query until every bytes are read
            byte[] queryBuffer = new byte[length];
            in.read(queryBuffer);

            //Initialize the Query variable
            Query query = new Query(queryBuffer);

            // Error Management :
            //
            // If there is a format error, reply with a valid DNS response
            // without answer and with response code (RCODE) set
            // to "Format Error" (point 2.4 of the assignemnt)
            if (query.checkFormatErrors()) {
                // Create a response with respective error code.
                Response rError = new Response(query, RCODE_FORMAT);

                // Write response to socket
                out.write(rError.getResponse());

                // Print question to stdout on server side
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());

                // Send response through socket
                out.flush();

                // Close socket
                this.clientSocket.close();

            // If the URL is not valid, reply with a valid DNS response
            // without answer and with response code (RCODE) set
            // to "Name Error" (point 2.6 of the assignemnt)
            } else if (!isValidURL(query.getQuestionUrl())) {
                // See above for process comments
                Response rError = new Response(query, RCODE_NAME);
                out.write(rError.getResponse());
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();

            // Check that the DNS query contains only one question of type TXT
            // with a name following this pattern:
            // <tunneled data encoded in base32>.<owned domain name>.
            // If this requirement is not respected, reply with a valid DNS
            // response without answer and with response code (RCODE) set
            // to "Refused" (point 2.5 of the assignemnt)
            } else if (query.getQDCOUNT() != (short)1
                        || query.getQTYPE() != TXT
                        || !(query.getOwnedDomainName().equals(this.args[0]))) {
                // See above for process comments
                Response rError = new Response(query, RCODE_REFUSED);
                out.write(rError.getResponse());
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();

            // No errors :
            //
            // Send valid DNS reply with with an answer and the HTTP response content
            } else {
                // Create a response with 0 error code.
                Response r = new Response(query) ;

                // See above for process comments
                out.write(r.getResponse());
                r.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();
            }

        } catch (IOException e) {
            throw new RuntimeException("Error in the process of the client request", e);
        }
    }
    /*------------------------------------------------------------------------*/
    /**
     * \fn boolean isValidURL(String url)
     * \brief Asserts that the entered string is a valid URL and has no format
     *        error.
     *
     * \param url, string containing the prurl to evaluate.
     *
     * \return  - true if the url is valid.
     *          - false if the url is not valid.
     *
     */
    public static boolean isValidURL(String url) {
        // Try creating a valid URL. If it works, returns true
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception while creating URL object,
        // exception returns false
        catch (Exception e) {
            return false;
        }
    }
}
/*----------------------------------------------------------------------------*/
/*- END OF CLASS -------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
