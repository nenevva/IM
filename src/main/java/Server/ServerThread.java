package Server;

import DataBase.*;
import com.google.gson.Gson;
import Util.FileSaver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerThread implements Runnable {

    private int byte_length=8196*20;
    private Connection conn = null;
    public Socket socket;
    private int id = -1;
    private Gson gson = new Gson();
    private DataOutputStream output;
    private String filename;
    final int PART_BYTE=byte_length-2-4;
    private int currentUploadFileNum=0;
    private HashMap<Integer,FileSaver> fileSaverMap=new HashMap<>();

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        conn = DataBase.JDBC.getConnection();
        try {
            File file=new File("log.bin");
            FileOutputStream fos=new FileOutputStream(file);
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());

            byte[] my=new byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
            //TODO 文件传输
            while (true) {
                int length=input.readInt();
                byte[] buffer=new byte[length];
                input.readFully(buffer);
                fos.write(length);
                fos.write(buffer);
                fos.write(my);
                if(buffer[0]!=123){
                    handle(buffer);
                    continue;
                }
                String str=new String(buffer,0,length, StandardCharsets.UTF_8);
                if(str.indexOf("\n")>-1){
                    str=str.substring(0,str.indexOf("\n"));
                }
                if(str==null){
                    logout(id);
                    break;
                }


                Message message = gson.fromJson(str, Message.class);
                if(message.getType()!=MessageType.USER_LIST)
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
                        throw new SocketException();
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
                    case VIDEO_CHAT:
                        sendVideoChatRequire(message.getTo());
                        break;
                    case VIDEO_CHAT_REPLY:
                        sendVideoChatReply(message.getTo(),body);
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



    private void send(DataOutputStream output, MessageType type, int from, int to, String body) {
        String str=gson.toJson(new Message(type, from, to, new Date(), body));
        if(type!=MessageType.USER_LIST)
            System.out.println("send :"+str);
        str=str+"\n";
        byte[] data=str.getBytes(StandardCharsets.UTF_8);
        try {
            byte[] sizeAr = ByteBuffer.allocate(4).putInt(data.length).array();
            output.write(sizeAr);
            output.write(data);
        }
        catch (SocketException e){
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void login(String username, String password) throws SQLException, IOException {


        //数据库验证账号密码
        id = DataBase.User.userValidate(username, password);
        if (id != -1) {
            System.out.println("username:" + username + " login");
            Server.clientMap.put(id, socket);
            send(output, MessageType.SUCCESS, 0, id, "login success");
        } else {
            send(output, MessageType.FAIL, 0, id, "login fail");
        }
    }

    private void register(String username, String password) throws SQLException {

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
        System.out.println("register: username:" + username + " password:" + password+" id:"+id);
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
        System.out.println("user "+Server.nameList.get(from) + " logout");

    }

    //to为群聊id
    private void sendGroup(int from, int to, String body) {
        DataBase.ChatContent.saveMsg(from,to,body,new Date(),1);
        for(Map.Entry<Integer,Socket> entry:Server.clientMap.entrySet()) {
            if(entry.getKey()!=from) {
                try {
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
        currentUploadFileNum++;
        boolean isGroup= body.substring(0,body.indexOf(";")).equals("1");
        body=body.substring(body.indexOf(";")+1);
        long fileLength= Long.parseLong(body.substring(0,body.indexOf(";")));
        String filename=body.substring(body.indexOf(";")+1);
        FileSaver fileSaver=new FileSaver(id,to,fileLength,filename,isGroup);
        fileSaver.startSave();
        fileSaverMap.put(currentUploadFileNum,fileSaver);

        send(output,MessageType.UPLOAD_FILE,0,id,""+currentUploadFileNum+";"+filename);
        System.out.println("start receive file "+filename);
    }

    private void handle(byte[] data){
        if(data[0]==1){
            int index=data[1];
            FileSaver fileSaver=fileSaverMap.get(index);
            int part=(data[2]&0xff)<<24|(data[3]&0xff)<<16|(data[4]&0xff)<<8|(data[5]&0xff);
            try {
                fileSaver.write(part,data);
                if(fileSaver.isFinish()){
                    if(fileSaver.isGroup()){
                        send(output,MessageType.UPLOAD_FILE_SUCCESS,0,id,fileSaver.getFileName());
                        for(Map.Entry<Integer,Socket> entry:Server.clientMap.entrySet()) {
                            if(entry.getKey()!=id) {
                                try {
                                    DataOutputStream output=new DataOutputStream(entry.getValue().getOutputStream());
                                    send(output,MessageType.FILE_INFO,id,fileSaver.getTo(),"1;"+fileSaver.getFileLength()+";"+fileSaver.getFileName());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    else {
                        System.out.println("file "+fileSaver.getFileName()+" receive success");
                        send(output,MessageType.UPLOAD_FILE_SUCCESS,0,id,fileSaver.getFileName());
                        Socket socketTo=Server.clientMap.get(fileSaver.getTo());
                        if(socketTo!=null){
                            DataOutputStream output=new DataOutputStream(socketTo.getOutputStream());
                            send(output,MessageType.FILE_INFO,id,fileSaver.getTo(),"0;"+fileSaver.getFileLength()+";"+fileSaver.getFileName());
                        }
                    }
                    fileSaverMap.remove(index);
                    currentUploadFileNum--;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendFile(int from,int to,String body){
        boolean isGroup= body.substring(0,body.indexOf(";")).equals("1");
        filename=body.substring(body.indexOf(";")+1);
        int order= Integer.parseInt(filename.substring(0,filename.indexOf(";")));
        filename=filename.substring(filename.indexOf(";")+1);
        File file=null;
        if(isGroup){
            int groupID=Integer.parseInt(filename.substring(0,filename.indexOf(";")));
            filename=filename.substring(filename.indexOf(";")+1);
            file=new File("file/group/"+groupID+"/"+from+"/"+filename);
        }
        else {
            file=new File("file/private/"+from+"/"+to+"/"+filename);
        }
        System.out.println(file.getName());
        if(file!=null) {
            System.out.println("send file " + file.getName() + "to user "+Server.nameList.get(id));
            byte[] data = new byte[byte_length];
            int partNum = (int) (file.length() / PART_BYTE + 1);
            try {
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                byte[] fileBuffer = new byte[PART_BYTE];
                int part_i = 0;
                while (bin.read(fileBuffer) > 0) {
                    part_i++;
                    data[0] = 1;
                    data[1] = (byte) order;
                    System.arraycopy(intTobyte(part_i), 0, data, 2, 4);
                    System.arraycopy(fileBuffer, 0, data, 6, PART_BYTE);
                    byte[] sizeAr = ByteBuffer.allocate(4).putInt(data.length).array();
                    output.write(sizeAr);
                    output.write(data);
                }
                bin.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendVideoChatRequire(int to){
        Socket socketTo=Server.clientMap.get(to);
        if(socketTo==null) {
            send(output, MessageType.VIDEO_CHAT_REPLY, 0, id, "fail;用户未上线");
        }
        else{
            try {
                DataOutputStream output=new DataOutputStream(socketTo.getOutputStream());
                send(output, MessageType.VIDEO_CHAT, id, to, "");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendVideoChatReply(int to,String relpy){
        Socket socketTo=Server.clientMap.get(to);
        if(socketTo==null) {
            send(output, MessageType.VIDEO_CHAT_REPLY, 0, id, "fail;用户未上线");
        }
        else{
            try {
                DataOutputStream output=new DataOutputStream(socketTo.getOutputStream());
                send(output, MessageType.VIDEO_CHAT_REPLY, id, to, relpy);
                if(relpy.equals("ok")){
                    send(this.output,MessageType.VIDEO_CHAT_REPLY,to,id,relpy);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //int 转化为字节数组
    public static byte[] intTobyte(int num) {
        return new byte[] {(byte)((num>>24)&0xff),(byte)((num>>16)&0xff),(byte)((num>>8)&0xff),(byte)(num&0xff)};
    }


}
