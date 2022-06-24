package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import static constant.Constant.*;

/**
 * <h3>NIO: 服务器</h3>
 */
@Slf4j
public class NioRpcServer {

    private ServerSocketChannel server;

    private Selector selector;

    private SelectionKey selectionKey;

    public void acceptHandler(SelectionKey key) throws IOException {
        // 1. 获取到对这个事件感兴趣的对象, 连接事件肯定对应的是服务器
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        // 2. 建立连接
        SocketChannel client = channel.accept();
        // 3. 将客户端注册到选择器中
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ, null);
        log.info("客户端连接成功");
        client.write(ByteBuffer.wrap("[response]: success".getBytes(StandardCharsets.UTF_8)));
    }

    public void readHandler(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        // 1. 开始读取
        if (channel.read(buffer) > 0){
            buffer.flip();
            // 1.1 填充字节数组
            byte[] bytes = new byte[buffer.limit()];
            buffer.get(bytes);
            // 1.2 向客户端响应消息
            log.info("[request]: {}", new String(bytes));
            channel.write(ByteBuffer.wrap("[response]: success".getBytes(StandardCharsets.UTF_8)));
        }
    }

    public void init() throws IOException {
        selector = Selector.open();
        // 1. 创建服务器对象
        server = ServerSocketChannel.open();
        // 2. 绑定端口号, 采用同步非阻塞模式
        server.bind(new InetSocketAddress(ADDRESS, PORT));
        server.configureBlocking(false);
        // 3. 将服务器注册到选择器中, 添加感兴趣的事件, 缓冲区
        selectionKey = server.register(selector, SelectionKey.OP_ACCEPT, null);
    }

    public void start(){
        try {
            // 1. 初始化参数
            init();
            log.debug("服务器启动成功");
            // 2. 开始执行
            while (true){
                // 2.1 阻塞服务器
                int keyCount = selector.select();
                if (keyCount > 0){
                    // 2.2 获取当前发生的所有事件
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    // 2.3 获取迭代器
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()){
                        SelectionKey key = iterator.next();
                        // 2.4 分别处理每个事件
                        if (key.isAcceptable()){
                            acceptHandler(key);
                        }else if (key.isReadable()){
                            readHandler(key);
                        }else{
                            log.info("暂时不提供处理方法");
                        }
                        // 2.5 移除事件
                        iterator.remove();
                    }
                }
            }
        } catch (IOException e) {
            log.error("服务器出现异常", e);
        } finally {
            close(server);
            close(selector);
        }
    }

    public void close(Closeable closeable){
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            log.error("服务器关闭失败", e);
        }
    }

    public static void main(String[] args) {
        new NioRpcServer().start();
    }
}
