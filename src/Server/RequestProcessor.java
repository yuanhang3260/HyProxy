package Server;

import java.net.Socket;
import java.lang.Thread;
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
            // TODO: hostname = null ?
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
                RequestProcessor.encryptConvert(bytes, readNum);
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
            // TODO: hostname = null ?
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
                //System.out.printf("server reading\n");
                readNum = ins.read(bytes, 0, bytes.length);
                if (readNum < 0) {
                    break;
                }
                //System.out.printf("server read %d bytes\n", readNum);
                //System.out.printf("%s", new String(bytes));
                //RequestProcessor.encryptConvert(bytes, readNum);
                response.sendContents(bytes, readNum);
            }
            response.getOutputStream().flush();

            // Close the socket to remote host
            System.out.printf("closing connection ......\n");
            outs.close();
            ins.close();
            socketToHost.close();

            // Close socket to client
            request.getInputStream().close();
            response.getOutputStream().close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] encryptConvert(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
        return buffer;
    }
}