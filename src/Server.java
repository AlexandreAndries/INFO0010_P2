import java.net.*;

public class Server{

    private static ServerSocket serverSocketConnection(int port) {
        try {
            return new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open port" + port, e);
        }
    }

    private static void interrupt() {
      Runtime
        .getRuntime()
        .addShutdownHook(
          new Thread() {
            public void run() {
              try {
                Thread.sleep(50);
                System.out.println();
                System.out.println("Server shutting down\n");
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
              }
            }
          }
        );
    }

    public static void run(String[] args) {
        int serverPort = 53;
        ServerSocket serverSocket = serverSocketConnection(serverPort);

        interrupt();
        while (true) {
            Socket client = null;
            try {
                client = serverSocket.accept();
                // System.out.println("New client connected " + client.getInetAddress().getHostAddress());
            } catch (Exception e) {
                throw new RuntimeException("Error with the connection to the client", e);
            }
            try {
                new Thread(new ClientRequest(client, args)).start();
            } catch (Exception e) {

            }
        }
    }

    public static void main(String[] args){
        run(args);
    }
}
