package Server;
  
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
    private static final int port = 8088;

    public static void main(String[] args) {
        // try {
        //     String hostname = "www.google.com";
        //     InetAddress ipaddress = InetAddress.getByName(hostname);
        //     System.out.println("IP address: " + ipaddress.getHostAddress());
        // }
        // catch (Exception e) {
        //     e.printStackTrace();
        //     System.exit(1);
        // }

        System.out.println("server web root is " + WEB_ROOT);            
        VPNServer server = new VPNServer();
        server.startup();
    }
    
    public void startup() {
        ServerSocket serverSocket = null;
        try {
            serverSocket =  new ServerSocket(port);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // // waiting for requests
        // ExecutorService cachedThreadPool = Executors.newCachedThreadPool();  
        // while (true) {
        //     Socket socket = null;
        //     try {
        //         socket = serverSocket.accept();
        //         cachedThreadPool.execute(new RequestProcessor(socket));
        //     }
        //     catch (Exception e) {
        //         e.printStackTrace();
        //         continue;
        //     }
        // }

        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                //System.out.println("\r\n\r\nAccept a new Connection\r\n\r\n");
                RequestProcessor rp =  new RequestProcessor(socket);
                rp.go();
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }
}