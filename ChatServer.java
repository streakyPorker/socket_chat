

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChatServer {

    public static final int LISTEN_PORT = 2908; // 服务端的端口


    static int idCounter = 1;
    private final Map<Integer, PrintWriter> clientWriter = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> msgQueue = new ConcurrentLinkedQueue<>(); // 使用非阻塞队列处理消息，暂时默认最大
    ServerSocket ss;

    ChatServer() throws IOException {
        ss = new ServerSocket(LISTEN_PORT);
        try {
            PrintWriter fileBackUp = new PrintWriter(new FileOutputStream("backup.txt"), true);
            clientWriter.put(0, fileBackUp); // 将指向文件的输出流第一个加到Map中，这样所有的聊天记录会在客户端保存
        } catch (FileNotFoundException e) {
            System.err.println("Can`t backup the chatting records.");
        }

    }

    public void listen() {
        System.out.println("Server is listening at port " + LISTEN_PORT);
        new MsgDispatcherThread().start(); // 开启监视信息的进程

        while (true) {
            Socket socket;
            try {
                socket = ss.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            System.out.println("Client " + idCounter + " comes in");
            PrintWriter writer;
            InputStream is; //可以把构建BufferedReader的任务放在子进程实现，降低主进程的工作量
            try {
                writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                is = socket.getInputStream();
                clientWriter.put(idCounter, writer); // 将writer加入map中，从而让dispatcher进程分发信息
                new ChatServeThread(is, idCounter).start(); // 一个Client给一个Thread处理输入
                idCounter++;
            } catch (IOException e) {
                e.printStackTrace(); //  构建writer时就断了
            }

        }
    }

    /**
     * 处理输入数据的进程
     */
    private class ChatServeThread extends Thread {
        private final BufferedReader in;
        private final Integer identifier;

        ChatServeThread(InputStream is, Integer identifier) {
            in = new BufferedReader(new InputStreamReader(is));
            this.identifier = identifier;
            clientWriter.get(identifier).println("ID=" + identifier); // 进行一次id赋值操作，让客户端感知到其id
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String line = in.readLine();
                    System.out.println("client " + identifier + " puts a msg");
                    msgQueue.add(line);
                } catch (IOException e) {
                    System.err.println("client end");
                    break;
                }
            }

            try {
                in.close(); // 关闭输入流
                clientWriter.remove(identifier).close();// 移出存储的客户端的记录,并关闭输出流
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("client " + identifier + " exit.");

            }

        }
    }

    /**
     * 将某个进程的输入进行分发的进程
     */
    private class MsgDispatcherThread extends Thread {
        @Override
        public void run() {
            while (true) {


                String msg = msgQueue.poll();
                if (msg == null) {
                    continue;
                }
                for (Integer key : clientWriter.keySet()) {
                    clientWriter.get(key).println(msg);// 便利和分发
                    // 这个分发过程中如果有连上的用户，就认为它没有接到这条消息
                }

            }
        }
    }

    public static void main(String[] args) {
        try {
            new ChatServer().listen();
        } catch (IOException e) {
            System.err.println("Can`t open the specified port.");
        }


    }


}

