package Server;
  
import java.io.InputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import Server.RequestProcessor;

public class HttpRequest {
    
    InputStream input;

    byte[] buffer;
    String requestString;
    String method;
    String uri;
    String host;
    String httpVersion;
    String accpet;
    int port = 80;
    int requestLength;
    
    public HttpRequest(InputStream input) {
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
            // BufferedReader br = new BufferedReader(new InputStreamReader(input));
            // String str = null;
            // while((str = br.readLine()) != null) {
            //     requestString = requestString + str + "\r\n";
            //     if (str.equals("\r\n")) {
            //         break;
            //     }
            // }
            // requestString = requestString + "\r\n";
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
        // parse {method, uri, httpvesion} line
        parseUriLine(lines[0]);
        // parse other lines
        for (int i = 0; i < lines.length; i++) {
            lines[i] = lines[i].trim();
            String line = lines[i];
            if (line.startsWith("Host: ")) {
                line = line.substring(6);
                String[] host_port = line.split(":");
                if (host_port.length > 1) {
                    host = host_port[0].trim();
                    port = Integer.parseInt(host_port[1].trim());
                }
                else {
                    host = line;
                }
            }
            else if (line.startsWith("Accept: ")) {
                accpet = line.substring(8);
            }
            else if (line.startsWith("Connection: ")) {
                lines[i] = "Connection: close";
            }
        }
        requestString = "";
        for (String line: lines) {
            //System.out.println(lines[i]);
            // if (line.startsWith("Cookie: ")) {
            //     continue;
            // }
            requestString = requestString + line + "\r\n";
            // if (line.startsWith("Connection: ")) {
            //     requestString = requestString + "Proxy-Connection: Close\r\n";
            // }
        }
        //System.out.println(requestString);
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

    public String getAccept() {
        return accpet;
    }

    public int getPort() {
        return port;
    }

    public InputStream getInputStream() {
        return input;
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