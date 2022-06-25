package org.nep.rpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcMessageCache;
import org.nep.rpc.framework.core.common.constant.ClientConfigConstant;
import org.nep.rpc.framework.core.common.constant.ProtocolConstant;
import org.nep.rpc.framework.core.handler.NeptuneRpcClientHandler;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.protocal.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocal.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxyFactory;


import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.nep.rpc.framework.core.common.constant.CommonConstant.DEFAULT_SERVER_ADDRESS;
import static org.nep.rpc.framework.core.common.constant.CommonConstant.DEFAULT_SERVER_PORT;

@Slf4j
public class NeptuneRpcClient {

    private final int port;

    private final String address;

    private EventLoopGroup worker;
    private NeptuneRpcReference reference;

    public NeptuneRpcClient(){
        this(DEFAULT_SERVER_PORT, DEFAULT_SERVER_ADDRESS);
    }

    public NeptuneRpcClient(int port, String address) {
        this.port = port;
        this.address = address;
    }

    /**
     * <h3>启动客户端</h3>
     */
    public void startNeptune(){
        // 1. 初始化线程池
        Bootstrap client = new Bootstrap();
        worker = new NioEventLoopGroup(ClientConfigConstant.WORKER_THREAD_COUNT);
        try {
            // 2. 配置参数
            client.group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new NeptuneRpcFrameDecoder());
                            channel.pipeline().addLast(new NeptuneRpcEncoder());
                            channel.pipeline().addLast(new NeptuneRpcDecoder());
                            channel.pipeline().addLast(new NeptuneRpcClientHandler());
                        }
                    });
            // 3. 启动客户端
            ChannelFuture future = client.connect(new InetSocketAddress(address, port)).sync();
            // 4. 开启异步线程发送数据
            asyncSend(future);
            // 5. 初始化调用者
            this.reference = new NeptuneRpcReference(new JdkDynamicProxyFactory());
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Client]: 客户端启动异常", e);
        }
    }

    public void closeNeptune(){
        worker.shutdownGracefully();
    }

    /**
     * <h3>启动线程异步发送消息</h3>
     */
    private void asyncSend(ChannelFuture future){
        new Thread(new AsyncSendTask(future), "async-send-task-thread").start();
        log.debug("[Neptune RPC Client]: 客户端异步线程启动");
    }

    public NeptuneRpcReference getReference(){
        return this.reference;
    }

    private static final class AsyncSendTask implements Runnable{

        private final ChannelFuture future;

        public AsyncSendTask(ChannelFuture future) {
            this.future = future;
        }

        @Override
        public void run() {
            while (true){
                try {
                    // 1. 从阻塞队列中获取消息
                    NeptuneRpcInvocation invocation = NeptuneRpcMessageCache.SEND_MESSAGE_QUEUE.take();
                    log.debug("[Neptune RPC Client]: 异步线程获取到消息 {}", invocation);
                    // 2. 序列化: 暂时采用 json
                    NeptuneRpcProtocol message =
                            new NeptuneRpcProtocol(JSON.toJSONString(invocation).getBytes(StandardCharsets.UTF_8));
                    log.debug("[Neptune RPC Client]: {}", message);
                    // 3. 发送给服务端
                    future.channel().writeAndFlush(message);
                } catch (InterruptedException e) {
                    log.error("[Neptune RPC Client]: 异步线程发送消息异常", e);
                }
            }
        }
    }

}
