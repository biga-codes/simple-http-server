import java.io.*;
import java.net.*;
import java.util.*;

public class http_client{
    
     public static void main(String[] args) {
        if (args.length != 1) {
            //only one allowed url 
            System.err.println("Usage: java HttpClient <url>");
            System.exit(1);
            
        }
        String url = args[0];
   // }

   // public boolean isValidURL(String url){

         if(!url.startsWith("http://")){
             System.err.println("Error: URL must start with http://");
             System.exit(1);
    }
     
     //public String substring(int beginIndex) --format
     //we split into host and path (not port)
     //e.g. http://example.com:8080/deafult.html
     // inst: you should not require a slash at the end of top-level url

    String urlafterhttp = url.substring(7);
   
    String host,path;
    int port; 
    int slashIndex = urlafterhttp.indexOf('/');
    if (slashIndex == -1) {
    
    host = urlafterhttp;
    path = "/";
   
   } else {
    //goes up to, but not including second slashIndex
    host= urlafterhttp.substring(0, slashIndex); // from after first slash to before second slash
    path= urlafterhttp.substring(slashIndex+1); // from after second slash to end of string
    }

    int colonIndex = host.indexOf(':');

    if (colonIndex == -1) {
    port = 80; // default HTTP port
    }else{
         port = Integer.parseInt(host.substring(colonIndex + 1)); //colon +1 

         host= host.substring(0, colonIndex); 
    }

    try{
        //"hostname"
        /*Escape character is '^]'.
          GET /basic.html HTTP/1.0
           Host: ....com */
        Socket socket = new Socket(host, port);
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

        writer.print("GET /"+path+" HTTP/1.0\r\n");
        writer.print("Host: "+host+"\r\n");
        writer.print("\r\n");
        writer.flush(); //dont wait to fill buffer

        //body
         socket.close();
    }
    catch(IOException e){
        System.err("Error: "+e.getMessage());
        System.exit(1);
    }


  }

   

}



// HTTP Client Requirements Summary
//
// INPUT:
//-it is command line client
//   - URL must start with "http://" (not https) → else non-zero exit
//   - Support port numbers in URL (e.g. http://host:8080/path)
//   - Trailing slash optional for top-level URLs
//
// REQUEST:
//   - Use GET method only
//   - Must include "Host: ..." header
//
// REDIRECTS (301/302):
//   - Follow redirects automatically
//   - Print to stderr: "Redirected to: <url>"
//   - Max 10 redirects → then non-zero exit
//   - If redirect leads to https → print error to stderr, non-zero exit
//
// RESPONSE:
//   - 200 OK + valid HTML → print body to stdout, exit 0
//   - >= 400 → print body to stdout (if content-type is text/html), non-zero exit
//   - content-type must start with "text/html" → else non-zero exit (don't print body)
//
// OUTPUT:
//   - Print ONLY the response body (no headers) to stdout
//   - All other messages (errors, redirects) → stderr
//   - Exit code: 0 = success, non-zero = failure
//
// READING BODY:
//   - Use Content-Length if present to know when to stop reading
//   - If Content-Length missing → read until server closes connection (HTTP/1.0 style)
//   - Must handle large pages efficiently (no arbitrary timeouts)
//
// JAVA NOTES:
//   - Use Socket (plain TCP, NOT HttpURLConnection) to avoid auto-redirect/https handling
//   - Use System.out for body, System.err for errors/redirects
//   - Use System.exit(0) for success, System.exit(1) for failure
//Write your request to the Socket Output Stream so the server gets your message.
//Read the server's response from the Socket Input Stream.
//Print that response (the HTML) to the CLI (Stdout) so the user can see it.