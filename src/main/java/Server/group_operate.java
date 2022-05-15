package Server;

import DataBase.Group;

import java.sql.SQLException;
import java.util.ArrayList;

public class group_operate {

    public static void main(String[] args) {
        try {
            new DataBase.JDBC();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("try to get members by group id 001");
        ArrayList<Integer> members= Group.getAllMembers(1234);
        ArrayList<String> res=Group.getGroupInfo(1000);
        System.out.println(res);
    }
}
