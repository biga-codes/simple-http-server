class HttpServer {
 public static void main(String[] args) {
        if (args.length != 1) { 
            System.err.println("Usage: java HttpServer <port>");
            System.exit(1);
            
        }
        int port = Integer.parseInt(args[0]);
        if(port < 1024){
            System.err.println("Error: Port number must be >= 1024");
            System.exit(1);
        }

        System.err.println("Server starting on port... " + port);


        try{
            //serversocket for server -> listens for incoming connections
            //server->socket listens
            //socket -> individual pipe for data exchange getInput getOutput
            ServerSocket serverSocket = new ServerSocket(port);
            System.err.println("Listening on port " + port);

            while(true){
               // When accept() returns, it gives you a new Socket object
               
                Socket individualConnectionSocket = serverSocket.accept();
                 System.err.println("New connection from: " + 
            connectionSocket.getInetAddress());

            connectionSocket.close(); 
            }
             
        }
        catch(IOException e){
            System.err.println("Error: " + e.getMessage());
            System.exit(1);

        }
 
    }

}







// HTTP Server Requirements Summary
//
// INPUT:
//   - Command line server: java HttpServer <port>
//   - Port must be >= 1024
//   - Listen on all IP addresses ("" / 0.0.0.0)
//
// SERVER SETUP:
//   - Create a TCP ServerSocket bound to the given port
//   - Listen for incoming connections in a loop (one at a time)
//   - Each connection gets its own "connection socket" from accept()
/*When accept() returns, it gives you a new Socket object. This specific socket is the dedicated pipe for that one specific client.

Why it exists: By creating a separate socket for each connection, 
the ServerSocket is immediately freed up to go back to accept() 
and wait for the next person 
at the front door while you handle the first person's request */
//
// REQUEST PARSING:
//   - Read the HTTP request from the connection socket
//   - Parse the first line e.g. "GET /file.html HTTP/1.1"
//   - Extract the requested file path
//
// FILE RULES:
//   - Only serve files in the CURRENT directory
//   - File must end with ".html" or ".htm" → else 403 Forbidden
//   - File must exist → else 404 Not Found
//   - If file exists and is valid → 200 OK + send file contents as body
//
// RESPONSE FORMAT:
//   - Status line: "HTTP/1.0 200 OK\r\n"
//   - Headers: "Content-Type: text/html\r\n" + blank line "\r\n"
//   - Then the file contents as the body
//
// OUTPUT:
//   - Send HTTP response (headers + body) to the connection socket
//   - Print status messages to stderr for your own debugging
//
// JAVA NOTES:
//   - Use ServerSocket to listen, Socket to handle each connection
//   - Read request from socket InputStream
//   - Write response to socket OutputStream
//   - Close connection socket after each request (not the server socket)
//   - Loop forever waiting for next connection