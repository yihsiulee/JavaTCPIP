package com.company;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;


public class Server extends Thread {

    //狀態機
    enum State {
        LOGIN,
        ERRORPASS,
        INTERACTION,
        EXIT
    }

    //connection 用 server抓到的socket
    class Connection {
        Socket socket;
        ObjectInputStream inputStream;
        ObjectOutputStream outputStream;
        State state;
        int a = 0;

        //        一個client連進來 就是一個Connection
        public Connection(Socket s) {
            //抓到的
            socket = s;
            try {
                System.out.println("open input stream");
                inputStream = new ObjectInputStream(socket.getInputStream());
                System.out.println("open output stream");
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("open output stream ok");
                state = State.LOGIN;
            } catch (IOException x) {
                x.printStackTrace();
            }
        }

        public void runProcess() throws IOException, ClassNotFoundException {

            String inputText;
//            outputStream.writeObject("hihihilll");
//            outputStream.flush();
//            給state初始值
            try {
                switch (state) {
                    case LOGIN:
                        System.out.println(a);
                        outputStream.writeObject("請輸入password:");
                        outputStream.flush();
                        inputText = (String) inputStream.readObject();

                        if (a == 2) {
                            outputStream.writeObject("錯誤超過３次");
                            outputStream.flush();
                            System.out.println("錯誤超過３次");
                            state = State.ERRORPASS;
                            break;
                        } else {

                            if (inputText.equals("1234")) {
                                state = State.INTERACTION;
                            } else {
                                a++;
                                state = State.LOGIN;
                                outputStream.writeObject("密碼錯誤請再輸入一次");
                                outputStream.flush();
                            }
                        }

                        break;

                    case ERRORPASS:
//                        password = (String) inputStream.readObject();
                        System.out.println("123");
//                        outputStream.writeObject("錯誤超過３次");
//                        outputStream.flush();
                        state = State.EXIT;
//                斷線
                        break;
                    case INTERACTION:
                        System.out.println("success");
                        outputStream.writeObject("success 請輸入指令");
                        outputStream.flush();
                        inputText = (String) inputStream.readObject();
                        System.out.println(inputText);
                        try {
                            Process process = Runtime.getRuntime().exec(inputText);
                            BufferedReader reader =
                                    new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String s;
                            String all ="";


                            while ((s = reader.readLine()) != null) {
                                all =all.concat(s+"\n");
                            }
                            outputStream.writeObject(all+"\n");
                            outputStream.flush();
                        } catch (IOException x) {
                        }


                        break;
                    case EXIT:
//                        password = (String) inputStream.readObject();
                        close();
                        break;

                }
            } catch (SocketTimeoutException e) {

            }
        }

        public void close() {
            try {
                System.out.println("close connection...");
                inputStream.close();
                outputStream.close();
                socket.close();
                removeConnection(this);
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }


    int port;
    int maxConnections;
    ServerSocket serverSocket;


    public Server(int p, int c) {
        port = p;
        maxConnections = c;

        try {
            serverSocket = new ServerSocket(port, maxConnections);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    //    抓socket
    @Override
    public void run() {
        while (true) {
            try {
                // connection Socket
                System.out.println("wait for connections...");
                //抓socket
                Socket connSocket = serverSocket.accept();
                System.out.println("maxConnections=" + maxConnections);
                if (saveConnections.size() < maxConnections) {
                    System.out.println("create connection socket");
//                    丟到上面給connection
                    Connection connection = new Connection(connSocket);
//                    把抓到的socket存進去陣列 （之後要丟到狀態機）
                    saveConnections.add(connection);

                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }

    //  存放進來的client
    List<Connection> saveConnections = new LinkedList();

    //加client進陣列
    public void addConnection(Connection connection) {
        saveConnections.add(connection);
    }

    //移client出陣列
    public void removeConnection(Connection connection) {
        saveConnections.remove(connection);
    }

    // 重複跑迴圈
    public void service() throws IOException, InterruptedException, ClassNotFoundException {
        while (true) {
            for (Connection connection : saveConnections) {
                connection.runProcess();
            }
            Thread.sleep(10);
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
        Server server = new Server(1236, 100);
        server.start();
        server.service();
    }

}
