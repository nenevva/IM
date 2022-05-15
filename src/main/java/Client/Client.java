package Client;

import DataBase.Message;
import DataBase.MessageType;
import com.google.gson.Gson;
import GUI.Model.Content;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

public class Client {
    private String hostName;
    private int port;
    private String userName;
    private Integer id = -1;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Gson gson = new Gson();

    public Client(String hostName, int port) {

        this.hostName = hostName;
        this.port = port;
        try {
            socket = new Socket(hostName,port);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ClientReceiveThread(this,socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(MessageType type,int to,String s){
        writer.println(gson.toJson(new Message(type,id,to,new Date(),s)));
        writer.flush();
    }
    public void closeConnect() {
        if(writer != null) {
            writer.close();
        }
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void login(String userName,String password){
        sendMsg(MessageType.LOGIN,0,""+userName+";"+password);
    }

    public void register(String userName,String password){
        sendMsg(MessageType.REGISTER,0,""+userName+";"+password);
    }

    public void sendGroupMsg(int to,String msg){
        msg= Content.userName+";"+msg;
        sendMsg(MessageType.GROUP_MSG,to,msg);
    }

    public void sendPrivateMsg(int to,String msg){
        msg = Content.userName+";"+msg;
        sendMsg(MessageType.PRIVATE_MSG,to,msg);
    }
}
