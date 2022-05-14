package bupt.edu.models;

import java.util.ArrayList;

public class PrivateChatUser {
    public String userID;
    public ArrayList<String> msg;

    public PrivateChatUser(){
        msg = new ArrayList<>();
    }
}
