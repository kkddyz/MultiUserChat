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
            System.out.println("连接断开" + clientSocket);
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
        /**-------------s-----------------------------------------------------------------------------------------------*//**----------------- 以上代码不需要改动 --------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/
        /**------------------------------------------------------------------------------------------------------------*/


        /**-------------------------- 核心代码-----------------------------------------------------------------------*/

        while ((line = reader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            /**提取cmd*/
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                /**处理cmd 基本分为 offline 和 online 两种情况*/
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
    /**----------------------------   以下代码为函数           -------------------------------------------------------*/
    /**---------------------------    不需要太关心          ---------------------------------------------------------*/
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
        /**cmd有效*/
        if (tokens.length == 3) {
            List<ServerWorker> workerList = server.getWorkerList();
            /**判断msg类型*/
            Boolean isTopic = (tokens[1].charAt(0) == '#');

            /**处理群发消息*/
            if (isTopic) {
                String topic = tokens[1];
                String text = tokens[2];
                String msg = "msg" + " " + topic + " " + login + ": " + text + "\n";
                /**检查每个用户是否属于topic*/
                for (ServerWorker worker : workerList) {
                    /**如果是send*/
                    if (!this.login.equals(worker.login)) {
                        if (worker.isMemberOfTopic(topic)) {
                            worker.send(msg);
                        }
                    }
                }
                System.out.println("向" + topic + "发送完毕");
                /**处理单发消息*/
            } else {
                Boolean haveSend = false;
                String msg = this.login + ": " + tokens[2] + "\n";
                String userTo = tokens[1];
                /**查找用户如果有*/
                for (ServerWorker worker : workerList) {
                    /**不是自己*/
                    if (userTo.equalsIgnoreCase(worker.login)) {
                        if (!this.login.equals(worker.login)) {
                            worker.send(msg);
                            haveSend = true;
                            break;
                        }

                    }
                }
                /**查找用户没找到*/
                if (haveSend == false) {
                    outputStream.write(("send to " + userTo + " failed\n").getBytes());
                }
            }
            /**cmd无效*/
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
                        // 假设GCY 已经 login ;GCY login 之后 在这里 会发现 相同 当作自己 不会发送onlineMsg
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for (ServerWorker worker : workerList) {
                    /**onlineMsg不会发给自己*/
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
        // 如果在在线发送信息
        if (login != null) {
            outputStream.write(msg.getBytes());
        }
    }
}
