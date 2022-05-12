package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private int port;
    public static Map<Integer,Socket> clientMap=new HashMap<>();
    public static List<Socket> socketList=new ArrayList<>();
    private ServerSocket serverSocket;

    public Server(int port) {
        this.port = port;
        try {
            serverSocket=new ServerSocket(port);
            while(true){
                Socket socket=serverSocket.accept();
                socketList.add(socket);
                new Thread(new ServerThread(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
