package Server;
  
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import Server.HttpRequest;
import Server.VPNServer;
  
/* 
  HTTP HttpResponse = Status-Line 
    *(( general-header | response-header | entity-header ) CRLF) 
    CRLF 
    [ message-body ] 
    Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF 
*/  
  
public class HttpResponse {
      
    private static final int BUFFER_SIZE = 1024;
    HttpRequest request;
    OutputStream output;
  
    public HttpResponse(OutputStream output) {
        this.output = output;
    }

    public HttpResponse(HttpRequest request, OutputStream output) {
        this.output = output;
        this.request = request;
    }
    
    public void setRequest(HttpRequest request) {
        this.request = request;
    }
    
    public void sendContents(byte[] contents, int length) {
        try {
            output.write(contents, 0, length);
        }
        catch (IOException e) {  
            System.out.println(e.toString() );
        }
    }

    public void sendStaticResource() throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];
        FileInputStream fis = null;
        try {
            String uri = request.getUri();
            if (uri.equals("/")) {
                uri = "/index.html";
            }
            File file = new File(VPNServer.WEB_ROOT, uri);
            if (file.exists()) {
                //System.out.println("file = " + file.getAbsolutePath());
                fis = new FileInputStream(file);
                int ch = 0;
                while ((ch = fis.read(bytes, 0, BUFFER_SIZE)) != -1) {
                    output.write(bytes, 0, ch);
                }
            }
            else {
                // file not found
                //System.out.println("file not found ");
                String errorMessage = "HTTP/1.1 404 File Not Found/r/n" +  
                                      "Content-Type: text/html/r/n" +  
                                      "Content-Length: 23/r/n" +  
                                      "/r/n" +  
                                      "<h1>File Not Found</h1>";
                output.write(errorMessage.getBytes());
            }
        }
        catch (Exception e) {
            // thrown if cannot instantiate a File object
            System.out.println(e.toString() );
        }
        finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public OutputStream getOutputStream() {
        return output;
    }
}