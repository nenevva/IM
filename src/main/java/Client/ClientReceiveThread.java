package Client;

import DataBase.ChatLog;
import DataBase.ChatLogList;
import DataBase.Message;
import DataBase.MessageType;
import Server.Server;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientReceiveThread implements Runnable {

    private Client client;
    public Socket socket;
    private String hostName;
    private Gson gson = new Gson();
    private DataOutputStream output;
    final int PART_BYTE=4096-2-4;
    public ClientReceiveThread(String hostName,Client client, Socket socket) {
        this.hostName=hostName;
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            output=new DataOutputStream(socket.getOutputStream());
            DataInputStream input= new DataInputStream(socket.getInputStream());
            byte[] buffer=new byte[4096];
            while (true) {
                int length=input.read(buffer,0,4096);
                if(buffer[0]!=123){
                    handle(buffer);
                    continue;
                }
                String str=new String(buffer,0,length, StandardCharsets.UTF_8);
                Message message = gson.fromJson(str, Message.class);
                String body = message.getBody();
                SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
                String date=dateFormat.format(message.getTime());
                switch (message.getType()) {
                    case SUCCESS:
                        client.setId(message.getTo());
                        System.out.println(date+" "+"System:"+body);
                        break;
                    case GROUP_MSG:
                        System.out.println(date+" "+message.getFrom()+":"+body);
                        break;
                    case PRIVATE_MSG:
                        System.out.println(date+" "+message.getFrom()+"(私):"+body);
                        break;
                    case FAIL:
                        System.out.println(date+" "+"System:"+body);
                        break;
                    case USER_LIST:
                        parseUserList(body);
                        break;
                    case USER_NAME_LIST:
                        parseUserNameList(body);
                        break;
                    case GROUP_MSG_LOG:
                        parseGroupMsgLog(body);
                        break;
                    case PRIVATE_MSG_LOG:
                        parsePrivateMsgLog(body);
                        break;
                    case UPLOAD_FILE:
                        uploadFile(body);
                        break;
                    case UPLOAD_FILE_SUCCESS:
                        uploadFileSuccess(body);
                        break;
                    case FILE_INFO:
                        receiveFileInfo(message.getFrom(),body);
                        break;
                    case VIDEO_CAHT_REPLY:
                        handleVideoChatReply(message.getFrom(),body);
                        break;
                    case VIDEO_CHAT:
                        handleVideoChat(message.getFrom(),body);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(byte[] data){
        if(data[0]==1){
            if(data[1]==1){
                int part=(data[2]&0xff)<<24|(data[3]&0xff)<<16|(data[4]&0xff)<<8|(data[5]&0xff);
                try {


                    if(Client.receivedParts==Client.fileParts-1){
                        Client.saveFileOutput.write(data,6, (int) (Client.fileLength-PART_BYTE*Client.receivedParts));
                        Client.saveFileOutput.flush();
                        Client.saveFileOutput.close();
                        Client.saveFileOutput=null;
                    }
                    else{
                        Client.saveFileOutput.write(data,6,PART_BYTE);
                        Client.saveFileOutput.flush();
                        Client.receivedParts++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void parseUserList(String body){
        String[] data=body.split(";");
        HashMap<String, Integer> userList = new HashMap();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            userList.put(user[1], Integer.valueOf(user[0]));
        }
        System.out.println(userList);
    }

    private void parseUserNameList(String body){
        String[] data=body.split(";");
        for (int i = 0; i < data.length; i++) {
            System.out.println(data[i].split(":")[0]+"   "+data[i].split(":")[1]);
        }
    }

    private void parseGroupMsgLog(String body){
        ArrayList<ChatLog> chatLogs=gson.fromJson(body, ChatLogList.class).getChatLogs();
        System.out.println(chatLogs);
    }

    private void parsePrivateMsgLog(String body){
        ArrayList<ChatLog> chatLogs=gson.fromJson(body, ChatLogList.class).getChatLogs();
        System.out.println(chatLogs);
    }

    private void uploadFile(String body){
        System.out.println(Client.upLoadFileMap);
        int order= Integer.parseInt(body.substring(0,body.indexOf(";")));
        String fileName=body.substring(body.indexOf(";")+1);
        File file=Client.upLoadFileMap.get(fileName);
        if(file!=null){
            System.out.println("尝试发送"+file.getName()+"给服务器");
            byte[] data=new byte[4096];
            int partNum= (int) (file.length()/PART_BYTE+1);
            try {
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                byte[] fileBuffer=new byte[PART_BYTE];
                int part_i=0;
                while(bin.read(fileBuffer)>0){
                    part_i++;
                    data[0]=1;
                    data[1]= (byte) order;
                    System.arraycopy(intTobyte(part_i),0,data,2,4);
                    System.arraycopy(fileBuffer,0,data,6,PART_BYTE);
                    output.write(data);
                }
                bin.close();
                Client.upLoadFileMap.remove(body);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e){
                e.printStackTrace();
            }


        }
    }

    private void uploadFileSuccess(String body){
        System.out.println("文件"+body+"上传成功");
        Client.upLoadFileMap.remove(body);
    }

    private void receiveFileInfo(int from,String body){
        boolean isGroup= Boolean.parseBoolean(body.substring(0,body.indexOf(";")));
        body=body.substring(body.indexOf(";")+1);
        long fileLength= Long.parseLong(body.substring(0,body.indexOf(";")));
        String filename=body.substring(body.indexOf(";")+1);
        if(isGroup){

        }
        else {
            //TODO 在聊天界面显示相应提示,将文件信息/发送方保存到合适的结构中
            System.out.println("" + from + "尝试发送文件" + filename + ",文件字节" + fileLength);
        }
    }

    private void handleVideoChatReply(int from,String body){
        if(Client.videoChatID==from){
            if(body.equals("ok")){
                System.out.println("开始视频通话");
                Client.isVideo=true;
                new Thread(new VideoChatThread(hostName,1235,client.getId(),from)).start();
                new Thread(new VoiceChatThread(hostName,1236,client.getId(),from)).start();
            }
            else if(body.equals("reject")){
                System.out.println("对方拒绝了视频通话");
            }
        }
        else{
            String[] str=body.split(";");
            System.out.println(str[1]);
        }
    }

    private void handleVideoChat(int from,String body){
        System.out.println("用户"+from+"向你发起了视频通话");
        //TODO 前端
    }

    //int 转化为字节数组
    public static byte[] intTobyte(int num)
    {
        return new byte[] {(byte)((num>>24)&0xff),(byte)((num>>16)&0xff),(byte)((num>>8)&0xff),(byte)(num&0xff)};
    }

}