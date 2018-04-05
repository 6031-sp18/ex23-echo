package echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class EchoServer {

    // whether we can handle only one client at a time (single-threaded server)
    // or multiple clients (multi-threaded server)
    private static boolean ONE_CLIENT_AT_A_TIME = true;
    
    /**
     * Listen for a connection from an EchoClient and then echo back each line of its input
     * until it disconnects, then wait for another client.
     * @param args unused
     */
    public static void main(String[] args) {

        final int port = 4589;

        try ( // try-with-resources: automatically closes the socket declared here
                // open the server socket
                final ServerSocket serverSocket = new ServerSocket(port);
            ) {
            while (true) {
                // get the next client connection
                final Socket socket = serverSocket.accept();

                if (ONE_CLIENT_AT_A_TIME) {
                    // handle the client in the main thread
                    handleClient(socket);
                } else {
                    // handle the client in a new thread, so that the main thread
                    // can resume waiting for another client
                    new Thread(new Runnable() {
                        public void run() {
                            handleClient(socket);
                        }
                    }).start();
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } // serverSocket is automatically closed here
    }
    
    private static void handleClient(final Socket socket) {
        try ( // try-with-resources: automatically closes the stream variables declared here
            // get an output stream for the client connection,
            // and wrap it in a PrintWriter so we can use print()/println() operations
            final PrintWriter writeToClient = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                
            // get an input stream for the client connection,
            // and wrap it into a BufferedReader for the readLine() operation
            final BufferedReader readFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        ) {
            while (true) {
                // read a message from the client
                final String message = readFromClient.readLine();
                if (message == null) break; // client closed its side of the connection
                if (message.equals("quit")) break; // client sent a quit message
                
                // prepare a reply, in this case just echoing the message
                final String reply = "echo: " + message;

                // write the reply to the client
                writeToClient.println(reply);
                writeToClient.flush(); // important! otherwise the reply may just sit in a buffer, unsent
            }
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } // outToClient, inFromClient are automatically closed here by the try-with-resources

        try {
            socket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }    
    }
}
