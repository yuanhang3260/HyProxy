package Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
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
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
// import org.apache.http.conn.params.ConnRoutePNames;
// import org.apache.http.client.params.ClientPNames;
// import org.apache.http.impl.client.DefaultHttpClient;
// import org.apache.http.params.HttpParams;
// import org.apache.http.params.CoreConnectionPNames;

import Server.VPNHttpRequest;
import Server.VPNHttpResponse;
import Util.BufferedDataReader;
import Util.SocketEncoder;

public class RequestProcessor implements Runnable {

	Socket socket = null;

	public RequestProcessor(Socket socket) {
		this.socket = socket;
	}

    @Override
    public void run() {
        
    }

    public void go() {
        HttpClient client = null;
        HttpResponse response = null;
        try {
        	// create Request object and parse
            // TODO: hostname = null ?
            VPNHttpRequest cliRequest = new VPNHttpRequest(socket.getInputStream());
            if (cliRequest.getHost() == null) {
                return;
            }
            InetAddress inetAddress = InetAddress.getByName(cliRequest.getHost().trim());
            System.out.println("*** hostname = " + cliRequest.getHost() + ", IP: " + inetAddress.getHostAddress());
            System.out.println("Method = " + cliRequest.getMethod());
            System.out.println("Url = " + cliRequest.getUrl());
            System.out.println("Uri = " + cliRequest.getUri());

            URL url = new URL(cliRequest.getUrl());
            HttpURLConnection conn = null;
            if (cliRequest.isHttpsRequest()) {
                conn = (HttpsURLConnection)url.openConnection();
            }
            else {
                conn = (HttpURLConnection)url.openConnection();
            }

            // send the request to remote host and wait for response
            InputStream ins = sendRequest(conn, cliRequest);
            
            VPNHttpResponse cliResponse = new VPNHttpResponse(cliRequest, socket.getOutputStream());
            byte[] bytes = new byte[4096];
            int readNum = 0;
            while (true) {
                //System.out.printf("server reading\n");
                readNum = ins.read(bytes, 0, bytes.length);
                if (readNum <= 0) {
                    break;
                }
                //System.out.printf("server read %d bytes\n", readNum);
                //System.out.printf("%s", new String(bytes));
                //RequestProcessor.encryptConvert(bytes, readNum);
                cliResponse.sendContents(bytes, readNum);
            }

            // Close the socket to remote host
            System.out.printf("closing connection ......\n\n");
            ins.close();

            // Close socket to client
            cliRequest.getInputStream().close();
            cliResponse.getOutputStream().close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private InputStream sendRequest(URLConnection conn, VPNHttpRequest cliRequest) {
        // add headers
        HashMap<String, String> headers = cliRequest.getHeaders();
        Iterator iter = headers.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, String> pairs = (Map.Entry<String, String>)iter.next();
            if (pairs.getKey().equals("Accept-Encoding")) {
                continue;
            }
            conn.setRequestProperty(pairs.getKey(), pairs.getValue());
            //System.out.println("adding header - " + pairs.getKey() + " = " + pairs.getValue());
        }

        InputStream ins = null;
        try {
            //conn.connect();

            // if a POST request
            if (cliRequest.getMethod().equals("POST")) {
                conn.setDoOutput(true);
                OutputStream outs = conn.getOutputStream();
                outs.write(cliRequest.getBody());
            }

            // send http request to socket
            ins = conn.getInputStream();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return ins;
    }

    public static byte[] encryptConvert(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
        return buffer;
    }
}