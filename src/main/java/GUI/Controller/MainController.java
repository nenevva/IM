package GUI.Controller;

import GUI.Model.Content;
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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javafx.event.EventHandler;
import java.io.IOException;import java.net.*;
import java.util.Date;


public class MainController {
    public static MainController instance;
    
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
//        //receive broadcast msg
//        try{
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run(){
//                while(true){
//                    receiveBrMsg();
//                }
//            }
//        }).start();
//
//        //receive private msg
//        try{
//            servrerSocket = new ServerSocket(8090);
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//        new Thread(new Runnable() {
//            @Override
//            public void run(){
//                while(true){
//                    try{
//                        Socket socket = servrerSocket.accept();
//                        receivePrMsg(socket);
//                    }catch(IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }).start();
//
//        // UI update is run on the Application thread
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                    }
//                    refreshUserList();
//                    refreshTextList();
//
//                }
//            }
//        }).start();
//
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
                stage.show();
                stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                    @Override
                    public void handle(WindowEvent e){
                        Content.privateChatWindows.remove(select);
                    }
                });
            }catch(IOException e){
                e.printStackTrace();
            }

		});
    }


    //刷新用户名单
    public void refreshUserList() {
//        Platform.runLater(() -> {
//            ObservableList<String> strList = FXCollections.observableArrayList(Content.getUsers());
//            userList.setItems(strList);
//        });
    }


    //刷新公聊信息
//    public void refreshTextList() {
//        Platform.runLater(() -> {
//            ObservableList<String> strList = FXCollections.observableArrayList(Content.getMsg());
//            msgList.setItems(strList);
//        });
//    }


    //接受公聊信息
//    public void receiveBrMsg(){
//    }


    //接受私聊信息
//    public void receivePrMsg(Socket socket){
//        InputStream is = null;
//        InputStreamReader isr = null;
//        BufferedReader bf = null;
//        try{
//            is  = socket.getInputStream();
//            isr = new InputStreamReader(is,"UTF-8");
//            bf = new BufferedReader(isr);
//            String msg = bf.readLine();
//            socket.close();
//            String privateUser = msg.substring(0, msg.indexOf(":"));
//            int symbol = 0;
//            for(Iterator<PrivateChatUser> it = Content.privateChatUserList.iterator();it.hasNext();){
//                PrivateChatUser privateChatUser = it.next();
//                if(privateChatUser.userID.equals(privateUser)){
//                    privateChatUser.msg.add(msg);
//                    symbol = 1;
//                    break;
//                }
//            }
//            if(symbol == 0){
//                PrivateChatUser newPrivateChatUser = new PrivateChatUser();
//                newPrivateChatUser.userID = privateUser;
//                newPrivateChatUser.msg.add(msg);
//                Content.privateChatUserList.add(newPrivateChatUser);
//            }
//
//        }catch(IOException e){
//            e.printStackTrace();
//        }
//  }


    @FXML
    public void sendFile(){
        //TODO send file by broadcast

    }


    @FXML
    public void sendMsg() throws IOException{
        Content.client.sendGroupMsg(1,textArea.getText());
        addGroupMsgSelf(1,new Date(),textArea.getText());
        textArea.clear();
    }

    public void addGroupMsg(String fromName,int from, int to, Date date, String body){
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
        main_info_vb.getChildren().add(hbox);
    }

    public void addGroupMsgSelf(int to, Date date, String body){
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
        main_info_vb.getChildren().add(hbox);
    }

    public void updateUserList(){
        Content.client.getUserList();
    }

}
