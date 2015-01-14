package Server;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
  
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;

import Server.HttpRequest;
import Server.HttpResponse;
  
public class VPNServer {
  
    /** WEB_ROOT is the directory where our HTML and other files reside. 
     *  For this package, WEB_ROOT is the "webroot" directory under the working 
     *  directory. 
     *  The working directory is the location in the file system 
     *  from where the java command was invoked. 
     */
    public static final String WEB_ROOT =  
                        System.getProperty("user.dir") + File.separator  + "webroot";  

    public static void main(String[] args) {
        System.out.println("server web root is " + WEB_ROOT);
        VPNServer server = new VPNServer();
        server.startup();
    }
    
    public void startup() {
        ServerSocket serverSocket = null;
        int port = 8088;
        try {
            serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
  
        // Loop waiting for a request  
        while (true) {
            Socket socket = null;
            InputStream input = null;
            OutputStream output = null;
            try {
                socket = serverSocket.accept();
                input = socket.getInputStream();
                output = socket.getOutputStream();
      
                // create Request object and parse  
                HttpRequest request = new HttpRequest(input);
                
                // create Response object  
                HttpResponse response = new HttpResponse(output);
                response.setRequest(request);
                response.sendStaticResource();
          
                // Close the socket  
                socket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}