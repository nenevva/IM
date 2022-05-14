package bupt.edu.controller;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import bupt.edu.models.Content;
import bupt.edu.models.StageManager;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


public class LoginController {
    @FXML
    private Button b1;
    @FXML
    private Button b2;
    @FXML
    private TextField userid;
    @FXML
    private TextField passwd;
    
    @FXML 
    public void onButtonclick() throws Exception{
        // ToDo connect to Sever and sent 00(Login)
        Socket socket = new Socket("localhost", 8081);
        OutputStream os = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
        pw.write("00"+userid.getText());
        pw.flush();
        socket.shutdownOutput();
        InputStream is = socket.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        String msg = br.readLine();
        
        String instruction = msg.substring(0,2);
        String data = msg.substring(2);
        if(instruction.equals("00")){
            Content.braddress = InetAddress.getByName(data); 
            Content.userID = userid.getText();
        }
        socket.close();
        //change stage
        try {
            Stage primaryStage = new Stage();
            SplitPane root = FXMLLoader.load(getClass().getClassLoader().getResource("main.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("tomato-im");
            primaryStage.show();
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>(){
                @Override
                public void handle(WindowEvent e){
                    try{
                        Socket socket = new Socket("localhost", 8081);
                        OutputStream os = socket.getOutputStream();
                        PrintWriter pw = new PrintWriter(os);
                        pw.write("10");
                        pw.flush();
                        socket.close();
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                    Content.userID = null;
                    Content.braddress = null;
                    Content.userinfo = new ArrayList<>();
                    Content.msg = new ArrayList<>();
                    StageManager.STAGE.get("Login").show();
                }
            });
            StageManager.STAGE.get("Login").hide();
            StageManager.STAGE.put("Main", primaryStage);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
    }

    @FXML
    public void signIn(){
        //ToDo send sign in msg to server
    }
    
}
