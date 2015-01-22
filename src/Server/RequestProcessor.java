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
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import org.apache.http.HttpResponse;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.params.CoreConnectionPNames;

import Server.VPNHttpRequest;
import Server.VPNHttpResponse;

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
            InetAddress inetAddress = InetAddress.getByName(cliRequest.getHost().trim());
            System.out.println("*** hostname = " + cliRequest.getHost() + ", IP: " + inetAddress.getHostAddress());

            // client = new DefaultHttpClient();
            // HttpParams params = client.getParams();
            // params.setParameter(ClientPNames.DEFAULT_HOST, 
            //                     new HttpHost(InetAddress.getByName(cliRequest.getHost()), cliRequest.getPort()));
            // //params.setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
            
            // HttpGet request = new HttpGet(cliRequest.getUri());
            // HashMap<String, String> headers = cliRequest.getHeaders();
            // Iterator iter = headers.entrySet().iterator();
            // while (iter.hasNext()) {
            //     Map.Entry<String, String> pairs = (Map.Entry<String, String>)iter.next();
            //     if (pairs.getKey().equals("Accept-Encoding")) {
            //         continue;
            //     }
            //     request.addHeader(pairs.getKey(), pairs.getValue());
            //     //System.out.println("adding header - " + pairs.getKey() + " = " + pairs.getValue());
            // }

            // response = client.execute(request);

            //System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
            
            URL myurl = new URL(cliRequest.getUri());
            HttpURLConnection con = (HttpURLConnection)myurl.openConnection();
            InputStream ins = con.getInputStream(); // send http request

            VPNHttpResponse cliResponse = new VPNHttpResponse(cliRequest, socket.getOutputStream());
            //InputStream ins = response.getEntity().getContent();
            byte[] bytes = new byte[4096];
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
                cliResponse.sendContents(bytes, readNum);
            }

            // connnect remote host to get resource
            // Socket socketToHost = new Socket(inetAddress, request.getPort());
            // InputStream ins = socketToHost.getInputStream();
            // OutputStream outs = socketToHost.getOutputStream();
            // outs.write(request.getRequestString().getBytes(), 0, request.getRequestString().length());
            // System.out.print(request.getRequestString());
            // outs.flush();

            // // receive response from remote host and forward to client
            // HttpResponse response = new HttpResponse(request, socket.getOutputStream());
            // byte[] bytes = new byte[1024];
            // int readNum = 0;
            // while (true) {
            //     //System.out.printf("server reading\n");
            //     readNum = ins.read(bytes, 0, bytes.length);
            //     if (readNum < 0) {
            //         break;
            //     }
            //     //System.out.printf("server read %d bytes\n", readNum);
            //     //System.out.printf("%s", new String(bytes));
            //     //RequestProcessor.encryptConvert(bytes, readNum);
            //     response.sendContents(bytes, readNum);
            // }
            // response.getOutputStream().flush();

            // Close the socket to remote host
            System.out.printf("closing connection ......\n");
            ins.close();

            // Close socket to client
            cliRequest.getInputStream().close();
            cliResponse.getOutputStream().close();
            socket.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // finally {
        //     response.close();
        // }
    }

    public static byte[] encryptConvert(byte[] buffer, int len) {
        for (int i = 0; i < len; i++) {
            buffer[i] = (byte)(255 - (int)((int)buffer[i]&0xFF));
        }
        return buffer;
    }
}