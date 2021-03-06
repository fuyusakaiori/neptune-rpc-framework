package org.nep.rpc.framework.core.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.handler.NeptuneRpcClientHandler;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.protocol.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxyFactory;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;
import org.nep.rpc.framework.interfaces.INeptuneService;
import org.nep.rpc.framework.registry.AbstractNeptuneRegister;
import org.nep.rpc.framework.registry.core.server.zookeeper.NeptuneZookeeperRegistry;
import org.nep.rpc.framework.registry.url.DefaultURL;
import org.nep.rpc.framework.registry.url.NeptuneURL;


import java.util.List;

@Slf4j
public class NeptuneRpcClient {

    // 工作线程数量
    private static final int WORKER_THREAD_COUNT = 4;
    // 循环事件组
    private EventLoopGroup worker;
    // 动态代理包装类
    private NeptuneRpcReference reference;
    // 注册中心
    private AbstractNeptuneRegister registry;
    // 序列化算法
    private INeptuneSerializer serializer;
    // 客户端配置类
    private final NeptuneRpcClientConfig config;
    // 负载均衡策略
    private INeptuneRpcLoadBalance loadBalance;
    // 客户端
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
        // 1. 如果注册中心或者客户端已经启动过了, 那么禁止重复启动
        if (registry != null || client != null){
            log.info("[Neptune RPC Client]: 客户端已经启动了, 请不要重复启动");
            return;
        }
        // 2. 初始化注册中心
        registry = new NeptuneZookeeperRegistry(config.getRegisterConfig());
        // 3. 初始化序列化算法
        serializer = config.getSerializer();
        // 4. 初始化负载均衡策略
        loadBalance = config.getLoadBalanceStrategy();
        // 5. 初始化循环实践组
        client = new Bootstrap();
        worker = new NioEventLoopGroup(WORKER_THREAD_COUNT);
        // 5. 初始化客户端的配置
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
        // 注: 测试使用
        subscribeService(INeptuneService.class);
        // 6. 初始化客户端和所有服务提供者的连接
        connectService();
        // 7. 开启异步线程发送数据
        asyncSend();
        // 8. 初始化动态代理类
        this.reference = new NeptuneRpcReference(new JdkDynamicProxyFactory());
    }

    /**
     * <h3>订阅服务: 将自己添加到注册中心</h3>
     */
    private void subscribeService(Class<?> service){
        NeptuneURL url = getUrl(service);
        registry.subscribe(url);
        log.debug("[Neptune RPC Client]: 客户端注册服务");
    }

    private NeptuneURL getUrl(Class<?> service){
        NeptuneURL url = new DefaultURL();
        url.setPort(config.getPort());
        // TODO 应该直接在配置文件中配置好, 暂时用于测试使用
        url.setServiceName(service.getName());
        url.setAddress(config.getAddress());
        url.setApplicationName(config.getApplication());
        return url;
    }

    /**
     * <h3>和所有提供订阅的服务建立连接</h3>
     */
    private void connectService(){
        // 1. 初始化连接器
        NeptuneRpcConnectionHandler.init(client, loadBalance);
        // 2. 建立连接
        List<NeptuneURL> serviceUrls = NeptuneRpcClientCache.Services.getServices();
        for (NeptuneURL url : serviceUrls) {
            String service = url.getServiceName();
            // 2.1. 客户端分别和每个提供服务的服务器建立连接
            registry.lookup(service)
                    .forEach(path -> NeptuneRpcConnectionHandler.connect(service, path));
            // 2.2. 监听建立连接的服务器
            NeptuneURL defaultURL = new DefaultURL();
            defaultURL.setServiceName(service);
            registry.afterSubscribe(defaultURL);
        }
    }


    /**
     * <h3>关闭客户端</h3>
     */
    public void closeNeptune(){
        // TODO 考虑下客户端应该怎么优雅地关闭
    }

    /**
     * <h3>异步线程: 负责客户端和所有服务提供者之间的消息通信</h3>
     */
    private void asyncSend(){
        // 1. 创建线程对象
        Thread thread = new Thread(new AsyncSendTask(), "async-send-task-thread");
        // 2. 设置为守护线程: 只要客户端主线程结束, 那么异步发送的线程一起结束
        thread.setDaemon(true);
        // 3. 启动线程
        thread.start();
        log.debug("[Neptune RPC Client]: 客户端异步线程启动");
    }

    public NeptuneRpcReference getReference(){
        return this.reference;
    }

    private final class AsyncSendTask implements Runnable{
        @Override
        public void run() {
            while (true){
                // 1. 从阻塞队列中获取消息
                NeptuneRpcInvocation invocation = NeptuneRpcClientCache.MessageQueue.receive();
                log.debug("[Neptune RPC Client]: 异步线程获取到消息 {}", invocation);
                // 2. 序列化
                NeptuneRpcProtocol message =
                        new NeptuneRpcProtocol(serializer.serialize(invocation));
                // 3. 获取服务提供者
                NeptuneRpcInvoker wrapper =
                        NeptuneRpcConnectionHandler.channelWrapper(invocation.getService());
                if (wrapper != null){
                    log.debug("[Neptune RPC Client]: 客户端向服务端发送消息 [IP地址: {}, 端口号: {}]",
                            wrapper.getAddress(), wrapper.getPort());
                    // 4. 发送消息给对应的服务端
                    wrapper.getFuture().channel().writeAndFlush(message);
                }
            }
        }
    }

}
