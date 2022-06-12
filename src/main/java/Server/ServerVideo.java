package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerVideo implements Runnable{
    private int port;
    private ServerSocket serverSocket;
    public static Map<Integer,Socket> videoClientMap = new HashMap<>();

    public ServerVideo(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept();
                new Thread(new ServerVideoThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
