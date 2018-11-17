package com.company;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Created by yihsiu on 2018/11/15.
 */
public class Client {

    String serverName;
    int port;//連接時監聽的port
    Socket socket;
    ObjectInputStream input;
    ObjectOutputStream output;

    public Client(String name, int p) {
        serverName = name;
        port = p;
        try {
            socket = new Socket(InetAddress.getByName(serverName), port);
            System.out.println("create client socket ok");
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("create output stream ok");
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("create input stream ok");

        } catch (UnknownHostException x) {
            x.printStackTrace();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    //exec方法
    public void exec() {
        System.out.println("三次內登入");
        Scanner scanner = new Scanner(System.in);
        //System.out.println("scanner=" + scanner); // server ready
        String command;//你下的指令

        do {


            //System.out.println(command);
            //System.out.flush();
            try {
                String message = (String)input.readObject();//我從Server讀入的訊息
                System.out.println(message);
                command = scanner.nextLine();//我輸入的指令

                output.writeObject(command);//我輸出到Server的指令
                output.flush();//推出去

            } catch (ClassNotFoundException x) {
                x.printStackTrace();
                break;
            } catch (IOException x) {
//                x.printStackTrace();
                System.out.println("close");
                break;
            }
        } while (!command.equals("quit") && !command.equals("exit"));

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Client client = new Client("localhost", 1236);
        client.exec();//public class Client 的 exec()方法
        //Scanner scanner = new Scanner(System.in);
        //String command = scanner.nextLine();
        //System.out.println(command);

    }
}
