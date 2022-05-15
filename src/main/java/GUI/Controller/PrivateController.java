package GUI.Controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import GUI.Model.Content;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

//
//import bupt.edu.models.Content;
//import bupt.edu.models.PrivateChatUser;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.Button;
//import javafx.scene.control.ListView;
//import javafx.scene.control.TextArea;
//
//import java.io.*;
//import java.net.InetAddress;
//import java.net.Socket;
//import java.util.ArrayList;
//import java.util.Iterator;
//
public class PrivateController {
    private String privateUser;
    private int privateUserID;
//    private ArrayList<String> msg;
//    private InetAddress ipaddress;
    @FXML
    private Button send;
    @FXML
    private Button file;
    @FXML
    private Button video;
    @FXML
    private TextArea textArea;
    @FXML
    private VBox private_info_vb;
    @FXML
    public void initialize(){
        
//        //初始化
       // 私聊对象名
        privateUser = Content.foucedPrivateUser;
        privateUserID = Content.userList.get(privateUser);
        if(Content.privateChatRecord.containsKey(privateUser)){
            ArrayList<String> record = Content.privateChatRecord.get(privateUser);
            for(Iterator<String> it = record.iterator(); it.hasNext();){
                String msg = it.next();
                String type = msg.substring(0,1);
                String body = msg.substring(1);
                if(type.equals("0")){
                    addPrivate(privateUser, privateUserID, Content.client.getId(), new Date(), body);
                }
                if(type.equals("1")){
                    addPrivateSelf(Content.client.getId(),new Date(), body);
                }
            }
        }
//        //私聊信息
//        int symbol = 0;
//        for(Iterator<PrivateChatUser> it = Content.privateChatUserList.iterator(); it.hasNext();){
//            PrivateChatUser privateChatUser = it.next();
//            if(privateChatUser.userID.equals(privateUserID)){
//                msg = privateChatUser.msg;
//                symbol = 1;
//                break;
//            }
//        }
//        if(symbol == 0){
//            PrivateChatUser newchat = new PrivateChatUser();
//            newchat.userID = privateUserID;
//            msg = newchat.msg;
//            Content.privateChatUserList.add(newchat);
//        }
//        //对方ip
//        try{
//            Socket socket  = new Socket("localhost", 8081);
//            OutputStream os = socket.getOutputStream();
//            PrintWriter ps = new PrintWriter(os);
//            ps.write("01" + privateUserID);
//            ps.flush();
//            socket.shutdownOutput();
//            InputStream is = socket.getInputStream();
//            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
//            BufferedReader br = new BufferedReader(isr);
//            String msg = br.readLine();
//            String instruction = msg.substring(0,2);
//            String data  = msg.substring(2);
//            if(instruction.equals("01")){
//                ipaddress = InetAddress.getByName(data);
//            }
//            socket.close();
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//
//        //刷新消息
//        new Thread(new Runnable() {
//            @Override
//            public void run(){
//                while(true){
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                    }
//                    refreshTextList();
//                }
//            }
//        }).start();
//
    }
//
//
//    //刷新消息
//    public void refreshTextList(){
//        Platform.runLater(()->{
//            ObservableList<String> strlist = FXCollections.observableArrayList(msg);
//            listView.setItems(strlist);
//        });
//    }
//
//
    //发送信息
    @FXML
    public void sendMsg() throws Exception{
        Content.client.sendPrivateMsg(privateUserID, textArea.getText());
        if(Content.privateChatRecord.containsKey(privateUser)){
            Content.privateChatRecord.get(privateUser).add("1" + textArea.getText());
        }else{
            ArrayList<String> newRecord = new ArrayList<>();
            newRecord.add("1" + textArea.getText());
            Content.privateChatRecord.put(privateUser, newRecord);
        }
        addPrivateSelf(privateUserID, new Date(), textArea.getText());
        textArea.clear();
    }

    @FXML
    public void sendFile(){
       //TODO send file
   }

    @FXML
    public void videoSound(){
        //TODO video
    }
    
    public void addPrivate(String fromName, int from, int to, Date date, String body){
        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,0,5,10));
        Text text=new Text(body);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(255,255,255);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);
        private_info_vb.getChildren().add(hbox);
    }

    public void addPrivateSelf(int to, Date date, String body){
        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5,0,5,10));
        Text text=new Text(body);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(15,125,242);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);
        private_info_vb.getChildren().add(hbox);
    }
}
