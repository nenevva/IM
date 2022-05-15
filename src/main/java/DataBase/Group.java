package DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Group {
    private int groupID;
    private ArrayList<Integer> memberID;
    private String groupName;
    private int ownerID;

    public Group() {
    }

    public Group(int groupID, ArrayList<Integer> memberID) {
        this.groupID = groupID;
        this.memberID = memberID;
    }

    public Group(int groupID) {
        this.groupID = groupID;
        this.memberID=new ArrayList<>();
    }

    public static ArrayList<Integer> getAllMembers(int groupID){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM group_member WHERE group_id = ?");
            ps.setInt(1, groupID);
            ResultSet rs = ps.executeQuery();
            ArrayList<Integer> memberID=new ArrayList<>();
            while(rs.next()) {
                int receiver_id=rs.getInt("member_id");
                memberID.add(receiver_id);
            }
            return memberID;
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int addMember(int groupID,int memberID){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO group_member VALUES(?, ?)");
            ps.setInt(1,groupID);
            ps.setInt(2, memberID);
            return ps.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int deleteMember(int groupID,int memberID){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("DELETE FROM group_member WHERE group_id=? AND member_id=?");
            ps.setInt(1,groupID);
            ps.setInt(2, memberID);
            return ps.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int createGroup(int ownerID,String groupName){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO group_info VALUES(?, ?, ?,?)");
            ps.setInt(1, getNewID());
            ps.setString(2, groupName);
            ps.setInt(3, ownerID);
            ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            return ps.executeUpdate();
        }catch(SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int deleteGroup(int groupID){
        return 0;
    }

    public static ArrayList<String> getGroupInfo(int groupID){
        try {
            Connection conn = JDBC.getConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM group_info WHERE group_id = ?");
            ps.setInt(1, groupID);
            ResultSet rs = ps.executeQuery();
            ArrayList<String> res=new ArrayList<>();
            while(rs.next()) {
                res.add(String.valueOf(rs.getInt("owner_id")));
                res.add(rs.getString("group_name"));
                res.add(String.valueOf(rs.getTimestamp("create_time")));
            }
            return res;
        }catch(SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int getNewID() throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT group_id FROM group_info ORDER BY group_id desc;");
        ResultSet rs = ps.executeQuery();
        int id=1000;
        if(rs.next()){
            id=rs.getInt("group_id")+1;
        }
        return id;
    }

}
