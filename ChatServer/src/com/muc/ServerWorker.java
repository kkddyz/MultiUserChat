package com.muc;


import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread {

    private Server server;
    private Socket clientSocket;
    private OutputStream outputStream;
    private String login = null;
    private HashSet<String> topicsSet = new HashSet<>();

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (SocketException e) {
            System.out.println("���ӶϿ�" + clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        outputStream.write("welcome!\n".getBytes());
        /**------------------------------------------------------------------------------------------------------------*/
        /**-------------s-----------------------------------------------------------------------------------------------*//**----------------- ���ϴ��벻��Ҫ�Ķ� --------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/


        /**-------------------------- ���Ĵ���-----------------------------------------------------------------------*/

        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            /**��ȡcmd*/
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                /**����cmd ������Ϊ offline �� online �������*/
                if (this.login == null) {
                    if ("login".equalsIgnoreCase(cmd)) {
                        handleLogin(outputStream, tokens);
                    } else {
                        String msg = "unknown " + cmd + "\n";
                        outputStream.write(msg.getBytes());
                        System.out.println("cmd error!");
                    }
                } else {
                    if ("logoff".equals(cmd) || "quit".equalsIgnoreCase(cmd)) {
                        handleLogoff();
                        break;
                    } else if ("msg".equalsIgnoreCase(cmd)) {
                        String[] tokenMsg = StringUtils.split(line, null, 3);
                        handleMsg(tokenMsg);
                    } else if ("join".equalsIgnoreCase(cmd)) {
                        handleJoin(tokens);
                    } else if("leave".equalsIgnoreCase(cmd)){
                        handleLeave(tokens);
                    } else {
                        String msg = "unknown " + cmd + "\n";
                        outputStream.write(msg.getBytes());
                        System.out.println("cmd error!");
                    }
                }
            }
        }
        clientSocket.close();
    }



    /**------------------------------------------------------------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**----------------------------   ���´���Ϊ����           -------------------------------------------------------*/
    /**---------------------------    ����Ҫ̫����          ---------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**------------------------------------------------------------------------------------------------------------*/
    /**
     * ------------------------------------------------------------------------------------------------------------
     */

    public String getLogin() {
        return login;
    }

    public Boolean isMemberOfTopic(String topis) {
        return topicsSet.contains(topis);
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length == 2) {
            String topic = tokens[1];
            topicsSet.remove(topic);
            System.out.println(login + " leave " + topic);
        }
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length == 2) {
            String topic = tokens[1];
            topicsSet.add(topic);
            System.out.println(login + " join " + topic);
        }
    }

    //format msg userTo text
    //format msg #topic text
    private void handleMsg(String[] tokens) throws IOException {
        // tokens[0]="msg" tokens[1]=<user to>&&#topic tokens[2]="text"
        /**cmd��Ч*/
        if (tokens.length == 3) {
            List<ServerWorker> workerList = server.getWorkerList();
            /**�ж�msg����*/
            Boolean isTopic = (tokens[1].charAt(0) == '#');

            /**����Ⱥ����Ϣ*/
            if (isTopic) {
                String topic = tokens[1];
                String text = tokens[2];
                String msg = "msg" + " " + topic + " " + login + ": " + text + "\n";
                /**���ÿ���û��Ƿ�����topic*/
                for (ServerWorker worker : workerList) {
                    /**�����send*/
                    if (!this.login.equals(worker.login)) {
                        if (worker.isMemberOfTopic(topic)) {
                            worker.send(msg);
                        }
                    }
                }
                System.out.println("��" + topic + "�������");
                /**��������Ϣ*/
            } else {
                Boolean haveSend = false;
                String msg = this.login + ": " + tokens[2] + "\n";
                String userTo = tokens[1];
                /**�����û������*/
                for (ServerWorker worker : workerList) {
                    /**�����Լ�*/
                    if (userTo.equalsIgnoreCase(worker.login)) {
                        if (!this.login.equals(worker.login)) {
                            worker.send(msg);
                            haveSend = true;
                            break;
                        }

                    }
                }
                /**�����û�û�ҵ�*/
                if (haveSend == false) {
                    outputStream.write(("send to " + userTo + " failed\n").getBytes());
                }
            }
            /**cmd��Ч*/
        } else {
            outputStream.write("failed\n".getBytes());
            System.out.println("error");
        }
    }

    private void handleLogoff() throws IOException {
        server.removeworker(this);
        // get workerList
        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin())) {
                worker.send(onlineMsg);
            }
        }
        clientSocket.close();
    }


    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if ((login.equals("GCY") && password.equals("123456")) || (login.equals("CXY") && password.equals("123456"))) {
                String msg = "you login successfully!\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);

                List<ServerWorker> workerList = server.getWorkerList();

                // send current user all other online logins
                for (ServerWorker worker : workerList) {

                    if (worker.getLogin() != null) {
                        // ����GCY �Ѿ� login ;GCY login ֮�� ������ �ᷢ�� ��ͬ �����Լ� ���ᷢ��onlineMsg
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for (ServerWorker worker : workerList) {
                    /**onlineMsg���ᷢ���Լ�*/
                    if (!login.equals(worker.getLogin())) {
                        worker.send(onlineMsg);
                    }
                }
            } else {
                String msg = "error login\n";
                outputStream.write(msg.getBytes());
            }
        } else {
            outputStream.write("error login!\n".getBytes());
        }
    }

    private void send(String msg) throws IOException {
        // ��������߷�����Ϣ
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
