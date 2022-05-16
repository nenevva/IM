package Server;

import DataBase.*;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class ServerThread implements Runnable {

    private Connection conn = null;
    public Socket socket;
    private int id = -1;
    private Gson gson = new Gson();
    private PrintWriter writer;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        conn = DataBase.JDBC.getConnection();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream());
            //TODO 文件传输
            while (true) {
                String str = reader.readLine();//读取客户端输入的内容
                if(str==null){
                    logout(id);
                    break;
                }
                Message message = gson.fromJson(str, Message.class);
                String body="";
                if(message!=null)
                    body = message.getBody();
                else{
                }
                System.out.println("receive:  "+str);
                String[] data;

                switch (message.getType()) {
                    case LOGIN:
                        data = body.split(";");
                        if (body.length()<2) {
                            send(writer, MessageType.FAIL, 0, 0, "输入不完整");
                        }
                        else {
                            login(data[0], data[1]);
                        }
                        break;
                    case LOGOUT:
                        logout(message.getFrom());
                        break;
                    case REGISTER:
                        data = body.split(";");
                        if(body.length()<2) {
                            send(writer, MessageType.FAIL,0,0,"输入不完整");
                        }
                        else {
                            register(data[0],data[1]);
                        }
                        break;
                    case GROUP_MSG:
                        sendGroup(message.getFrom(), message.getTo(), message.getBody());
                        break;
                    case PRIVATE_MSG:
                        sendPrivate(message.getFrom(), message.getTo(), message.getBody());
                        break;
                    case USER_LIST:
                        sendUserList(message.getFrom());
                        break;
                    case USER_NAME_LIST:
                        sendUserNameList(message.getFrom());
                        break;
                    case GROUP_MSG_LOG:
                        sendGroupLog(Integer.parseInt(body));
                        break;
                    case PRIVATE_MSG_LOG:
                        sendPrivateLog(Integer.parseInt(body.split(";")[0]), Integer.parseInt(body.split(";")[1]));
                        break;
                }
            }
        }
        catch (SocketException e) {
            //Socket断开，用户离线
            logout(id);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库已离线");
        }

    }

    private void send(PrintWriter writer, MessageType type, int from, int to, String body) {
        System.out.println("send :"+gson.toJson(new Message(type, from, to, new Date(), body)));
        writer.println(gson.toJson(new Message(type, from, to, new Date(), body)));
        writer.flush();
    }
    private void login(String username, String password) throws SQLException, IOException {

        System.out.println("username:" + username + " password:" + password);
        //数据库验证账号密码
        id = DataBase.User.userValidate(username, password);
        if (id != -1) {
            Server.clientMap.put(id, socket);
            PrintWriter writer = new PrintWriter(socket.getOutputStream());
            send(writer, MessageType.SUCCESS, 0, id, "login success");
        } else {
            send(writer, MessageType.FAIL, 0, id, "login fail");
        }
    }

    private void register(String username, String password) throws SQLException {
        System.out.println("username:" + username + " password:" + password);
        //验证用户名是否已存在
        if (!DataBase.User.isUsernameAvailable(username)) {
            send(writer, MessageType.FAIL, 0, id, "register fail");
            return;
        }
        //创建用户
        id=DataBase.User.getNewID();
        User usr = new User(id, password, username);
        DataBase.User.userCreate(usr);
        Server.clientMap.put(id, socket);
        send(writer,MessageType.SUCCESS,0,id,"register success");
    }

    //向用户所在的群聊通知该用户下线
    private void logout(int from) {
        //遍历from所在的群聊列表，并sendGroup
        try {
            Server.nameList= User.getAllName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendGroup(0, 0, "用户"+Server.nameList.get(from) + "已下线");
        System.out.println("用户"+Server.nameList.get(from) + "已下线");
        Server.clientMap.remove(id);
    }

    //to为群聊id
    private void sendGroup(int from, int to, String body) {
        DataBase.ChatContent.saveMsg(from,to,body,new Date(),1);
        for(Map.Entry<Integer,Socket> entry:Server.clientMap.entrySet()) {
            if(entry.getKey()!=from) {
                try {
                    PrintWriter writer=new PrintWriter(entry.getValue().getOutputStream());
                    send(writer,MessageType.GROUP_MSG,from,to,body);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendPrivate(int from, int to, String body) {
        DataBase.ChatContent.saveMsg(from,to,body,new Date(),0);
        Socket socketTo=Server.clientMap.get(to);
        if(socketTo==null) {
            send(writer, MessageType.FAIL, 0, from, "用户未上线");
        }
        else{
            try {
                PrintWriter writer = new PrintWriter(socketTo.getOutputStream());
                send(writer, MessageType.PRIVATE_MSG, from, to, body);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //返回所有的在线用户
    private void sendUserList(int from){
        String body="";
        Server.nameList=null;
        try {
            Server.nameList= User.getAllName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(Map.Entry<Integer,Socket> entry:Server.clientMap.entrySet()) {
            if(entry.getKey()!=from) {
                String name=Server.nameList.get(entry.getKey());
                body+=entry.getKey()+":"+name+";";
            }
        }
        send(writer, MessageType.USER_LIST, 0, from, body);
    }

    private void sendUserNameList(int from){
        String body="";
        Server.nameList=null;
        try {
            Server.nameList= User.getAllName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for(Map.Entry<Integer,String > entry:Server.nameList.entrySet()) {
                String name= entry.getValue();
                body+=entry.getKey()+":"+name+";";
        }
        System.out.println(Server.nameList);
        send(writer, MessageType.USER_NAME_LIST, 0, from, body);
    }

    private void sendGroupLog(int groupID){
        ArrayList<ChatLog> chatLogs=DataBase.ChatContent.getGroupChatLog(groupID);
        send(writer,MessageType.GROUP_MSG_LOG,0,id,gson.toJson(new ChatLogList(chatLogs), ChatLogList.class));
    }

    private void sendPrivateLog(int id1,int id2){
        ArrayList<ChatLog> chatLogs=DataBase.ChatContent.getPrivateChatLog(id1,id2);
        send(writer,MessageType.PRIVATE_MSG_LOG,0,id,gson.toJson(new ChatLogList(chatLogs), ChatLogList.class));
    }
}
