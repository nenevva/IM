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

        //初始化
       // 私聊对象名
        privateUser = Content.foucedPrivateUser;
        privateUserID = Content.userList.get(privateUser);

        Content.privateChatWindows.put(privateUserID,this);
        if(Content.privateChatRecord.containsKey(privateUserID)){
            ArrayList<String> record = Content.privateChatRecord.get(privateUserID);
            for(Iterator<String> it = record.iterator(); it.hasNext();){
                String msg = it.next();
                String type = msg.substring(0,1);
                String body = msg.substring(1);
                if(type.equals("0")){
                    addPrivate(privateUserID, Content.client.getId(), new Date(), body);
                }
                if(type.equals("1")){
                    addPrivateSelf(Content.client.getId(),new Date(), body);
                }
            }
        }
    }
    //发送信息
    @FXML
    public void sendMsg() throws Exception{
        Content.client.sendPrivateMsg(privateUserID, textArea.getText());
        if(Content.privateChatRecord.containsKey(privateUserID)){
            Content.privateChatRecord.get(privateUserID).add("1" + textArea.getText());
        }else{
            ArrayList<String> newRecord = new ArrayList<>();
            newRecord.add("1" + textArea.getText());
            Content.privateChatRecord.put(privateUserID, newRecord);
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
    
    public void addPrivate(int from, int to, Date date, String body){
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
                        + "-fx-background-color:rgb(173,209,248);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);
        private_info_vb.getChildren().add(hbox);
    }
}
