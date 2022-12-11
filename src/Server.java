/**
 * \file Server.java
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
  * Server class is used as the main program. The program creates a multithreaded
  * server waiting for client connections. If a client initiates a connection,
  * the server receives and parses its query, forwards its HTTP request and
  * sends back an appropriate response. The program is designed to tackle errors
  * and respond with appropriate error messages.
  *
  */
import java.net.*;

// class Server - main program
public class Server{
    /*------------------------------------------------------------------------*/
    /*- Constructor ----------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    private static ServerSocket serverSocketConnection(int port) {
        try {
            return new ServerSocket(port);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open port" + port, e);
        }
    } // Server object constuctor
    /*------------------------------------------------------------------------*/
    /*- Private Static Methods -----------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn boolean manageArgs(String[] args)
     * \brief Manages the program inputs. Print help on stdout if necessary.
     *
     * \param args, string array containing the program arguments.
     *
     * \return  - true if the arguemnts are valid.
     *          - false if the arguments are not valid.
     *
     */
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
    /*------------------------------------------------------------------------*/
    /**
     * \fn void interrupt()
     * \brief Allows the program to print an exit message on stdout
     *        when being interrupted.
     *
     */
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
    /*------------------------------------------------------------------------*/
    /*- Public Methods -------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    /**
     * \fn void run(String[] args)
     * \brief Run() function of the server. Allows to accepts clients on
     *        separate threads as they come. The function is build around
     *        an infinite loo which can be interrupted by an interrupt
     *        signal such as <CTRL-C> on the CLI.
     *
     * \param args, string array containing the program arguments.
     *
     */
    public static void run(String[] args) {
        int serverPort = 53;
        ServerSocket serverSocket = serverSocketConnection(serverPort);

        // Initialise interrupt reaction
        interrupt();

        // Main loop
        while (true) {
            Socket client = null;
            try {
                client = serverSocket.accept(); // Init client socket
            } catch (Exception e) {
                throw new RuntimeException("Error with the connection to the client", e);
            }
            try {
                // Create new thread for client
                new Thread(new ClientRequest(client, args)).start();
            } catch (Exception e) {

            }
        }
    }
    /*------------------------------------------------------------------------*/
    /*- Main -----------------------------------------------------------------*/
    /*------------------------------------------------------------------------*/
    public static void main(String[] args){
        // Manage inputs, if the inputs are invalid, exit program.
        if(!manageArgs(args)){
            System.out.println("Arguments provided are not valid!") ;
            System.exit(0) ;
        }

        // Run server with correct inputs
        run(args);
    }
}
/*----------------------------------------------------------------------------*/
/*- END OF CLASS -------------------------------------------------------------*/
/*----------------------------------------------------------------------------*/
