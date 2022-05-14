package Server;

import DataBase.chatting_contentDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class content_operate {

    /**
     * 这个函数通过发件人和收件人的id得到他们所有私聊所有的聊天信息和对应的聊天记录
     * */
    public static String getPrivateContentById(String senderId,String receiverId){
        chatting_contentDB db=new chatting_contentDB();
        String sql="SELECT *FROM chatting_content";
        ResultSet rs=db.query(sql);
        try {
            while(rs.next()) {
                String sender_id=rs.getString(1);
                String receiver_id=rs.getString(2);
                String chatting_content=rs.getString(3);
                String msg_date=rs.getString(4);
                String classification=rs.getString(5);

                if(sender_id.equals(senderId)&&receiver_id.equals(receiverId)&&classification.equals("private")){
                    //System.out.println("date is :"+msg_date+"chatting content is :"+chatting_content);
                    return "date is :"+msg_date+"chatting content is :"+chatting_content;
                }
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
        return "cannot find such content!";
    }

    /**
     * 通过发信人和收信人id来存储私聊的内容
     * */
    public static void storePrivateContentInfo(String senderId,String receiverId,String content,String date){
        chatting_contentDB db=new chatting_contentDB();
        String sql="INSERT INTO chatting_content VALUES ('"+senderId+ "','" +receiverId+"','"+content+"','"+date+"','private','null')";
        int res=db.update(sql);
        if(res==1) {
            System.out.println("插入私聊内容成功");
        }else {
            System.out.println("插入私聊内容失败");
        }
    }

    /*通过群id来查找公聊内容*/
    public static String getPublicContentByGroupId(String groupId){
        chatting_contentDB db=new chatting_contentDB();
        String sql="SELECT *FROM chatting_content";
        ResultSet rs=db.query(sql);
        String result ="";
        try {
            while(rs.next()) {
                String sender_id=rs.getString(1);
                String chatting_content=rs.getString(3);
                String msg_date=rs.getString(4);
                String classification=rs.getString(5);
                String group_id=rs.getString(6);

                if(classification.equals("public")&&groupId.equals(group_id)){
                    result += ("sender_id:"+sender_id+" chatting_content:"+chatting_content+" date:"+msg_date);
                }
            }
        }catch(SQLException e) {
            e.printStackTrace();
        }
        if(result.length()>0)
            return result;
        else
            return "cannot find such content!";
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Scanner get=new Scanner(System.in);
        System.out.println("try to get content and date by id 123456 and 654321");
        String result = getPrivateContentById("123456","654321");
        System.out.println(result);
        //storePrivateContentInfo("123456","654321","how do you do?","20220514185550");这个调用是对的
        System.out.println("try to get information by id group id: 001");
        System.out.println(getPublicContentByGroupId("001"));
        get.close();
    }


}
