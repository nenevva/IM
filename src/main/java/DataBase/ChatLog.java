package DataBase;

import java.util.Date;

public class ChatLog {
    int senderID;
    int receiverID;
    Date date;
    String body;

    public ChatLog(int senderID, int receiverID, Date date, String body) {
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.date = date;
        this.body = body;
    }

    public int getSenderID() {
        return senderID;
    }

    public void setSenderID(int senderID) {
        this.senderID = senderID;
    }

    public int getReceiverID() {
        return receiverID;
    }

    public void setReceiverID(int receiverID) {
        this.receiverID = receiverID;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ChatLog{" +
                "senderID=" + senderID +
                ", receiverID=" + receiverID +
                ", date=" + date +
                ", body='" + body + '\'' +
                '}'+"\n";
    }
}
