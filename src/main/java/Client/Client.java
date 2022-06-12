package Client;

import DataBase.Message;
import DataBase.MessageType;
import com.google.gson.Gson;
import GUI.Model.Content;
import Util.FileSaver;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class Client {
    private final String hostName;
    private final int port;
    private String userName;
    private Integer id = -1;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private DataOutputStream outputStream;
    private final Gson gson = new Gson();

    public Client(String hostName, int port) {

        this.hostName = hostName;
        this.port = port;
        try {
            socket = new Socket(hostName,port);
            writer = new PrintWriter(socket.getOutputStream());
            outputStream=new DataOutputStream(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ClientReceiveThread(hostName, this,socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(MessageType type,int to,String s){
        String str=gson.toJson(new Message(type,id,to,new Date(),s));
        str=str+"\n";
//        if(str.getBytes(StandardCharsets.UTF_8).length<Content.bytelength){
//            str=str+new String(new char[Content.bytelength-str.getBytes(StandardCharsets.UTF_8).length]).replace("\0", " ");
//        }
//        writer.print(str);
//        writer.flush();
        byte[] sizeAr = ByteBuffer.allocate(4).putInt(str.getBytes(StandardCharsets.UTF_8).length).array();
        try {
            outputStream.write(sizeAr);
            outputStream.write(str.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

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
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void logout(){sendMsg(MessageType.LOGOUT,0,"");}

    public void register(String userName,String password){
        sendMsg(MessageType.REGISTER,0,""+userName+";"+password);
    }

    public void sendGroupMsg(int to,String msg){
        sendMsg(MessageType.GROUP_MSG,to,msg);
    }

    public void sendPrivateMsg(int to,String msg){
        sendMsg(MessageType.PRIVATE_MSG,to,msg);
    }

    public void getUserList(){
        sendMsg(MessageType.USER_LIST,0,"");
    }

    public void getUserNameList(){sendMsg(MessageType.USER_NAME_LIST,0,"");}

    public void getGroupMsgLog(int groupID){
        sendMsg(MessageType.GROUP_MSG_LOG,0, String.valueOf(groupID));
    }

    public void getPrivateMsgLog(int id){
        sendMsg(MessageType.PRIVATE_MSG_LOG,0,""+this.id+";"+id);
    }

    public boolean isClose(){
        return socket.isClosed();
    }

    public void sendFilePrivate(String filename, int to){
        File file=new File(filename);
        Content.upLoadFileMap.put(file.getName(),file);
        sendMsg(MessageType.FILE_INFO,to,"0;"+file.length()+";"+file.getName());
    }

    public void sendFileGroup(String filename, int to){
        File file=new File(filename);
        Content.upLoadFileMap.put(file.getName(),file);
        sendMsg(MessageType.FILE_INFO,to,"1;"+file.length()+";"+file.getName());
    }

    public void receiveFilePrivate(String filename,Long fileLength,int from){
        //TODO
        FileSaver fileSaver=Content.privateFileReceiveMap.get(from).get(filename);
        fileSaver.startSave();
        Content.currentDownloadFileMap.put(Content.currentDownloadFileMap.size()+1,fileSaver);
        sendMsg(MessageType.RECEIVE_FILE,fileSaver.getFrom(),"0;"+Content.currentDownloadFileMap.size()+";"+filename);
    }

    public void receiveFileGroup(String filename,Long fileLength,int from,int groupID){
        //TODO
        FileSaver fileSaver=Content.groupFileReceiveMap.get(from).get(filename);
        fileSaver.startSave();
        Content.currentDownloadFileMap.put(Content.currentDownloadFileMap.size()+1,fileSaver);
        sendMsg(MessageType.RECEIVE_FILE,fileSaver.getFrom(),"1;"+Content.currentDownloadFileMap.size()+";"+groupID+";"+filename);
    }

    public void startVideoChat(int to){
        sendMsg(MessageType.VIDEO_CHAT,to,"");
        Content.videoChatID=to;
        System.out.println("向"+Content.idNameRecord.get(to)+"发起视频通话");
    }

    public void sendVideoChatReply(int to,String reply){

        Content.videoChatID=to;
        sendMsg(MessageType.VIDEO_CHAT_REPLY,to,reply);
    }
}
