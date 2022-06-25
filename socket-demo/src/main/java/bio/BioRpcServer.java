package bio;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static constant.Constant.*;

/**
 * <h3>BIO: 服务器</h3>
 */
@Slf4j
public class BioRpcServer {

    // 线程池: 负责处理客户端请求
    private final static ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(THREAD_COUNT);

    // 服务器启动: 简单写写, 就不封装成类
    public static void main(String[] args){
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT);
            log.info("服务器启动成功");
            while (true){
                log.debug("等待客户端连接");
                Socket client = server.accept();
                log.info("客户端建立连接: {}", client);
                EXECUTOR_SERVICE.submit(()-> handler(client));
            }
        }catch (IOException e) {
            log.error("服务器启动错误", e);
            throw new RuntimeException(e);
        } finally {
            close(server);
        }

    }

    public static void handler(Socket client){
        log.debug("异步线程开始处理客户端请求");
        int length = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        InputStream in = null;
        try {
            in = client.getInputStream();
            while ((length = in.read(buffer)) != -1){
                // 1. 处理客户端请求
                log.info("[request]: {}", new String(buffer, 0, length));
                // 2. 返回客户端响应
                client.getOutputStream().write("[response]: success".getBytes(StandardCharsets.UTF_8));
                client.getOutputStream().flush();
            }
        } catch (IOException e) {
            log.error("客户端请求处理异常", e);
        } finally {
            close(in);
            log.debug("异步线程处理客户端请求结束");
        }
    }

    public static void close(Closeable closeable){
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            log.error("服务器关闭失败", e);
        }
    }

}
