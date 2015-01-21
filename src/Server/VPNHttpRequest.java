package Server;

import java.util.HashMap;  
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import Server.RequestProcessor;


public class VPNHttpRequest {
    
    InputStream input;

    byte[] buffer;
    String requestString;
    String method;
    String uri;
    String host;
    String httpVersion;
    int port = 80;
    int requestLength;

    HashMap<String, String> headers = new HashMap<String, String>();
    
    public VPNHttpRequest(InputStream input) {
        this.input = input;
        parse();
    }
    
    private void parse() {
        // Read request from the socket
        StringBuffer request = new StringBuffer(2048);
        buffer = new byte[2048];

        try {
            requestLength = input.read(buffer);
            RequestProcessor.encryptConvert(buffer, requestLength);
        }
        catch (IOException e) {
            e.printStackTrace();
            requestLength = -1;
        }
        // TODO: remove this
        for (int i = 0; i < requestLength; i++) {
            request.append((char)buffer[i]);
        }

        // start parsing
        requestString = request.toString();
        //System.out.print(requestString);
        String[] lines = requestString.split("\n");
        // parse request line
        lines[0] = lines[0].trim();
        parseUriLine(lines[0]);
        // parse headers
        for (int i = 1; i < lines.length; i++) {
            lines[i] = lines[i].trim();
            int sepIndex = lines[i].indexOf(":");
            if (sepIndex < 0) {
                break;
            }
            String key = lines[i].substring(0, sepIndex).trim();
            String value = lines[i].substring(sepIndex + 1).trim();

            if (key.equals("Host")) {
                String[] host_port = value.split(":");
                if (host_port.length > 1) {
                    host = host_port[0].trim();
                    port = Integer.parseInt(host_port[1].trim());
                }
                else {
                    host = value;
                }
            }
            else if (key.equals("Connection")) {
                lines[i] = "Connection: close";
                value = "Close";
            }
            //System.out.println("putting: " + key + " -> " + value);
            headers.put(key, value);
        }
        requestString = "";
        for (String line: lines) {
            requestString = requestString + line + "\r\n";
        }
        System.out.println(requestString);
    }
    
    public byte[] getRequest() {
        return buffer;
    }

    public String getRequestString() {
        return requestString;
    }

    public int getRequestLength() {
        return requestLength;
    }

    public String getUri() {
        return uri;
    }

    public String getHost() {
        return host;
    }

    public String getHttpVersion() {
        return httpVersion;
    } 

    public int getPort() {
        return port;
    }

    public InputStream getInputStream() {
        return input;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }
    
    private void parseUriLine(String requestLine) {
        String[] results = requestLine.split(" ");
        method = results[0].trim();
        if (results.length > 1) {
            uri = results[1].trim();
        }
        if (results.length > 2) {
            httpVersion = results[2].substring(5).trim();
        }
        //System.out.println("hehe: " + method + " " + uri + " " + " " + httpVersion + " " + accpet);
    }

}