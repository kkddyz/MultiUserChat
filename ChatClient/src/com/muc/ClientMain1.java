package com.muc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

public class ClientMain1 {
    public static void main(String[] args) throws IOException {
        System.out.println("��������server.....");
        try {
            Socket serversocket = new Socket(InetAddress.getByName("127.0.0.1"), 8808);
            System.out.println("�ɹ�����server!!");
            Client client1 = new Client(serversocket);
            client1.start();
        }catch(SocketException e){
            System.out.println("����ʧ��");
        }



    }
}
