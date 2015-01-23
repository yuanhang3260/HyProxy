package Local;
  
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.util.concurrent.ExecutorService;  
import java.util.concurrent.Executors;


public class VPNLocal {
  
    /** WEB_ROOT is the directory where our HTML and other files reside. 
     *  For this package, WEB_ROOT is the "webroot" directory under the working 
     *  directory. 
     *  The working directory is the location in the file system 
     *  from where the java command was invoked. 
     */
    private static final int port = 8088;

    public static void main(String[] args) {
        VPNLocal local = new VPNLocal();
        local.startup();
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

        InputStream inVPN = null;
        OutputStream outVPN = null;
        try {
            //InetAddress inetAddress = InetAddress.getByName("128.2.220.18");
            // InetAddress inetAddress = InetAddress.getByName("localhost");
            // Socket socketVPN = new Socket(inetAddress, 12345);
            // inVPN = socketVPN.getInputStream();
            // outVPN = socketVPN.getOutputStream();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            Socket socket = null;
            try {
                //InetAddress inetAddress = InetAddress.getByName("128.2.220.16");
                InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
                Socket socketVPN = new Socket(inetAddress, 12345);
                inVPN = socketVPN.getInputStream();
                outVPN = socketVPN.getOutputStream();

                Socket socketClient = serverSocket.accept();
                System.out.println("Accept a new Connection");
                InputStream inCli = socketClient.getInputStream();
                OutputStream outCli = socketClient.getOutputStream();

                byte[] requstBuffer = new byte[2048];
                int len = inCli.read(requstBuffer);
                encryptConvert(requstBuffer, len);
                outVPN.write(requstBuffer, 0, len);
                outVPN.flush();

                // foward to VPN
                byte[] contentBuffer = new byte[2048];
                while (true) {
                    //System.out.printf("local reading\n");
                    int readNum = inVPN.read(contentBuffer, 0, contentBuffer.length);
                    if (readNum < 0) {
                        break;
                    }
                    encryptConvert(contentBuffer, readNum);
                    //System.out.printf("local read %d bytes\n", readNum);
                    //System.out.printf("%s", new String(contentBuffer));
                    outCli.write(contentBuffer, 0, readNum);
                }
                outCli.flush();

                // close socket to client
                inCli.close();
                outCli.close();
                socketClient.close();

                // close socket to VPN
                inVPN.close();
                outVPN.close();
                socketVPN.close();               
            }
            catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public void encryptConvert(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
    }
}