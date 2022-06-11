package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerVoice implements Runnable{
    private int port;
    private ServerSocket serverSocket;
    public static Map<Integer,Socket> voiceClientMap = new HashMap<>();

    public ServerVoice(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            System.out.println("监听音频传输");
            serverSocket = new ServerSocket(port);
            while(true) {
                Socket socket = serverSocket.accept();
                new Thread(new ServerVoiceThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
