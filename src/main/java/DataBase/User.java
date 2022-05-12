package DataBase;

import java.sql.*;
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

    public static boolean userCreate(User user) throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("INSERT INTO user VALUES(?, ?, ?, ?)");
        ps.setInt(1, user.id);
        ps.setString(2, user.password);
        ps.setString(3, user.username);
        ps.setTimestamp(4, user.createTime);
        return ps.execute();
    }

    public static boolean userValidate(int id, String password) throws SQLException {
        Connection conn = JDBC.getConnection();
        PreparedStatement ps = conn.prepareStatement("SELECT password FROM user WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next())
        {
            String correctPw = rs.getString(1);
            return correctPw.equals(password);
        }
        else return false;
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
