package bio;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static constant.Constant.ADDRESS;
import static constant.Constant.PORT;

/**
 * <h3>BIO: 客户端</h3>
 */
@Slf4j
public class BioRpcClient {

    public static void main(String[] args) {
        Socket client = null;
        try {
            // 1. 建立连接
           client = new Socket(ADDRESS, PORT);
           // 2. 发送内容
           client.getOutputStream().write("Hello BIO Socket RPC".getBytes(StandardCharsets.UTF_8));
           // 3. 记得刷新缓冲区
           client.getOutputStream().flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(client);
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
