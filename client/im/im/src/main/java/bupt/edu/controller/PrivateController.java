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
import java.util.Iterator;
import bupt.edu.models.Content;
import bupt.edu.models.PrivateChatUser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class PrivateController {
    private String privateUserID;
    private ArrayList<String> msg;
    private InetAddress ipaddress;
    @FXML
    private Button send;
    @FXML
    private Button file;
    @FXML
    private Button video;
    @FXML
    private TextArea textArea;
    @FXML
    private ListView<String> listView;
    @FXML
    public void initialize(){
        //初始化 
        // 私聊对象名
        privateUserID = Content.privateUser;
        //私聊信息
        int symbol = 0;
        for(Iterator<PrivateChatUser> it = Content.privateChatUserList.iterator();it.hasNext();){
            PrivateChatUser privateChatUser = it.next();
            if(privateChatUser.userID.equals(privateUserID)){
                msg = privateChatUser.msg;
                symbol = 1;
                break;
            }
        }
        if(symbol == 0){
            privateChatUser newchat = new PrivateChatUser();
            newchat.userID = privateUserID;
            msg = newchat.msg;
            Content.privateChatUserList.add(newchat); 
        }
        //对方ip
        try{
            Socket socket  = new Socket("localhost", 8081);
            OutputStream os = socket.getOutputStream();
            PrintWriter ps = new PrintWriter(os);
            ps.write("01" + privateUserID);
            ps.flush();
            socket.shutdownOutput();
            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String msg = br.readLine();
            String instruction = msg.substring(0,2);
            String data  = msg.substring(2);
            if(instruction.equals("01")){
                ipaddress = InetAddress.getByName(data);
            }
            socket.close();
        }catch(IOException e){
            e.printStackTrace();
        }
        
        //刷新消息
        new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    refreshTextList();
                }     
            }
        }).start();

    }


    //刷新消息
    public void refreshTextList(){
        Platform.runLater(()->{
            ObservableList<String> strlist = FXCollections.observableArrayList(msg);
            listView.setItems(strlist);
        });
    }


    //发送信息
    @FXML
    public void sendMsg() throws Exception{
        String msg = textArea.getText();
        this.msg.add(msg);
        //send msg 
        Socket socket = new Socket(ipaddress,8090);
        OutputStream os = socket.getOutputStream();
        PrintWriter pw = new PrintWriter(os);
        pw.write(Content.userID + ": " +msg);
        pw.flush();
        socket.close();
    }

    @FXML
    public void sendFile(){
        //TODO send file
    }

    @FXML
    public void videoSound(){
        //TODO video 
    }

}
