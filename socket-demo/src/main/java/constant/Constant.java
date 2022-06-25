package constant;

/**
 * <h3>常量</h3>
 */
public class Constant {

    // 线程数量
    public static final int THREAD_COUNT = 10;
    // 端口号: 建议不要使用 8080, 可能会有本地的进程占用, 导致客户端连接不到服务器
    public static final int PORT = 4396;
    // IP 地址
    public static final String ADDRESS = "127.0.0.1";
    // 缓冲区大小
    public static final int BUFFER_SIZE = 1024;
}
