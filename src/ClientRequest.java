import java.net.*;
import java.io.*;

import DnsTunneling.*;

public class ClientRequest implements Runnable {
    private Socket clientSocket = null;
    private String[] args = null;

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

            //ici le code à faire

            out.write(new Response(query).getResponse());
            out.flush();
            this.clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error in the process of the client request", e);
        }
    }
}
