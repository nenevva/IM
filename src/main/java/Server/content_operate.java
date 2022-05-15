package Server;

import DataBase.ChatContent;

import java.sql.ResultSet;
import java.sql.SQLException;

public class content_operate {

    public static void main(String[] args) {
        // TODO Auto-generated method stub

        try {
            new DataBase.JDBC();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        ChatContent.saveMsg(1,2,"测试私发存储2",new Date(),0);
//        ChatContent.saveMsg(2,1,"测试私发回复2",new Date(),0);
        System.out.println(ChatContent.getPrivateChatLog(1,2));
//        Scanner get=new Scanner(System.in);
//        System.out.println("try to get content and date by id 123456 and 654321");
//        String result = getPrivateContentById("123456","654321");
//        System.out.println(result);
//        //storePrivateContentInfo("123456","654321","how do you do?","20220514185550");这个调用是对的
//        System.out.println("try to get information by id group id: 001");
//        System.out.println(getPublicContentByGroupId("001"));
//        get.close();
    }


}
