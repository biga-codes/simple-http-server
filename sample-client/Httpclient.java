import java.io.*;
import java.net.*;
import java.util.*;

public class Httpclient{
    
     public static void main(String[] args) {
        if (args.length != 1) {
            //only one allowed url 
            System.err.println("Usage: java HttpClient <url>");
            System.exit(1);
            
        }
        String url = args[0];
   // }
    int count=0;
    //location = url of redirect

    while (true) {

   // public boolean isValidURL(String url){

         if(!url.startsWith("http://")){
             System.err.println("Error: URL must start with http://");
             System.exit(1);
    }
         if(url.startsWith("https://")){
             System.err.println("Error: URL must start with http://, not https://");
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
    path= urlafterhttp.substring(slashIndex); // from second slash to end of string
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

        writer.print("GET "+path+" HTTP/1.0\r\n");
        writer.print("Host: "+host+"\r\n");
        writer.print("\r\n");
        writer.flush(); //dont wait to fill buffer

        
         
        //read first line from body i.e HTTP/1.0 200 OK
        InputStreamReader r= new InputStreamReader(socket.getInputStream());
        
        BufferedReader br = new BufferedReader(r);

        //buffered reader of the input stream
        String statusResponse =br.readLine();
        if(statusResponse == null){
            System.err.println("Error: empty response from server");
            socket.close(); //close the socket before exiting-> else loops
            System.exit(1);
        }
        String[] parts = statusResponse.split(" ");
        //convert to int berfore print

        String connectionType= null;
        String contentType = null;
        int contentLength = -1;
        String location = null;
        
        //buffered reader of the input stream -> convert byte to char and /r/n handle
        //dont call br.readLine thrice 

        while(true){
          
          
          
          String headerLabel = br.readLine();
          if(headerLabel == null|| headerLabel.isEmpty()){
            break; //line 172 -> or handles space after headers befw body
          }
          String[] arrayLabels = headerLabel.split(":");  

        
          //WE CAN PRINT RESPONSE BODY FOR EVERYTHING UNLESS REDIRECT

          if(arrayLabels[0].toLowerCase().equals("content-type")){
            contentType = arrayLabels[1].trim(); //may have spaces after colon " ":" "
          }
          else if(arrayLabels[0].toLowerCase().equals("content-length")){
            contentLength = Integer.parseInt(arrayLabels[1].trim()); // convert to int
          }
          else if(arrayLabels[0].toLowerCase().equals("connection")){
            connectionType = arrayLabels[1]; //no criteria in requirement
          }
          else if(arrayLabels[0].toLowerCase().equals("location")){
            location = arrayLabels[1].trim(); 
          }
          else{
               //pass
          }

          System.err.println(headerLabel); 


        }
        
         

        //no need for flag
        if(!contentType.startsWith("text/html")) {
            System.err.println("Error: content-type must start with text/html");
            socket.close();
            System.exit(1);
        }
        
       
        if(Integer.parseInt(parts[1])>=400){
            System.err.println("HTTP/1.0 "+ parts[1]+" NOT OK");
            //just have to print body(not obtain it here)
            //(Strings) in Java are UTF-16 data on the wire might be UTF-8
            //  ISO-8859-1, or raw binary -> handle in bytes
             //All OutputStreams in Java are strictly byte-oriented.

            byte buffer[] = new byte[8192];
            int bytesRead;
            InputStream in = socket.getInputStream();
            while((bytesRead= in.read(buffer)) != -1){
                if(bytesRead > 0){
                    System.out.write(buffer, 0, bytesRead); 
                }
               
            }
            System.out.flush();
            socket.close();
            System.exit(1);
        }

        if(Integer.parseInt(parts[1])==200 || Integer.parseInt(parts[1])<=299){
            System.err.println("HTTP/1.0 "+ parts[1]+ " OK");
            byte buffer[] = new byte[8192]; //8KB buffer-> infinite
            int bytesRead;
            InputStream in = socket.getInputStream();
            while((bytesRead= in.read(buffer)) != -1){
                 //read input stream bytes
                if(bytesRead > 0){
                    System.out.write(buffer, 0, bytesRead); //write to stdout (from buffer, start at 0, and write no of bytes)
                     //if only 2KB out of 8KB size
                }
            }
            System.out.flush();
            socket.close();
             System.exit(0);   
        }
             //need to close socket 
             // before system exit success 
             // or non success which is 
             // why handling here
       




        //redirects to label specified in location header
        if(Integer.parseInt(parts[1])==301 || Integer.parseInt(parts[1])==302){
            count++;
            if(location==null){
                System.err.println("Error: no location found");
                socket.close();
                System.exit(1);
            }
             url = location; 

            if(count>10){
                System.err.println("Error: too many redirects");
                socket.close();
                System.exit(1);
            }
            //handle location and no location
             System.err.println("Redirected to: "+ location);
             socket.close(); //-> to listen to new input stream for new url
             continue; //go back to start of while loop and handle new url

            
             
        }
           
         //if not redirect comes here closes while(true) after one pass/ or response header was empty
         socket.close();
         System.exit(0);


    }   
         catch(IOException e){
           System.err.println("Error: "+e.getMessage());
            System.exit(1);
    }


  }

   

}

}



// HTTP Client Requirements Summary
//
// INPUT:
//   -it is command line client
//   - URL must start with "http://" (not https) → else non-zero exit
//   - Support port numbers in URL (e.g. http://host:8080/path)
//   - Trailing slash optional for top-level URLs
//
// REQUEST:
//   - Use GET method only
//   - Must include "Host: ..." header - rest optional
//
// REDIRECTS (301/302):
//   - Follow redirects automatically
//   - Print to stderr: "Redirected to: <url>"
//   - Max 10 redirects → then non-zero exit
//   - keep reading html status code for redirects until you get 200 OK or >=400 error
//   - If redirect leads to https → print error to stderr, non-zero exit
//
// RESPONSE:
//   - 200 OK + valid HTML → print body to stdout, exit 0
//   - >= 400 → print body to stdout (if content-type is text/html), non-zero exit
//   - content-type must start with "text/html" → else non-zero exit (don't print response)
//
// OUTPUT:
//   - Print ONLY the response body (NO HEADERS) to stdout
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