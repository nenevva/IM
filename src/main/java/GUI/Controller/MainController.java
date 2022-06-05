package GUI.Controller;

import GUI.Model.Content;
import GUI.Model.StageManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.Date;


public class MainController {
    public static MainController instance;

    private int groupID=1;

    @FXML
    private ListView<String> userList;
    @FXML
    private ListView<String> msgList;
    @FXML
    private TextArea textArea;

    @FXML
    private VBox main_info_vb;

    public MainController() {
        instance = this;
    }

    //返回当前实例，方便ClientReceiveThread调用
    public static MainController getInstance(){
        return instance;
    }

    @FXML
    public void initialize(){
        // UI update is run on the Application thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    if(!Content.client.isClose())
                        updateUserList();

                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if(!Content.client.isClose())
                            updateIDRecord();
                        Thread.sleep(100000);
                    } catch (InterruptedException ex) {
                    }

                }
            }
        }).start();

//        //userList 监听是否打开私聊窗口
        userList.setOnMouseClicked((event) -> {
			String select = userList.getFocusModel().getFocusedItem();
            try{
                Stage stage = new Stage();
                Content.foucedPrivateUser = select;
                VBox root = FXMLLoader.load(getClass().getClassLoader().getResource("private.fxml"));
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle(select);
                StageManager.STAGE.put(select, stage);
                stage.show();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                    @Override
                    public void handle(WindowEvent e){
                        Integer selectID = Content.userList.get(select); 
                        Content.privateChatWindows.remove(selectID);
                        StageManager.STAGE.remove(select);
                    }
                });
            }catch(IOException e){
                e.printStackTrace();
            }

		});
    }


    //刷新用户名单
    public void refreshUserList() {
        Platform.runLater(() -> {
            ObservableList<String> strList = FXCollections.observableArrayList(Content.userList.keySet());
            userList.setItems(strList);
        });
    }



    @FXML
    public void sendFile(){
        //打开文件资源管理器，选择文件，传入绝对路径
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(StageManager.STAGE.get("Main"));
        String filename = file.getName();
        addGroupMsgSelf(1,new Date(), filename);
        Content.client.sendFileGroup(file.getPath(), groupID);
    }


    @FXML
    public void sendMsg() throws IOException{
        Content.client.sendGroupMsg(1,textArea.getText());
        addGroupMsgSelf(1,new Date(),textArea.getText());
        textArea.clear();
    }

    public void addGroupMsg(int from, int to, Date date, String body){
        HBox hbox_name=new HBox();
        hbox_name.setAlignment(Pos.CENTER_LEFT);
        hbox_name.setPadding(new Insets(5,0,5,0));
        TextFlow name=new TextFlow(new Text(Content.idNameRecord.get(from)));
        name.setStyle(
                "-fx-color:rgb(239,240,255);"
        );
        name.setPadding(new Insets(5,10,5,10));
        hbox_name.getChildren().add(name);

        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,20,5,20));
        Text text=new Text(body);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(255,255,255);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);

        main_info_vb.getChildren().add(hbox_name);
        main_info_vb.getChildren().add(hbox);
    }

    //TODO 放头像
    public void addGroupMsgSelf(int to, Date date, String body){
        HBox hbox_name=new HBox();
        hbox_name.setAlignment(Pos.CENTER_RIGHT);
        hbox_name.setPadding(new Insets(5,0,5,10));
        TextFlow name=new TextFlow(new Text(Content.userName));
        name.setStyle(
                "-fx-color:rgb(239,240,255);"
        );
        name.setPadding(new Insets(5,10,5,10));
        hbox_name.getChildren().add(name);

        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5,20,5,10));
        Text text=new Text(body);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(173,209,248);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);

        main_info_vb.getChildren().add(hbox_name);
        main_info_vb.getChildren().add(hbox);
    }

    public void updateUserList(){
        Content.client.getUserList();
        refreshUserList();
    }

    public void updateIDRecord(){
        Content.client.getUserNameList();
    }

    public void addGroupFile(int from, int to, long fileLength, String filename){
        HBox hbox_name=new HBox();
        hbox_name.setAlignment(Pos.CENTER_LEFT);
        hbox_name.setPadding(new Insets(5,0,5,0));
        TextFlow name=new TextFlow(new Text(Content.idNameRecord.get(from)));
        name.setStyle(
                "-fx-color:rgb(239,240,255);"
        );
        name.setPadding(new Insets(5,10,5,10));
        hbox_name.getChildren().add(name);

        HBox hbox=new HBox();
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPadding(new Insets(5,20,5,20));
        Text text=new Text(filename);
        TextFlow textFlow=new TextFlow(text);
        textFlow.setStyle(
                "-fx-color:rgb(239,240,255);"
                        + "-fx-background-color:rgb(255,255,255);"
                        +"-fx-background-radius: 20px"
        );
        textFlow.setOnMouseClicked((event)->{
            Content.client.receiveFileGroup(filename, fileLength, from, to);
        });
        textFlow.setPadding(new Insets(5,10,5,10));
        hbox.getChildren().add(textFlow);

        main_info_vb.getChildren().add(hbox_name);
        main_info_vb.getChildren().add(hbox);
    }
}