package nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static constant.Constant.ADDRESS;
import static constant.Constant.PORT;

@Slf4j
public class NioRpcClient {

    public static void main(String[] args) throws IOException{
        SocketChannel client = SocketChannel.open();
        client.connect(new InetSocketAddress(ADDRESS, PORT));
        client.write(ByteBuffer.wrap("Hello".getBytes(StandardCharsets.UTF_8)));
        System.in.read();
    }
}
