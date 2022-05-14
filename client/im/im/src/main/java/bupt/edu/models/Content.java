package bupt.edu.models;

import java.net.InetAddress;
import java.util.ArrayList;


public class Content {
    public static String userID;
    public static InetAddress braddress;
    public static String privateUser;
    public static ArrayList<PrivateChatUser> privateChatUserList = new ArrayList<>();
    public static ArrayList<String> userinfo = new ArrayList<>();
    public static ArrayList<String> msg = new ArrayList<>();
    

    public static ArrayList<String> getUsers(){
        return userinfo;
    }

    public static ArrayList<String> getMsg(){
        return msg;
    }
}
