package Client;

import DataBase.Message;
import DataBase.MessageType;
import GUI.Controller.PrivateController;
import com.google.gson.Gson;
import GUI.Controller.LoginController;
import GUI.Controller.MainController;
import GUI.Model.Content;
import GUI.Model.StageManager;
import javafx.application.Platform;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Util.FileSaver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.plaf.metal.MetalBorders.PaletteBorder;

public class ClientReceiveThread implements Runnable {

    private final Client client;
    public Socket socket;
    private final String hostName;
    private final Gson gson = new Gson();
    private DataOutputStream output;

    protected LoginController loginController = LoginController.getInstance();
    protected MainController mainController;

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
                        Content.id=message.getTo();
                        System.out.println(date+" "+"System:"+body);
                        Platform.runLater(() ->{
                            loginController.changeMain();
                            mainController=MainController.getInstance();
                            mainController.updateUserList();
                        });
                        break;
                    case GROUP_MSG:
                        System.out.println(date+" "+message.getFrom()+":"+body);
                        Platform.runLater(() ->{
                            mainController.addGroupMsg(message.getFrom(), message.getTo(), message.getTime(),body);
                        });
                        break;
                    case PRIVATE_MSG:
                        System.out.println(date+" "+message.getFrom()+"(私):"+body);

                        if(Content.privateChatRecord.containsKey(message.getFrom())){
                            Content.privateChatRecord.get(message.getFrom()).add("0" + body);
                        }else{
                            ArrayList<String> newRecord = new ArrayList<>();
                            newRecord.add("1" + body);
                            Content.privateChatRecord.put(message.getFrom(), newRecord);
                        }
                        Platform.runLater(() ->{
                            if(Content.privateChatWindows.containsKey(message.getFrom())){
                                System.out.println("addPrivate");
                                Content.privateChatWindows.get(message.getFrom()).
                                        addPrivate(message.getFrom(), message.getTo(), message.getTime(), body);
                            }
                        });
                        break;
                    case FAIL:
                        System.out.println(date+" "+"System:"+body);
                        Content.userName="";
                        break;
                    case USER_LIST:
                        parseUserList(body);
                        break;
                    case USER_NAME_LIST:
                        parseUserNameList(body);
                        break;
                    case UPLOAD_FILE:
                        uploadFile(body);
                        break;
                    case UPLOAD_FILE_SUCCESS:
                        uploadFileSuccess(body);
                        break;
                    case FILE_INFO:
                        receiveFileInfo(message.getFrom(), message.getTo(), body);
                        break;
                    case VIDEO_CAHT_REPLY:
                        handleVideoChatReply(message.getFrom(),body);
                        break;
                    case VIDEO_CHAT:
                        handleVideoChat(message.getFrom(),body);
                        break;
                }
            }
        }
        catch (SocketException e){

        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handle(byte[] data){
        if(data[0]==1){
            int index=data[1];
            FileSaver fileSaver=Content.currentDownloadFileMap.get(index);
            int part=(data[2]&0xff)<<24|(data[3]&0xff)<<16|(data[4]&0xff)<<8|(data[5]&0xff);
            try {
                fileSaver.write(part,data);
                if(fileSaver.isFinish()){
                    System.out.println(fileSaver.getFileName()+"接收完成");
                    Content.currentDownloadFileMap.remove(index);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void parseUserList(String body){
        String[] data=body.split(";");
        Content.userList.clear();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            if(user.length<2)
                break;
            Content.userList.put(user[1], Integer.valueOf(user[0]));
        }
    }

    private void parseUserNameList(String body){
        String[] data=body.split(";");
        Content.idNameRecord.clear();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            if(user.length<2)
                break;
            Content.idNameRecord.put( Integer.valueOf(user[0]),user[1]);
        }
        System.out.println(Content.idNameRecord);
    }

    private void uploadFile(String body){
        int order= Integer.parseInt(body.substring(0,body.indexOf(";")));
        String fileName=body.substring(body.indexOf(";")+1);
        File file=Content.upLoadFileMap.get(fileName);
        if(file!=null){
            System.out.println("尝试发送"+file.getName()+"给服务器");
            byte[] data=new byte[4096];
            int partNum= (int) (file.length()/Content.PART_BYTE+1);
            try {
                BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
                byte[] fileBuffer=new byte[Content.PART_BYTE];
                int part_i=0;
                while(bin.read(fileBuffer)>0){
                    part_i++;
                    data[0]=1;
                    data[1]= (byte) order;
                    System.arraycopy(intTobyte(part_i),0,data,2,4);
                    System.arraycopy(fileBuffer,0,data,6,Content.PART_BYTE);
                    output.write(data);
                }
                bin.close();

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
        Content.upLoadFileMap.remove(body);
        //前端显示发送完成
        Platform.runLater(()->{
            Stage alert = new Stage();
            VBox vbox = new VBox();
            Label label = new Label("文件"+ body+"上传成功");
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().add(label);
            Scene scene = new Scene(vbox);
            alert.setScene(scene);
            alert.setTitle("文件上传");
            alert.show();
        });

    }

    private void receiveFileInfo(int from,int to,String body){
        boolean isGroup= body.substring(0,body.indexOf(";")).equals("1");
        body=body.substring(body.indexOf(";")+1);
        long fileLength= Long.parseLong(body.substring(0,body.indexOf(";")));
        String filename=body.substring(body.indexOf(";")+1);
        if(isGroup){
            //在聊天界面显示相应提示
            Platform.runLater(()->{
                mainController.addGroupFile(from, to, fileLength, filename);
            });
            FileSaver fileSaver=new FileSaver(from,to,fileLength,filename,isGroup);
            if(Content.groupFileReceiveMap.get(from)==null)
                Content.groupFileReceiveMap.put(from,new HashMap<String,FileSaver>());
            Content.groupFileReceiveMap.get(from).put(filename,fileSaver);
            System.out.println("" + from + "尝试发送文件(群聊)" + filename + ",文件字节" + fileLength);
            //此时默认下载文件
            //Content.client.receiveFileGroup(fileSaver.getFileName(),fileSaver.getFileLength(),fileSaver.getFrom(),fileSaver.getTo());
        }
        else {
            //在聊天界面显示相应提示
            if(Content.privateChatRecord.containsKey(from)){
                Content.privateChatRecord.get(from).add("0" + filename);
            }else{
                ArrayList<String> newRecord = new ArrayList<>();
                newRecord.add("1" + filename);
                Content.privateChatRecord.put(from, newRecord);
            }
            Platform.runLater(() ->{
                if(Content.privateChatWindows.containsKey(from)){
                    System.out.println("addPrivate");
                    Content.privateChatWindows.get(from).
                            addFile(from, fileLength, filename);
                }
            });

            //
            FileSaver fileSaver=new FileSaver(from,Content.id,fileLength,filename,isGroup);
            if(Content.privateFileReceiveMap.get(from)==null)
                Content.privateFileReceiveMap.put(from,new HashMap<String,FileSaver>());
            Content.privateFileReceiveMap.get(from).put(filename,fileSaver);
            System.out.println("" + from + "尝试发送文件" + filename + ",文件字节" + fileLength);

            //Content.client.receiveFilePrivate(fileSaver.getFileName(),fileSaver.getFileLength(),fileSaver.getFrom());
        }
    }

    private void handleVideoChatReply(int from,String body){
        if(Content.videoChatID==from){
            if(body.equals("ok")){
                System.out.println("开始视频通话");
                //TODO 前端开启视频窗口
                Content.isVideo=true;
                Content.isVoice=true;
                Platform.runLater(()->{
                    mainController.showVideo(Content.idNameRecord.get(from));
                });
                new Thread(new VideoChatThread(Content.server,Content.port+1,client.getId(),from)).start();
                new Thread(new VoiceChatThread(Content.server,Content.port+2,client.getId(),from)).start();
            }
            else if(body.equals("reject")){
                System.out.println("对方拒绝了视频通话");
                //聊天界面显示
                Platform.runLater(()->{
                    Content.privateChatWindows.get(from).addPrivateSelf(from, new Date(), "对方拒绝了视频通话");
                });            
            }
        }
        else{
            String[] str=body.split(";");
            System.out.println(str[1]);
        }
    }

    private void handleVideoChat(int from,String body){
        System.out.println("用户"+Content.idNameRecord.get(from)+"向你发起了视频通话");
        //聊天界面显示,能够选择接收或拒绝
        Platform.runLater(()->{
            Stage alert = new Stage();
            VBox vbox = new VBox();
            Label label = new Label("用户"+Content.idNameRecord.get(from)+"向你发起了视频通话");
            vbox.setAlignment(Pos.CENTER);
            HBox hbox = new HBox();
            Button accept = new Button("接受");
            Button reject = new Button("拒绝");
            accept.setOnMouseClicked((event) -> {
                Content.client.sendVideoChatReply(from, "ok");
                alert.close();
            });
            reject.setOnMouseClicked((event) -> {
                Content.client.sendVideoChatReply(from, "reject");
                alert.close();
            });
            hbox.getChildren().addAll(accept, reject);
            vbox.getChildren().addAll(label, hbox);
            
            Scene scene = new Scene(vbox);
            alert.setScene(scene);
            alert.setTitle("视频请求");
            alert.show();
        });
        

    }

    //int 转化为字节数组
    public static byte[] intTobyte(int num)
    {
        return new byte[] {(byte)((num>>24)&0xff),(byte)((num>>16)&0xff),(byte)((num>>8)&0xff),(byte)(num&0xff)};
    }
}
