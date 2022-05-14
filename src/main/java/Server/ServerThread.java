package Server;

import DataBase.Message;
import DataBase.MessageType;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;

public class ServerThread implements Runnable {

    private Connection conn = null;
    public Socket socket;
    private int id;
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
            writer = new PrintWriter(socket.getOutputStream());;
            while (true) {
                String str = reader.readLine();//读取客户端输入的内容
                Message message = gson.fromJson(str, Message.class);
                String body = message.getBody();
                System.out.println(str);
                String[] data;

                //TODO 用户在登录系统成功之前不能发送其它类型的信息

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
                }
            }
        } catch (SocketException e) {
            //Socket断开，用户离线
            logout(id);
            Server.clientMap.remove(id);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("数据库已离线");
        }

    }

    private void send(PrintWriter writer, MessageType type, int from, int to, String body) {

        writer.println(gson.toJson(new Message(type, from, to, new Date(), body)));
        writer.flush();
    }

    private void login(String username, String password) throws SQLException {

        System.out.println("username:" + username + " password:" + password);
        //数据库验证账号密码
        id = DataBase.User.userValidate(username, password);
        if (id != -1) {
            Server.clientMap.put(id, socket);
            try {
                PrintWriter writer = new PrintWriter(socket.getOutputStream());
                send(writer, MessageType.SUCCESS, 0, id, "login success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            send(writer, MessageType.FAIL, 0, id, "login fail");
        }
    }

    private void register(String username, String password) {
        System.out.println("username:" + username + " password:" + password);
    }

    //向用户所在的群聊通知该用户下线
    private void logout(int from) {
        //遍历from所在的群聊列表，并sendGroup
        sendGroup(0, 0, "用户" + from + "已下线");
        System.out.println("用户" + from + "已下线");
    }

    //to为群聊id
    private void sendGroup(int from, int to, String body) {

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
}
