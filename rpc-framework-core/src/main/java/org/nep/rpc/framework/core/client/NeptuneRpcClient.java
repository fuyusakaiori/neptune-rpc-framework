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
import org.nep.rpc.framework.registry.AbstractNeptuneRegister;
import org.nep.rpc.framework.registry.core.server.zookeeper.NeptuneZookeeperRegistry;
import org.nep.rpc.framework.registry.url.NeptuneDefaultURL;
import org.nep.rpc.framework.registry.url.NeptuneURL;


import java.util.List;
import java.util.Objects;

@Slf4j
public class NeptuneRpcClient {

    private static final String CONSUMER = "/consumer";

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
        // 6. 初始化客户端和所有服务提供者的连接
        connectService();
        // 7. 开启异步线程发送数据
        asyncSend();
        // 8. 初始化动态代理类
    }

    public void closeNeptune(){
        // TODO 考虑下客户端应该怎么优雅地关闭
    }

    public NeptuneRpcReference getReference(){
        if (Objects.nonNull(reference)){
            return this.reference;
        }
        return reference = new NeptuneRpcReference(new JdkDynamicProxyFactory());
    }

    public NeptuneRpcClientConfig getClientConfig(){
        return config;
    }

    /**
     * <h3>服务订阅: 自动装配时订阅服务</h3>
     */
    public void subscribeService(Class<?> service){
        // 1. 根据订阅的服务接口组成生成 URL 对象
        NeptuneURL url = getUrl(service);
        // 2. 服务订阅
        registry.subscribe(url);
        // 3. 记录订阅的路径
        log.info("[neptune rpc client]: client subscribe service url - {}", url.toString(CONSUMER));
    }

    /**
     * <h3>组装服务订阅的 URL 对象</h3>
     */
    private NeptuneURL getUrl(Class<?> service){
        NeptuneURL url = new NeptuneDefaultURL();
        url.setPort(config.getPort());
        // TODO 应该直接在配置文件中配置好, 暂时用于测试使用
        url.setServiceName(service.getName());
        url.setAddress(config.getAddress());
        url.setApplicationName(config.getApplicationName());
        return url;
    }

    /**
     * <h3>客户端和所有提供服务的服务端建立连接</h3>
     * <h3>1. 从哈希表中获取所有订阅的服务</h3>
     * <h3>2. 查询每个服务有哪些服务端提供并且和所有服务端都建立连接</h3>
     */
    private void connectService(){
        // 1. 初始化连接器
        NeptuneRpcConnectionHandler.init(client, loadBalance);
        // 2. 从哈希表中获取所有已经订阅的服务接口
        List<String> services = NeptuneRpcClientCache.Service.getServices();
        // 3. 遍历所有订阅的服务接口然后和所有提供服务的服务端建立连接
        for (String service : services) {
            // 3.1. 调用注册中心查询提供服务的所有服务端然后分别建立连接
            registry.lookup(service)
                    .forEach(path -> NeptuneRpcConnectionHandler.connect(service, path));
            // 3.2 监听已经订阅的服务下的所有子结点
            registry.afterSubscribe(service);
        }
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
