package Client;

import DataBase.Message;
import DataBase.MessageType;
import com.google.gson.Gson;
import Util.FileSaver;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

public class Client {
    private String hostName;
    private int port;
    private String userName;
    private Integer id = -1;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Gson gson = new Gson();
    public static HashMap<String,File> upLoadFileMap =new HashMap<>();
    public static File fileReceive;
    public static long fileLength;
    public static int receivedParts;
    public static int fileParts;
    public static FileOutputStream saveFileOutput;
    public static int currentDownloadFileNum=0;
    private HashMap<Integer,FileSaver> fileSaverMap=new HashMap<>();
    final int PART_BYTE=4096-2-4;
    public static int videoChatID;
    public static Boolean isVideo=false;

    public Client(String hostName, int port) {

        this.hostName = hostName;
        this.port = port;
        try {
            socket = new Socket(hostName,port);
            writer = new PrintWriter(socket.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new Thread(new ClientReceiveThread(hostName, this,socket)).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(MessageType type,int to,String s){
        System.out.println("send "+s);
        String str=gson.toJson(new Message(type,id,to,new Date(),s));
        str=str+"\n";
        if(str.getBytes(StandardCharsets.UTF_8).length<4096){
            str=str+new String(new char[4096-str.getBytes(StandardCharsets.UTF_8).length]).replace("\0", " ");
        }
        writer.print(str);
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

    public void sendFilePrivate(String filename, int to){
        File file=new File(filename);
        upLoadFileMap.put(file.getName(),file);
        sendMsg(MessageType.FILE_INFO,to,"0;"+file.length()+";"+file.getName());
    }

    public void receiveFilePrivate(String filename,Long fileLength,int from){

    }

    public void startVideoChat(int to){
        sendMsg(MessageType.VIDEO_CHAT,to,"");
        videoChatID=to;
    }

    public void sendVideoChatReply(int to,String reply){

        videoChatID=to;
        sendMsg(MessageType.VIDEO_CAHT_REPLY,to,reply);
    }
}
