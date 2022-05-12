package DataBase;

import java.sql.*;

public class JDBC {

    private String url = "jdbc:mysql://0.0.0.0:3306";
    private String driverName = "com.mysql.cj.jdbc.Driver";
    private String username = "root";
    private String password = "55611224";

    private static Connection conn = null;

    public JDBC() throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn = DriverManager.getConnection(url, username, password);
    }

    public static Connection getConnection() {
        return conn;
    }

    public void closeConnection() throws SQLException {
        conn.close();
    }
}
