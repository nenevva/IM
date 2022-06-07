package GUI.Controller;


import Client.Client;
import GUI.Model.Content;
import GUI.Model.StageManager;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.FileInputStream;


public class LoginController{
    @FXML
    private Button b1;
    @FXML
    private Button b2;
    @FXML
    private TextField userid;
    @FXML
    private TextField passwd;

    private static LoginController instance;

    public LoginController() {
        instance = this;
    }

    //返回当前实例，方便ClientReceiveThread调用
    public static LoginController getInstance(){
        return instance;
    }

    @FXML 
    public void onButtonclick() throws Exception{
        Content.client=new Client("localhost",1234);
        String userName=userid.getText();
        String password=passwd.getText();
        Content.userName=userName;
        Content.client.login(userName,password);
        //ClientReceiveThread中收到登录失败的消息时会将userName设为""
    }

    @FXML
    public void signIn(){
        Content.client=new Client("localhost",1234);
        String userName=userid.getText();
        String password=passwd.getText();
        Content.userName=userName;
        Content.client.register(userName,password);
    }

    public void changeMain(){
        try {
            Stage primaryStage = new Stage();
            SplitPane root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("tomato-im");
            primaryStage.show();
            StageManager.STAGE.get("Login").hide();
            StageManager.STAGE.put("Main", primaryStage);
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                @Override
                public void handle(WindowEvent e){
                    System.out.println("退出登录");
                    Content.client.logout();
                    Content.client.closeConnect();
                    StageManager.STAGE.remove("Main");
                    changeLogin();
                }
            });
            
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void changeLogin(){
        userid.clear();
        passwd.clear();
        StageManager.STAGE.get("Login").show();
    }

    public void showVideo(String name){
        try {
            Stage stage = new Stage();
            VBox root = FXMLLoader.load(getClass().getClassLoader().getResource("video.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(name + "视频通话");
            stage.show();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                @Override
                public void handle(WindowEvent e){
                    System.out.println("关闭视频通话");
                    Content.isVideo=false;
                    Content.isVoice=false;
                }
            });

        } catch(Exception e) {
            e.printStackTrace();
        }

    }


}
