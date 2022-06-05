package GUI.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import GUI.Model.Content;
import GUI.Model.StageManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

public class PrivateController {
    private String privateUser;
    private int privateUserID;
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
        //打开文件资源管理器，选择文件，传入绝对路径
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(StageManager.STAGE.get(privateUser));
        String filename = file.getName();
        addPrivateSelf(privateUserID, new Date(), filename);
        Content.client.sendFilePrivate(file.getPath(), privateUserID);
   }

    @FXML
    public void videoSound(){
        //TODO
        Content.client.startVideoChat(privateUserID);
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

    public void addFile(int from, long fileLength, String filename){
        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,0,5,10));
        Text text=new Text(filename);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(255,255,255);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        text.setOnMouseClicked((event)->{
            Content.client.receiveFilePrivate(filename, fileLength, from);
        });
        hbox.getChildren().add(textFlow);
        private_info_vb.getChildren().add(hbox);
    }
}
