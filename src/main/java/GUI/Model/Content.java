package GUI.Model;

import Client.Client;
import GUI.Controller.PrivateController;

import java.util.ArrayList;
import java.util.HashMap;


public class Content {
    public static Client client;
    public static String userName;
    public static int id=-1;
    //public static ArrayList<String> msg = new ArrayList<>();
    public static String foucedPrivateUser;
    public static HashMap<String, Integer> userList = new HashMap();//当前的用户列表<用户名， 用户ID>
    public static HashMap<String, PrivateController> privateChatWindows = new HashMap<>();//打开的私聊窗口<用户名， controller>
    public static HashMap<String, ArrayList<String>> privateChatRecord = new HashMap<>();//私聊记录<用户名， 记录>
}