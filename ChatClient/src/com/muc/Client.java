package com.muc;

import java.io.IOException;
import java.net.Socket;

public class Client {

    private Socket serversocket;
    private ClientWriter writer;
    private ClientReader reader;

    Client(Socket serversocket) throws IOException {
        this.serversocket = serversocket;
        this.reader = new ClientReader(serversocket);
        this.writer = new ClientWriter(serversocket);
    }

    public void start(){
        writer.start();
        reader.start();
    }
}
