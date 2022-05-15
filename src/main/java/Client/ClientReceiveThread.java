package Client;

import DataBase.Message;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class ClientReceiveThread implements Runnable {

    private Client client;
    public Socket socket;
    private Gson gson = new Gson();

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
                String[] data;
                PrintWriter writer;
                SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
                String date=dateFormat.format(message.getTime());
                switch (message.getType()) {
                    case SUCCESS:
                        client.setId(message.getTo());
                        System.out.println(date+" "+"System:"+body);
                        break;
                    case GROUP_MSG:
                        System.out.println(date+" "+message.getFrom()+":"+body);
                        break;
                    case PRIVATE_MSG:
                        System.out.println(date+" "+message.getFrom()+"(私):"+body);
                        break;
                    case FAIL:
                        System.out.println(date+" "+"System:"+body);
                        break;
                    case USER_LIST:
                        parseUserList(body);
                        break;
                    case USER_NAME_LIST:
                        parseUserNameList(body);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseUserList(String body){
        String[] data=body.split(";");
        HashMap<String, Integer> userList = new HashMap();
        for (int i = 0; i < data.length; i++) {
            String[] user=data[i].split(":");
            userList.put(user[1], Integer.valueOf(user[0]));
        }
        System.out.println(userList);
    }

    private void parseUserNameList(String body){
        String[] data=body.split(";");
        for (int i = 0; i < data.length; i++) {
            System.out.println(data[i].split(":")[0]+"   "+data[i].split(":")[1]);
        }
    }
}