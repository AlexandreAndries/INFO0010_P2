import java.net.*;
import java.io.*;

import DnsTunneling.*;

public class ClientRequest implements Runnable {
    private final int RCODE_FORMAT = 1 ;
    private final int RCODE_SERVERFAILURE = 2 ;
    private final int RCODE_NAME = 3 ;
    private final int RCODE_NIMPLEMENTED = 4 ;
    private final int RCODE_REFUSED = 5 ;
    private final short TXT = 16 ;

    private Socket clientSocket = null;
    private String[] args ;

    public ClientRequest(Socket socket, String[] args) {
        this.clientSocket = socket;
        this.args = args;
    }

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

            // =================================================================
            if (query.checkFormatErrors()) {                                       // 4 works !
                Response rError = new Response(query, RCODE_FORMAT);
                out.write(rError.getResponse());
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();

            } else if (!isValidURL(query.getQuestionUrl())) {                      // 6 works !
                Response rError = new Response(query, RCODE_NAME);
                out.write(rError.getResponse());
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();

            } else if (query.getQDCOUNT() != (short)1
                        || query.getQTYPE() != TXT
                        || !(query.getOwnedDomainName().equals(this.args[0]))) {   // 5 almost finished

                Response rError = new Response(query, RCODE_REFUSED);
                out.write(rError.getResponse());
                rError.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();
            // =================================================================
            } else {
                Response r = new Response(query) ;
                out.write(r.getResponse());
                r.printQuestion(this.clientSocket.getInetAddress().getHostAddress());
                out.flush();
                this.clientSocket.close();
            }


        } catch (IOException e) {
            throw new RuntimeException("Error in the process of the client request", e);
        }
    }

    public static boolean isValidURL(String url)
    {
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
