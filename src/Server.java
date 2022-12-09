import java.io.*;
import java.net.*;

public class Server{


    static private boolean checkUrl(String url){
        try{
            new URL(url).toURI();
            return true;

        } catch (Exception e) {
            return false;
        }
    }


    public static void main(String[] args){
        try (ServerSocket server = new ServerSocket(53)){
            System.out.println("Server: ON");

            while (true) {
                //Waiting for a new client
                Socket clientSocket = server.accept();
                System.out.println("New client connected to the sever...");

                //Creating the output and input stream for the client
                InputStream in = clientSocket.getInputStream();
                OutputStream out = clientSocket.getOutputStream();

                //Reading the length of the query
                byte[] lengthBuffer = new byte[2];
                in.read(lengthBuffer);
                int length = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

                //Reading the query until every bytes are read
                byte[] queryBuffer = new byte[length];
                in.read(queryBuffer);
                

                //Initialize the Query variable
                Query query = new Query(queryBuffer);

                //System.out.println(String.format("%8s", Integer.toBinaryString(query.header[0] & 0xFF)).replace(' ', '0') + String.format("%8s", Integer.toBinaryString(query.header[1] & 0xFF)).replace(' ', '0'));

                Response r = new Response(query);

                //System.out.println(String.format("%8s", Integer.toBinaryString(r.getResponse()[0] & 0xFF)).replace(' ', '0') + String.format("%8s", Integer.toBinaryString(r.getResponse()[1] & 0xFF)).replace(' ', '0'));


                out.write(r.getResponse());
                out.flush();
                clientSocket.close();
                
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
