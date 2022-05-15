package DataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class User {

    private int id;
    private String password;
    private String username;
    private Timestamp createTime;

    public User(int id, String password, String username) {
        this.id = id;
        this.password = password;
        this.username = username;
        this.createTime = new Timestamp(System.currentTimeMillis());
    }

    public static void userCreate(User user) throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("INSERT INTO user VALUES(?, ?, ?, ?)");
        ps.setInt(1, user.id);
        ps.setString(2, user.password);
        ps.setString(3, user.username);
        ps.setTimestamp(4, user.createTime);
        ps.execute();
    }

    public static int userValidate(String username, String password) throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE username = ?");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
            String correctPw = rs.getString("password");
            System.out.println(correctPw);
            if (correctPw.equals(password)) {
                return rs.getInt("id");
            }
        }
        return -1;
    }

    public static ArrayList<String> getAllName() throws SQLException{
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM user");
        ResultSet rs = ps.executeQuery();
        ArrayList<String> res=new ArrayList<>();
        while (rs.next())
        {
            res.add(String.valueOf(rs.getInt("id")));
            res.add(rs.getString("username"));
        }
        return res;
    }

    public static int getNewID() throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM user;");
        ResultSet rs = ps.executeQuery();
        int i = 1;
        while (rs.next()) {
            i++;
        }
        return i;
    }

    public static boolean isUsernameAvailable(String username) throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM user WHERE username = ?;");
        ps.setString(1, username);
        ResultSet rs = ps.executeQuery();
        return !rs.next();
    }

    public int getUserID() {
        return id;
    }

    public void setUserID(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }
}
