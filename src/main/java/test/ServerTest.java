package test;

import Server.Server;

import java.sql.SQLException;

public class ServerTest {
    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new Server(1234);
    }
}
