package Server;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.File;
import Server.HttpRequest;
import Server.HttpResponse;

public class RequestProcessor implements Runnable {

	Socket socket = null;

	public RequestProcessor(Socket socket) {
		this.socket = socket;
	}

    @Override
    public void run() {
        try {
            // create Request object and parse
            HttpRequest request = new HttpRequest(socket.getInputStream());

            // connnect remote host to get resource
            System.out.println("hostname = " + request.getHost());
            InetAddress inetAddress = InetAddress.getByName(request.getHost().trim());
            System.out.println("IP address: " + inetAddress.getHostAddress());
            Socket socketToHost = new Socket(inetAddress, request.getPort()); 
            InputStream ins = socketToHost.getInputStream();
            OutputStream outs = socketToHost.getOutputStream();
            outs.write(request.getRequestString().getBytes(), 0, request.getRequestString().length());
            //System.out.print(request.getRequestString());
            outs.flush();

            // receive response from remote host and forward to client
            HttpResponse response = new HttpResponse(request, socket.getOutputStream());
            byte[] bytes = new byte[1024];
            int readNum = 0;
            while (true) {
                //System.out.printf("reading\n");
                readNum = ins.read(bytes, 0, bytes.length);
                if (readNum < 0) {
                    break;
                }
                //System.out.printf("read %d bytes\n", readNum);
                //System.out.printf("%s", new String(bytes));
                response.sendContents(bytes, readNum);
            }
            response.getOutputStream().flush();

            System.out.printf("closing connection ......\n");
            outs.close();
            ins.close();

            // Close the socket  
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void go() {
        try {
        	// create Request object and parse
            HttpRequest request = new HttpRequest(socket.getInputStream());

            // connnect remote host to get resource
            System.out.println("hostname = " + request.getHost());
            InetAddress inetAddress = InetAddress.getByName(request.getHost().trim());
            System.out.println("IP address: " + inetAddress.getHostAddress());
            Socket socketToHost = new Socket(inetAddress, request.getPort()); 
            InputStream ins = socketToHost.getInputStream();
            OutputStream outs = socketToHost.getOutputStream();
            outs.write(request.getRequestString().getBytes(), 0, request.getRequestString().length());
            //System.out.print(request.getRequestString());
            outs.flush();

            // receive response from remote host and forward to client
            HttpResponse response = new HttpResponse(request, socket.getOutputStream());
            byte[] bytes = new byte[1024];
            int readNum = 0;
            while (true) {
                //System.out.printf("reading\n");
                readNum = ins.read(bytes, 0, bytes.length);
                if (readNum < 0) {
                    break;
                }
                //System.out.printf("read %d bytes\n", readNum);
                //System.out.printf("%s", new String(bytes));
                response.sendContents(bytes, readNum);
            }
            response.getOutputStream().flush();

            System.out.printf("closing connection ......\n");
            outs.close();
            ins.close();

            // Close the socket  
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}