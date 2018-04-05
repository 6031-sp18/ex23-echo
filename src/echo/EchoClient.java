package echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 */
public class EchoClient {
    
    /**
     * Make a socket connection to an EchoServer (required to be already running) 
     * and sends the input by the user to the server, displaying the server's replies.
     * @param args unused
     */
    public static void main(String[] args) {
        
        final String hostname = "localhost";
        final int port = 4589;
        
        try ( // try-with-resources: will automatically close the socket when the try statement exits
                
            // connect to the server
            final Socket socket = new Socket(hostname, port);

        ) {

            System.err.println("connected to server; type a line to see it echoed, type quit to exit");
            talkToServer(socket);
            System.err.println("server disconnected");
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } 
        // socket.close() is automatically called here by the try-with-resources

    }
    
    private static void talkToServer(final Socket socket) {
        try ( // try-with-resources: will autoclose all the stream variables declared between the parens

            // get an output stream for the server connection,
            // and wrap it in a PrintWriter so we can use print()/println() operations
            final PrintWriter writeToServer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                
            // get an input stream for the server connection,
            // and wrap it into a BufferedReader for the readLine() operation
            final BufferedReader readFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            // wrap the standard input (the user's keyboard) in a BufferedReader
            // for the readLine() operation.
            final BufferedReader readFromUser = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ) {
            
            while (true) {
                // read a message from the keyboard
                final String message = readFromUser.readLine();
                if (message == null) break; // user closed standard input (typically by typing Ctrl-D)
                
                // write the message to the server
                writeToServer.println(message);
                writeToServer.flush(); // important! otherwise the message may just sit in a buffer, unsent
                
                // read a reply from the server
                final String serverReply = readFromServer.readLine();                
                if (serverReply == null) break; // server closed connection
                
                // show the reply to the user
                System.out.println(serverReply);
                // don't need to call System.out.flush(), because System.out is autoflushing
            }
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } 
        // writeToServer.close(), readFromServer.close(), readFromUser.close() 
        // are automatically called here by the try-with-resources
    }
}