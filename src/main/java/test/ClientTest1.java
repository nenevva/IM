package test;


import Client.Client;
import DataBase.MessageType;

import java.util.Scanner;

public class ClientTest1 {
    public static void main(String[] args){
        Client client=new Client("localhost",1234);
        client.sendMsg(MessageType.LOGIN,0,"xyx;123456");
        String input;
        Scanner in=new Scanner(System.in);
        while(!(input=in.nextLine()).isEmpty()) {
            client.sendMsg(MessageType.PRIVATE_MSG, 2, input);
        }
    }
}
