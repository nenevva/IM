package DataBase;

import java.util.Date;

public class Message {
    private MessageType type;
    private int from;
    private int to;
    private Date time;
    private String body;

    public Message(MessageType type, int from, int to, Date time, String body) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.time = time;
        this.body = body;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
