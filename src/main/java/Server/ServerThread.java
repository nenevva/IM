package Server;

import DataBase.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
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
    private DataOutputStream output;
    private String filename;
    private long fileLength;
    private File saveFile;
    private FileOutputStream saveFileOutput;
    private int fileParts;
    private int receivedParts;
    private int fileTo;
    private boolean isGroup;
    final int PART_BYTE=4096-2-4;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        conn = DataBase.JDBC.getConnection();
        try {
            //BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output=new DataOutputStream(socket.getOutputStream());
            //writer = new PrintWriter(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            byte[] buffer=new byte[4096];

            //TODO 文件传输
            while (true) {
                int length=input.read(buffer,0,4096);
                //String str = reader.readLine();//读取客户端输入的内容
                if(buffer[0]!=123){
                    handle(buffer);
                    continue;
                }
                String str=new String(buffer,0,length, StandardCharsets.UTF_8);
                if(str==null){
                    logout(id);
                    break;
                }


                Message message = gson.fromJson(str, Message.class);
                System.out.println("receive:  "+str);
                String body="";
                if(message!=null)
                    body = message.getBody();
                else{
                }
                String[] data;

                switch (message.getType()) {
                    case LOGIN:
                        data = body.split(";");
                        if (body.length()<2) {
                            send(output, MessageType.FAIL, 0, 0, "输入不完整");
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
                            send(output, MessageType.FAIL,0,0,"输入不完整");
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
                    case FILE_INFO:
                        saveFile(message.getTo(), body);
                        break;
                    case RECEIVE_FILE:
                        sendFile(message.getTo(),message.getFrom(),body);
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

    private void handle(byte[] data){
        if(data[0]==1){
            if(data[1]==1){
                int part=(data[2]&0xff)<<24|(data[3]&0xff)<<16|(data[4]&0xff)<<8|(data[5]&0xff);
                try {


                    if(receivedParts==fileParts-1){
                        saveFileOutput.write(data,6, (int) (fileLength-PART_BYTE*receivedParts));
                        saveFileOutput.flush();
                        saveFileOutput.close();
                        saveFileOutput=null;
                        send(output,MessageType.UPLOAD_FILE_SUCCESS,0,id,saveFile.getName());
                        if(isGroup){

                        }
                        else {
                            Socket socketTo=Server.clientMap.get(fileTo);
                            if(socketTo!=null){
                                DataOutputStream output=new DataOutputStream(socketTo.getOutputStream());
                                send(output,MessageType.FILE_INFO,id,fileTo,"0;"+saveFile.length()+";"+saveFile.getName());
                            }
                        }
                        //通知收发双方
                    }
                    else{
                        saveFileOutput.write(data,6,PART_BYTE);
                        saveFileOutput.flush();
                        receivedParts++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void send(DataOutputStream output, MessageType type, int from, int to, String body) {
        System.out.println("send :"+gson.toJson(new Message(type, from, to, new Date(), body)));
        try {
            output.write((gson.toJson(new Message(type, from, to, new Date(), body))+"\n").getBytes(StandardCharsets.UTF_8));
        }
        catch (SocketException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
//        writer.println(gson.toJson(new Message(type, from, to, new Date(), body)));
//        writer.flush();
    }
    private void login(String username, String password) throws SQLException, IOException {

        System.out.println("username:" + username + " password:" + password);
        //数据库验证账号密码
        id = DataBase.User.userValidate(username, password);
        if (id != -1) {
            Server.clientMap.put(id, socket);
            send(output, MessageType.SUCCESS, 0, id, "login success");
        } else {
            send(output, MessageType.FAIL, 0, id, "login fail");
        }
    }

    private void register(String username, String password) throws SQLException {
        System.out.println("username:" + username + " password:" + password);
        //验证用户名是否已存在
        if (!DataBase.User.isUsernameAvailable(username)) {
            send(output, MessageType.FAIL, 0, id, "register fail");
            return;
        }
        //创建用户
        id=DataBase.User.getNewID();
        User usr = new User(id, password, username);
        DataBase.User.userCreate(usr);
        Server.clientMap.put(id, socket);
        send(output,MessageType.SUCCESS,0,id,"register success");
    }

    //向用户所在的群聊通知该用户下线
    private void logout(int from) {
        //遍历from所在的群聊列表，并sendGroup
        Server.clientMap.remove(id);
        try {
            Server.nameList= User.getAllName();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        sendGroup(0, 0, "用户"+Server.nameList.get(from) + "已下线");
        System.out.println("用户"+Server.nameList.get(from) + "已下线");

    }

    //to为群聊id
    private void sendGroup(int from, int to, String body) {
        DataBase.ChatContent.saveMsg(from,to,body,new Date(),1);
        for(Map.Entry<Integer,Socket> entry:Server.clientMap.entrySet()) {
            if(entry.getKey()!=from) {
                try {
                    //PrintWriter writer=new PrintWriter(entry.getValue().getOutputStream());
                    DataOutputStream output=new DataOutputStream(entry.getValue().getOutputStream());
                    send(output,MessageType.GROUP_MSG,from,to,body);
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
            send(output, MessageType.FAIL, 0, from, "用户未上线");
        }
        else{
            try {
                DataOutputStream output=new DataOutputStream(socketTo.getOutputStream());
                //PrintWriter writer = new PrintWriter(socketTo.getOutputStream());
                send(output, MessageType.PRIVATE_MSG, from, to, body);
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
        send(output, MessageType.USER_LIST, 0, from, body);
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
        send(output, MessageType.USER_NAME_LIST, 0, from, body);
    }

    private void sendGroupLog(int groupID){
        ArrayList<ChatLog> chatLogs=DataBase.ChatContent.getGroupChatLog(groupID);
        send(output,MessageType.GROUP_MSG_LOG,0,id,gson.toJson(new ChatLogList(chatLogs), ChatLogList.class));
    }

    private void sendPrivateLog(int id1,int id2){
        ArrayList<ChatLog> chatLogs=DataBase.ChatContent.getPrivateChatLog(id1,id2);
        send(output,MessageType.PRIVATE_MSG_LOG,0,id,gson.toJson(new ChatLogList(chatLogs), ChatLogList.class));
    }

    private void saveFile(int to,String body){
        fileTo=to;
        isGroup= Boolean.parseBoolean(body.substring(0,body.indexOf(";")));
        body=body.substring(body.indexOf(";")+1);
        fileLength= Long.parseLong(body.substring(0,body.indexOf(";")));
        filename=body.substring(body.indexOf(";")+1);
        fileParts= (int) (fileLength/PART_BYTE+1);
        receivedParts=0;
        if(isGroup){
            String path="file/group/"+to+"/"+id+"/";
            File file=new File(path);
            file.mkdirs();
            saveFile=new File(path+"/"+filename);
            try {
                saveFileOutput=new FileOutputStream(saveFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else {
            String path="file/private/"+id+"/"+to+"/";
            File file=new File(path);
            file.mkdirs();
            saveFile=new File(path+"/"+filename);
            try {
                saveFileOutput=new FileOutputStream(saveFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            send(output,MessageType.UPLOAD_FILE,0,id,filename);
        }
    }

    private void sendFile(int from,int to,String body){
        boolean isGroup= Boolean.parseBoolean(body.substring(0,body.indexOf(";")));
        filename=body.substring(body.indexOf(";")+1);
        if(isGroup){

        }
        else {
            String path = "file/private/" + from + "/" + to + "/" + filename;
            System.out.println(path);
            File file = new File(path);
            if (file != null) {
                try {
                    Server.nameList= User.getAllName();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                System.out.println("尝试发送" + file.getName()
                        + "给" + Server.nameList.get(to));
                byte[] data = new byte[4096];
                int partNum = (int) (file.length() / PART_BYTE + 1);
                try {
                    BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                    byte[] fileBuffer = new byte[PART_BYTE];
                    int part_i = 0;
                    while (bin.read(fileBuffer) > 0) {
                        part_i++;
                        data[0] = 1;
                        data[1] = 1;
                        System.arraycopy(intTobyte(part_i), 0, data, 2, 4);
                        System.arraycopy(fileBuffer, 0, data, 6, PART_BYTE);
                        output.write(data);
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //int 转化为字节数组
    public static byte[] intTobyte(int num) {
        return new byte[] {(byte)((num>>24)&0xff),(byte)((num>>16)&0xff),(byte)((num>>8)&0xff),(byte)(num&0xff)};
    }
}
