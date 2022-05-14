package test;


import Client.Client;
import DataBase.MessageType;

import java.util.Scanner;

public class ClientTest2 {
    public static void main(String[] args){

        Client client=new Client("localhost",1234);
        client.login("xyx1234","123456");
        String input;
        Scanner in=new Scanner(System.in);
        while(!(input=in.nextLine()).isEmpty()) {
            client.sendGroupMsg(1,input);
        }
    }
}
