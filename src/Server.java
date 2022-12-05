import java.io.*;
import java.net.*;

public class Server{

    public static int checkQuery(byte[] query){
        

        //A faire pour alexandre 

        return 0;
    }

    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(53)){
            System.out.println("Server: ON");

            while (true) {
                Socket clientSocket = server.accept();
                System.out.println("New client connected to the sever...");

                byte[] lengthBuffer = new byte[2];
                InputStream in = clientSocket.getInputStream();

                in.read(lengthBuffer);
                int length = ((lengthBuffer[0] & 0xff) << 8) | (lengthBuffer[1] & 0xff);

                byte[] queryBuffer = new byte[length];
                if (in.read(queryBuffer) != length) {
                    clientSocket.setSoTimeout(5000);
                    clientSocket.close();
                }

                Query query = new Query(queryBuffer);

                System.out.println(RequestHTTP.Request(query));
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}