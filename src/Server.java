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
                if (in.read(queryBuffer) != length) {
                    clientSocket.setSoTimeout(5000);
                    clientSocket.close();
                }

                //Initialize the Query variable
                Query query = new Query(queryBuffer);
<<<<<<< HEAD

                //Check if the url is valid
                if (!checkUrl(query.getQuestionUrl())) {
                    out.write(new Response(query, 3).getResponse());
                    out.flush();
                    clientSocket.close();
                }


                Response r = new Response(query);

                clientSocket.close();


            }
=======

                
>>>>>>> b15c15ddd85e01939eee69eae58aee54ca038748

                //Check if the url is valid
                if (!checkUrl(query.getQuestionUrl())) {
                    out.write(new Response(query, 3).getResponse());
                    out.flush();
                    clientSocket.close();
                }

                Response r = new Response(query);

                clientSocket.close();

                
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
