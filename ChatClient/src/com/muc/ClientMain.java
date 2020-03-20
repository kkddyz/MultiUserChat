package com.muc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        System.out.println("正在连接server.....");
        try {
            Socket client = new Socket(InetAddress.getByName("127.0.0.1"), 8808);
            System.out.println("成功连接server!!");
            Client client1 = new Client(client);
            client1.start();
        }catch(SocketException e){
            System.out.println("连接失败");
        }

    }
}
