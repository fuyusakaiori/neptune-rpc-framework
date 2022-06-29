package org.nep.rpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.constant.ClientConfigConstant;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.handler.NeptuneRpcClientHandler;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.protocal.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocal.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxyFactory;
import org.nep.rpc.framework.registry.service.RegistryService;
import org.nep.rpc.framework.registry.service.zookeeper.NeptuneZookeeperRegister;
import org.nep.rpc.framework.registry.url.DefaultURL;
import org.nep.rpc.framework.registry.url.URL;


import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.nep.rpc.framework.core.common.constant.CommonConstant.*;

@Slf4j
public class NeptuneRpcClient {
    private EventLoopGroup worker;

    private ChannelFuture future;

    private RegistryService registryService;

    private NeptuneRpcClientConfig config;
    private NeptuneRpcReference reference;

    private Bootstrap client;

    public NeptuneRpcClient(){
        this(PropertyBootStrap.loadClientConfiguration());
    }

    public NeptuneRpcClient(NeptuneRpcClientConfig config) {
        this.config = config;
    }

    /**
     * <h3>启动客户端</h3>
     */
    public void startNeptune(){
        if (registryService != null || client != null){
            log.info("[Neptune RPC Client]: 客户端已经启动了, 请不要重复启动");
            return;
        }
        // 0. 初始化注册中心
        registryService = new NeptuneZookeeperRegister(config.getRegisterConfig());
        // 1. 初始化线程池
        client = new Bootstrap();
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
            future = client.connect(new InetSocketAddress(config.getAddress(), config.getPort())).sync();
            // 4. 开启异步线程发送数据
            asyncSend();
            // 5. 初始化调用者
            this.reference = new NeptuneRpcReference(new JdkDynamicProxyFactory());
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Client]: 客户端启动异常", e);
        }
    }

    /**
     * <h3>订阅服务: 将自己添加到注册中心</h3>
     */
    private void subscribeService(Class<?> service){
        URL url = getUrl(service);
        registryService.subscribe(url);
        log.debug("[Neptune RPC Client]: 客户端注册服务");
    }

    private URL getUrl(Class<?> service){
        URL url = new DefaultURL();
        url.setPort(config.getPort());
        // TODO 应该直接在配置文件中配置好, 暂时用于测试使用
        url.setAddress(service.getName());
        url.setAddress(config.getAddress());
        url.setApplicationName(config.getApplication());
        return url;
    }

    /**
     * <h3>关闭客户端</h3>
     */
    public void closeNeptune(){
        try {
            future.channel().closeFuture().sync();
            worker.shutdownGracefully();
            log.info("[Neptune RPC Client]: 客户端关闭");
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Client]: 客户端关闭异常");
        }
    }

    /**
     * <h3>启动线程异步发送消息</h3>
     */
    private void asyncSend(){
        new Thread(new AsyncSendTask(), "async-send-task-thread").start();
        log.debug("[Neptune RPC Client]: 客户端异步线程启动");
    }

    public NeptuneRpcReference getReference(){
        return this.reference;
    }

    private final class AsyncSendTask implements Runnable{

        @Override
        public void run() {
            while (true){
                try {
                    // 1. 从阻塞队列中获取消息
                    NeptuneRpcInvocation invocation = NeptuneRpcClientCache.SEND_MESSAGE_QUEUE.poll(CALL_TIME_OUT, TimeUnit.SECONDS);
                    log.debug("[Neptune RPC Client]: 异步线程获取到消息 {}", invocation);
                    if (invocation == null){
                        future.channel().close();
                        break;
                    }
                    // 2. 序列化: 暂时采用 json
                    NeptuneRpcProtocol message =
                            new NeptuneRpcProtocol(JSON.toJSONString(invocation).getBytes(StandardCharsets.UTF_8));
                    // 3. 发送给服务端
                    future.channel().writeAndFlush(message);
                } catch (InterruptedException e) {
                    log.error("[Neptune RPC Client]: 异步线程发送消息异常", e);
                }
            }
        }
    }

}
