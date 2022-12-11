import java.net.*;

public class Server{

    private static ServerSocket serverSocketConnection(int port) {
        try {
            return new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open port" + port, e);
        }
    }

    private static boolean manageArgs(String[] args) {
        // Send help to stdout if asked
        if(args.length == 1 && (args[0].equals("h") || args[0].equals("help"))){
            System.out.println("Help :") ;
            System.out.println("Launch server using the following command :\n");
            System.out.println("\t java Server <owned domain name> \n");
            System.out.println("Example : java Server tnl.test");
            System.exit(0);
        }

        // Program only accepts 1 argument :
        //    - a valid domain name uner the responsability of the server
        // If not enough arguments (or too many) are provided, the program should
        // detect it, return and error and end.
        if(args == null || args.length > 1 || args.length == 0){
            return false ;
        } else {
            return true ;
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
                System.out.println("Shutting down Server\n");
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
        if(!manageArgs(args)){
            System.out.println("Arguments provided are not valid!") ;
            System.exit(0) ;
        }

        run(args);
    }
}
