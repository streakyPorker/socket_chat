

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    static final int SERVER_PORT = 8081;
    static final String SERVER_IP = "127.0.0.1"; // 存储id，变动了也方便修改

    private Socket clientSocket; // 客户端的socket
    private Integer identifier;  // 客户端的标识符
    private boolean isSet = false; // 用于防止标识符被正常信息赋值

    // 创建客户端的时候应该能够抛出异常
    public ChatClient() throws IOException {
        clientSocket = new Socket(InetAddress.getByName(SERVER_IP), SERVER_PORT);
        new ClientInputThread(clientSocket.getInputStream()).start();
        new ClientOutputThread(clientSocket.getOutputStream()).start();
    }


    private class ClientInputThread extends Thread {
        private final BufferedReader in;

        ClientInputThread(InputStream is) {
            in = new BufferedReader(new InputStreamReader(is));
        }

        @Override
        public void run() {
            while (true) {
                String line = "";
                try {
                    line = in.readLine();
                    if (line.startsWith("ID=") && !isSet) { // 一开始传入信息给id赋值
                        identifier = Integer.parseInt(line.split("=")[1]);
                        isSet = true;
                    } else {
                        System.out.println(line);
                    }

                } catch (IOException e) {
                    System.err.println("Server has dead");
                    System.exit(0);
                    break;
                }

            }
        }
    }

    private class ClientOutputThread extends Thread {
        private final PrintWriter out;
        private final Scanner in;

        ClientOutputThread(OutputStream os) {
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(os)), true);
            in = new Scanner(System.in);
        }

        @Override
        public void run() {

            while (true) {
                String line = in.nextLine();
                if (line.length() != 0) {// 直接打回车不算输入
                    out.println("userId" + identifier + ":" + line);
                }

            }
        }
    }


    public static void main(String[] args) {
        try {
            new ChatClient();
        } catch (IOException e) {
            System.err.println("Can`t connect to server");
        }
    }
}







