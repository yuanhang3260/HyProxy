package Server;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
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
            
            // create Response object
            HttpResponse response = new HttpResponse(request, socket.getOutputStream());
            response.sendStaticResource();
            
            // Close the socket  
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}