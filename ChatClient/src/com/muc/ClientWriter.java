package com.muc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class ClientWriter extends Thread {
    private Socket serverSocket;
    private OutputStream outputStream;
    private BufferedReader brKey;
    private String keyboard_input;


    public ClientWriter(Socket serversocket) throws IOException {
        this.serverSocket = serversocket;
        outputStream = this.serverSocket.getOutputStream();
        this.brKey = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        try {
            while (true){
                keyboard_input=brKey.readLine();
                if (keyboard_input.equalsIgnoreCase("quit")){
                    serverSocket.close();
                }else if (keyboard_input!=null){
                    String send = keyboard_input+"\n";
                    outputStream.write(send.getBytes());
                    System.out.println("消息发送成功");
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
