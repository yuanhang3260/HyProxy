package Server;

import java.lang.Thread;
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
    boolean isHttps;

    HashMap<String, String> headers = new HashMap<String, String>();
    
    public VPNHttpRequest(InputStream input) {
        this.input = input;
        parse();
    }
    
    private void parse() {
        // parse headers
        parseRequest();

        // create request string
        requestString = "";
        //System.out.println(requestString);
    }


    /**
     *  parse Request
     */
    private void parseRequest() {
        int lineNum = 0;
        BufferedReader bufferedReader = 
             new BufferedReader(new InputStreamReader(input));
        
        try {
            while (true) {
                lineNum++;
                String line = bufferedReader.readLine();
                line = new String(RequestProcessor.encryptConvert(line.getBytes(), line.length()));

                if (lineNum == 1) {
                    parseUriLine(line);
                }

                int sepIndex = line.indexOf(":");
                if (sepIndex < 0) { // empty line
                    break;
                }

                String key = line.substring(0, sepIndex).trim();
                String value = line.substring(sepIndex + 1).trim();

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
                    value = "Close";
                }
                //System.out.println("putting: " + key + " -> " + value);
                headers.put(key, value);

                //System.out.printf("%s", new String(buffer));
            }
            System.out.println("hehe");
        }
        catch (IOException e) {
            e.printStackTrace();
            requestLength = -1;
        }
    }

    /**
     * parse request line
     * @param requestLine reqeust line
     */
    private void parseUriLine(String requestLine) {
        String[] results = requestLine.split(" ");
        method = results[0].trim();
        if (results.length > 1) {
            uri = results[1].trim();
        }
        if (results.length > 2) {
            httpVersion = results[2].substring(5).trim();
        }
        if (uri.startsWith("https")) {
            isHttps = true;
        }
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

    public String getMethod() {
        return method;
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

    public boolean isHttpsRequest() {
        return isHttps;
    }

}