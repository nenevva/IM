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
    public static HashMap<Integer, PrivateController> privateChatWindows = new HashMap<>();//打开的私聊窗口<id， controller>
    public static HashMap<Integer, ArrayList<String>> privateChatRecord = new HashMap<>();//私聊记录<id， 记录>
    public static HashMap<Integer,String> idNameRecord=new HashMap<>();//记录id与用户名的映射关系
}