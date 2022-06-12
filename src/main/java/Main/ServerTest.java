package Main;

import Server.Server;
import java.sql.SQLException;

public class ServerTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        if(args.length<1){
            System.out.println("use defalut port 7777");
            new Server(7777);
        }
        else{
            System.out.println("port is "+args[0]);
            new Server(Integer.parseInt(args[0]));
        }
    }
}
