package Client;

import DataBase.Message;
import com.google.gson.Gson;
import GUI.Controller.LoginController;
import GUI.Controller.MainController;
import GUI.Model.Content;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ClientReceiveThread implements Runnable {

    private Client client;
    public Socket socket;
    private Gson gson = new Gson();

    protected LoginController loginController = LoginController.getInstance();
    protected MainController mainController;

    public ClientReceiveThread(Client client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String str = reader.readLine();
                System.out.println("receive: "+str);
                Message message = gson.fromJson(str, Message.class);
                String body = message.getBody();
                SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
                String date=dateFormat.format(message.getTime());
                switch (message.getType()) {
                    //TODO 增加更多的MessageType
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
                        String fromName=body.substring(0,body.indexOf(';'));
                        String content=body.substring(body.indexOf(';')+1);
                        System.out.println(date+" "+fromName+":"+content);
                        Platform.runLater(() ->{
                            //TODO 在此处调用MainController中的函数，更新消息
                            mainController.addGroupMsg(fromName, message.getFrom(), message.getTo(), message.getTime(),content);
                        });
                        break;
                    case PRIVATE_MSG:
                        System.out.println(date+" "+message.getFrom()+"(私):"+body);
                        String fromName1 = body.substring(0, body.indexOf(';'));
                        String content1 = body.substring(body.indexOf(';')+1);
                        if(Content.privateChatRecord.containsKey(fromName1)){
                            Content.privateChatRecord.get(fromName1).add("0" + content1);
                        }else{
                            ArrayList<String> newRecord = new ArrayList<>();
                            newRecord.add("1" + content1);
                            Content.privateChatRecord.put(fromName1, newRecord);
                        }
                        Platform.runLater(() ->{
                            //TODO 在此处调用PrivateController中的函数，更新消息
                            if(Content.privateChatWindows.containsKey(fromName1)){
                                Content.privateChatWindows.get(fromName1).addPrivate(fromName1, message.getFrom(), message.getTo(), message.getTime(), content1);
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
                }
            }
        }
        catch (SocketException e){

        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void parseUserList(String body){
        String[] data=body.split(";");
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            if(user.length<2)
                break;
            Content.userList.put(user[1], Integer.valueOf(user[0]));
        }
        System.out.println(Content.userList);
    }

    private void parseUserNameList(String body){
        String[] data=body.split(";");
        for (int i = 0; i < data.length; i++) {
            //System.out.println(data[i].split(":")[0]+"   "+data[i].split(":")[1]);
        }
    }
}
