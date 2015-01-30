package Server;

import java.lang.Thread;
import java.util.HashMap;  
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import Server.RequestProcessor;
import Util.BufferedDataReader;
import Util.SocketEncoder;
import Util.StandardBufferedInputStream;


public class VPNHttpRequest {
    
    InputStream input;

    byte[] buffer;
    String requestString;
    String method;
    String uri;
    String url;
    String host;
    String httpVersion;
    int port = 80;
    int requestLength;
    String protocl = "http";
    byte[] body;

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
        BufferedDataReader bufferedReader = new BufferedDataReader(input);
        
        try {
            // byte[] bytes = new byte[23];
            // int readNum = 0;
            // int total = 0;
            // BufferedDataReader bins = new BufferedDataReader(input, 79);
            // while (true) {
            //     //System.out.printf("server reading\n");
            //     readNum = bins.read(bytes, 0, bytes.length);
            //     if (readNum <= 0) {
            //         break;
            //     }
            //     total += readNum;
            //     System.out.printf("VPN server read %d bytes. Total %d\n", readNum, total);
            //     //System.out.printf("%s", new String(bytes));
            //     //RequestProcessor.encryptConvert(bytes, readNum);
            // }

            while (true) {
                lineNum++;
                String line = bufferedReader.readLine();
                
                if (lineNum == 1) {
                    parseUriLine(line);
                    continue;
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
                        host = host_port[0];
                        port = Integer.parseInt(host_port[1].trim());
                    }
                    else {
                        host = value;
                    }
                    // set URL
                    if (uri.startsWith("/")) {
                        url = protocl + host + uri;
                    }
                    else {
                        url = uri;
                        if (url.startsWith("http") || url.startsWith("Http")) {
                            String[] resource = url.split("//");
                            uri = resource[1].substring(resource[1].indexOf("/"));
                        }
                    }
                }
                else if (key.equals("Connection")) {
                    value = "Close";
                }
                //System.out.println("putting: " + key + " --- " + value);
                headers.put(key, value);
            }
            System.out.println("--------- end of request header ----------");

            if (method.equals("POST")) {
                if (headers.containsKey("Content-Length")) {
                    int contentLen = Integer.parseInt(headers.get("Content-Length"));
                    body = new byte[contentLen];
                    bufferedReader.read(body, 0, contentLen);
                    //System.out.println("Get Body: " + new String(body));
                }
                else {
                    // ?
                    int contentLen = 0;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            requestLength = -1;
        }
    }

    /**
     * parse request line
     * @param requestLine reqeust line
     */
    private void parseUriLine(String requestLine) {
        //System.out.println("parsing requestLine: " + requestLine);
        String[] results = requestLine.split(" ");
        method = results[0].trim();
        if (results.length > 1) {
            uri = results[1].trim();
        }
        if (results.length > 2) {
            httpVersion = results[2].substring(5).trim();
        }
        if (uri.startsWith("https")) {
            protocl = "https";
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

    public String getUrl() {
        return url;
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

    public byte[] getBody() {
        return body;
    }

    public boolean isHttpsRequest() {
        return protocl.equals("https");
    }

}