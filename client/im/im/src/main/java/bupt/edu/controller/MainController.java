package bupt.edu.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import bupt.edu.models.Content;
import bupt.edu.models.PrivateChatUser;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class MainController {
    private MulticastSocket socket = null; 
    private InetAddress braddress = null;
    private DatagramPacket inpack = new DatagramPacket(new byte[4096], 4096);
    private ServerSocket servrerSocket;
    @FXML
    private ListView<String> userList;
    @FXML
    private ListView<String> msgList;
    @FXML
    private TextArea textArea;
    @FXML
    public void initialize(){
        //receive broadcast msg
        try{
            socket = new MulticastSocket(8088);
            braddress = Content.braddress;
            socket.joinGroup(braddress);
            socket.setLoopbackMode(false); // 必须是false才能开启广播功能！！
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    receiveBrMsg();
                }
            }
        }).start();
          
        //receive private msg
        try{
            servrerSocket = new ServerSocket(8090);
        }catch(IOException e){
            e.printStackTrace();
        }
        new Thread(new Runnable() {
            @Override
            public void run(){
                while(true){
                    try{
                        Socket socket = servrerSocket.accept();
                        receivePrMsg(socket);
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }     
            }
        }).start();

        // UI update is run on the Application thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    refreshUserList();
                    refreshTextList();
                    
                }
            }
        }).start();

        //userList 监听是否打开私聊窗口
        userList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            //s：改变前的值    t1：改变后的值
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                try{
                    Stage stage = new Stage();
			Content.privateUser = t1;
                    VBox root = FXMLLoader.load(getClass().getClassLoader().getResource("private.fxml"));
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.setTitle(t1);
                    
                    stage.show();
                }catch(IOException e){
                    e.printStackTrace();
                }
                
            }
        });   
    }


    //刷新用户名单
    public void refreshUserList() {
        Platform.runLater(() -> {
            ObservableList<String> strList = FXCollections.observableArrayList(Content.getUsers());
            userList.setItems(strList);
        });
    }


    //刷新公聊信息
    public void refreshTextList() {
        Platform.runLater(() -> {
            ObservableList<String> strList = FXCollections.observableArrayList(Content.getMsg());
            msgList.setItems(strList);
        });
    }


    //接受公聊信息
    public void receiveBrMsg(){
        try{
            socket.receive(inpack);
        }catch (IOException e) {
            System.out.println("hello");
            e.printStackTrace();
            if (socket != null) {
                try {
                    socket.leaveGroup(braddress);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                socket.close();
            }
        }
        String msg = new String(inpack.getData(), 0, inpack.getLength());
        String instruction = msg.substring(0,2);
        String data = msg.substring(2);
        if(instruction.equals("10")){// 接受到服务器的用户表广播修改用户表
            ArrayList<String> newUserifo = new ArrayList<>();
            while(true){
                if(data.equals("/")){
                    break;
                }else{
                    String user = data.substring(0,data.indexOf("/"));
                    if(!user.equals(Content.userID))
                        newUserifo.add(user);
                    if(data.indexOf("/")+1 < data.length())
                        data = data.substring(data.indexOf("/")+1);
                    else
                        data = data.substring(data.indexOf("/"));
                }
            }
            Content.userinfo = newUserifo;
        }else if(instruction.equals("11")){//接收到其他成员的广播信息
            String sender = data.substring(0, data.indexOf(":"));
            if(!sender.equals(Content.userID))
                Content.msg.add(data);
        }
    }


    //接受私聊信息
    public void receivePrMsg(Socket socket){
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader bf = null;
        try{
            is  = socket.getInputStream();
            isr = new InputStreamReader(is,"UTF-8");
            bf = new BufferedReader(isr);
            String msg = bf.readLine();
            socket.close();
            String privateUser = msg.substring(0, msg.indexOf(":"));
            int symbol = 0;
            for(Iterator<PrivateChatUser> it = Content.privateChatUserList.iterator();it.hasNext();){
                PrivateChatUser privateChatUser = it.next();
                if(privateChatUser.userID.equals(privateUser)){
                    privateChatUser.msg.add(msg);
                    symbol = 1;
                    break;
                }
            }
            if(symbol == 0){
                PrivateChatUser newPrivateChatUser = new PrivateChatUser();
                newPrivateChatUser.userID = privateUser;
                newPrivateChatUser.msg.add(msg);
                Content.privateChatUserList.add(newPrivateChatUser);
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }


    @FXML
    public void sendFile(){
        //TODO send file by broadcast 

    }


    @FXML
    public void sendMsg() throws IOException{
        String msg = textArea.getText();
        textArea.setText("");
        Content.msg.add("我：" +msg);
        String sendMsg = "11" + Content.userID + ": "+msg;
        // send msg by broadcast 
        DatagramPacket outpack = new DatagramPacket(new byte[0], 0, braddress, 8088);
        outpack.setData(sendMsg.getBytes());
        socket.send(outpack);
    }

}
