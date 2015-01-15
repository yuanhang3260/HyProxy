package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
  
public class ClientExample {
    public static final String IP_ADDR = "localhost";//服务器地址   
    public static final int PORT = 8088;//服务器端口号    
      
    public static void main(String[] args) {
        System.out.println("Start Client ...");
        while (true) {
            Socket socket = null;
            try {
                //创建一个流套接字并将其连接到指定主机上的指定端口号
                socket = new Socket(IP_ADDR, PORT);
                
                //读取服务器端数据
                DataInputStream input = new DataInputStream(socket.getInputStream());
                //向服务器端发送数据
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.print("Please input: \t");
                String str = new BufferedReader(new InputStreamReader(System.in)).readLine();
                out.writeUTF(str);
                
                String ret = input.readUTF();
                System.out.println("Server returns: " + ret);
                
                out.close();
                input.close();
            }
            catch (Exception e) {
                System.out.println("Client Exception:" + e.getMessage());
            }
            finally {
                if (socket != null) {
                    try {
                        socket.close();
                    }
                    catch (IOException e) {
                        socket = null;
                        System.out.println("Client finally Exception:" + e.getMessage());
                    }  
                }  
            }  
        }
    }
}