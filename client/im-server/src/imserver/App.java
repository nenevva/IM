package imserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class App {
    static int maxPeople = 10;
    static List<user> userbook = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8081);
        try{
            Socket socket = null;
            while(true){
                socket = serverSocket.accept();
                Thread thread = new Thread(new serverThread(socket));
                thread.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            serverSocket.close();
        }
        
    }
}
