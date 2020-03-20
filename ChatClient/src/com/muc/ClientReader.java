package com.muc;

import javax.security.sasl.SaslException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class ClientReader extends Thread {
    private Socket serverSocket;
    private InputStream inputStream;
    private BufferedReader br;
    private String line;

    public ClientReader(Socket serversocket) throws IOException {
        this.serverSocket = serversocket;
        this.inputStream = serversocket.getInputStream();
        this.br = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        try {
            while (true){
                if ((line=br.readLine())!=null){
                    System.out.println(line);
                }
            }
        } catch (SocketException e){
            System.out.println("server Á¬½Ó¶Ï¿ª");
        }catch (IOException e) {
            e.printStackTrace();
        }



    }
}
