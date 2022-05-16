package DataBase;

import java.util.ArrayList;

//方便gson解析
public class ChatLogList {
    ArrayList<ChatLog> chatLogs;

    public ChatLogList(ArrayList<ChatLog> chatLogs) {
        this.chatLogs = chatLogs;
    }

    public ArrayList<ChatLog> getChatLogs() {
        return chatLogs;
    }

    public void setChatLogs(ArrayList<ChatLog> chatLogs) {
        this.chatLogs = chatLogs;
    }
}
