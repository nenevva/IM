package DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class ChatContent {

    public static int saveMsg(int senderID, int receiverID, String body, Date date,int isPublic){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO chat_content VALUES(?, ?, ? , ? , ?)");
            ps.setInt(1, senderID);
            ps.setInt(2, receiverID);
            ps.setTimestamp(3, new Timestamp(date.getTime()));
            ps.setInt(4, isPublic);
            ps.setString(5,body);
            return ps.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static ArrayList<ChatLog> getGroupChatLog(int groupID){
        try {
            //TODO 分页查找
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM chat_content WHERE receiver_id = ? ORDER BY send_time desc");
            ps.setInt(1, groupID);
            ResultSet rs = ps.executeQuery();
            ArrayList<ChatLog> chatLogs=new ArrayList<>();
            while(rs.next()) {
                chatLogs.add(new ChatLog(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getTimestamp("send_time"),
                        rs.getString("content")
                ));
            }
            return chatLogs;
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<ChatLog> getPrivateChatLog(int ID1,int ID2){
        try {
            //TODO 分页查找
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM chat_content WHERE sender_id = ? AND receiver_id = ? ORDER BY send_time desc");
            ps.setInt(1, ID1);
            ps.setInt(2, ID2);
            ResultSet rs = ps.executeQuery();
            ArrayList<ChatLog> chatLogs=new ArrayList<>();
            while(rs.next()) {
                chatLogs.add(new ChatLog(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getTimestamp("send_time"),
                        rs.getString("content")
                ));
            }

            ps = conn.prepareStatement("SELECT * FROM chat_content WHERE sender_id = ? AND receiver_id = ? ORDER BY send_time desc");
            ps.setInt(1, ID2);
            ps.setInt(2, ID1);
            rs = ps.executeQuery();
            while(rs.next()) {
                chatLogs.add(new ChatLog(
                        rs.getInt("sender_id"),
                        rs.getInt("receiver_id"),
                        rs.getTimestamp("send_time"),
                        rs.getString("content")
                ));
            }
            //需要查找两次并对结果进行排序
            if(chatLogs!=null){
                chatLogs.sort(
                        new Comparator<ChatLog>() {
                            @Override
                            public int compare(ChatLog o1, ChatLog o2) {
                                return (int) (o2.getDate().getTime()-o1.getDate().getTime());
                            }
                        }
                );
            }
            return chatLogs;
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
