package DataBase;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *用来进行群聊数据库操作
 */

public class groupDB {

    private Connection conn;
    private Statement stmt;

    public groupDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url="jdbc:mysql://101.43.157.247:3306/IM";
            this.conn=DriverManager.getConnection(url, "root", "55611224");

            if(!this.conn.isClosed())
                System.out.println("Database is connected!");

        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //增刪改
    public int update(String sql) {
        System.out.println("sql:"+sql);
        try {
            this.stmt=this.conn.createStatement();
            return this.stmt.executeUpdate(sql);		//返回 影响 了几行
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }


    //查询方法
    public ResultSet query(String sql) {
        //System.out.println("sql:"+sql);
        try {
            this.stmt=this.conn.createStatement();
            return this.stmt.executeQuery(sql);
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
