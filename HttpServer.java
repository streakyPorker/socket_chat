import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class HttpServer {
    public static final int LISTEN_PORT = 2908; // 服务端的端口
    private final Map<Integer, PrintWriter> clientWriter = new ConcurrentHashMap<>();
    ServerSocket ss;

    public HttpServer() throws IOException {
        ss = new ServerSocket(LISTEN_PORT);

    }

}
