package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Server {
    private int port;
    public static Map<Integer,Socket> clientMap = new HashMap<>();
    public static HashMap<Integer,String> nameList=null;
    public static List<Socket> socketList = new ArrayList<>();
    private ServerSocket serverSocket;

    public Server(int port) throws SQLException, ClassNotFoundException {

        new DataBase.JDBC();
        this.port = port;
        try {
            serverSocket = new ServerSocket(port);
            new Thread(new ServerVideo(1235)).start();
            new Thread(new ServerVoice(1236)).start();
            while(true) {
                Socket socket = serverSocket.accept();
                socketList.add(socket);
                new Thread(new ServerThread(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
