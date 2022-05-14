package imserver;
//记录当前登陆的用户
public class user {
    private String username;
    private String IP;

    user(String name, String ip){
        username = name;
        IP = ip;
    }

    public String getName(){
        return username;
    }
    public String getIP(){
        return IP;
    }
}
