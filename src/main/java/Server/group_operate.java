package Server;

import DataBase.chatting_contentDB;
import DataBase.groupDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class group_operate {

    /**
     * 这个函数通过群聊的id得到群聊的所有成员
     * */
    public static String getAllMembersById(String groupId){
        groupDB db=new groupDB();
        String sql="SELECT *FROM group_info";
        ResultSet rs=db.query(sql);
        try {
            while(rs.next()) {
                String group_id=rs.getString(1);
                String receiver_id=rs.getString(2);

                if(group_id.equals(groupId)){
                    String members = "";
                    for(int i = 0;i<receiver_id.length();i+=6)
                        members+=(" "+receiver_id.substring(i,i+6));
                    return "group id:"+groupId+" has members:"+members;
                }
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return "cannot find such group!";
    }

    /**这个函数利用members_id和newGroupId用来创建新的群聊
     */
    public static void storeGroupInfo(String members,String newGroupId){
        groupDB db=new groupDB();
        String sql="INSERT INTO group_info VALUES ('"+newGroupId+"','"+members+"')";
        int res=db.update(sql);
        if(res==1) {
            System.out.println("新建群聊成功");
        }else {
            System.out.println("新建群聊失败");
        }
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Scanner get=new Scanner(System.in);
        System.out.println("try to get members by group id 001");
        String result = getAllMembersById("001");
        System.out.println(result);
        System.out.println("create new group ,new group id:003,members:123456 654321 ");
        storeGroupInfo("123456654321","003");
        get.close();
    }
}
