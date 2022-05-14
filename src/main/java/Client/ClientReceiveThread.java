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
                        Platform.runLater(() ->{
                            //TODO 在此处调用PrivateController中的函数，更新消息
                        });
                        break;
                    case FAIL:
                        System.out.println(date+" "+"System:"+body);
                        Content.userName="";
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
}
