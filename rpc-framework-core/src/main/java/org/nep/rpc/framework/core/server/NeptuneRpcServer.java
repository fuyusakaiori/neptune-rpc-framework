package org.nep.rpc.framework.core.server;

import cn.hutool.core.collection.CollectionUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.common.constant.ServerConfig;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.filter.server.NeptuneServerLogFilter;
import org.nep.rpc.framework.core.filter.server.NeptuneTokenFilter;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcServerHandler;
import org.nep.rpc.framework.core.protocol.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.registry.AbstractNeptuneRegister;
import org.nep.rpc.framework.registry.core.server.zookeeper.NeptuneZookeeperRegistry;
import org.nep.rpc.framework.registry.url.NeptuneDefaultURL;
import org.nep.rpc.framework.registry.url.NeptuneURL;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * <h3>Neptune RPC 服务器</h3>
 */
@Slf4j
public class NeptuneRpcServer {

    private static final String PROVIDER = "/provider";

    /**
     * <h3>处理连接事件</h3>
     */
    private EventLoopGroup boss;
    /**
     * <h3>处理读写事件</h3>
     */
    private EventLoopGroup worker;
    private ChannelFuture future;
    /**
     * <h3>注册中心</h3>
     */
    private AbstractNeptuneRegister registry;
    /**
     * <h3>服务器</h3>
     */
    private ServerBootstrap server;

    /**
     * <h3>配置类</h3>
     */
    private final NeptuneRpcServerConfig config;


    public NeptuneRpcServer(){
        this(PropertyBootStrap.loadServerConfiguration());
    }

    public NeptuneRpcServer(NeptuneRpcServerConfig config){
        this.config = config;
    }


    /**
     * <h3>启动服务器</h3>
     */
    public void startNeptune() {
        // 0. 初始化注册中心
        registry = new NeptuneZookeeperRegistry(config.getConfig());
        // 1. 初始化服务器
        server = new ServerBootstrap();
        // 2. 初始化事件循环组
        boss = new NioEventLoopGroup(ServerConfig.BOSS_THREAD_COUNT);
        worker = new NioEventLoopGroup(ServerConfig.WORKER_THREAD_COUNT);
        // 3. 配置参数
        server.option(ChannelOption.TCP_NODELAY, true)  // 2.1 禁用 Nagle 算法
                .option(ChannelOption.SO_BACKLOG, ServerConfig.BACK_LOG_SIZE) // 2.2 服务器端是单线程处理, 所以会有等待队列
                .option(ChannelOption.SO_SNDBUF, ServerConfig.SEND_BUFFER_SIZE) // 2.3 发送方缓冲区大小
                .option(ChannelOption.SO_RCVBUF, ServerConfig.RECEIVE_BUFFER_SIZE) // 2.4 接收方缓冲区大小
                .option(ChannelOption.SO_KEEPALIVE, true); // 2.5 如果超过两个小时没有数据发送, 那么就会发送探测报文
        // 4. 服务注册
        registryServices();
        // 5. 启动服务器
        try {
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            // 6. 添加处理器
                            channel.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                            channel.pipeline().addLast(new NeptuneRpcFrameDecoder()); // 6.1 定长解码器 防止黏包和半包问题
                            channel.pipeline().addLast(new NeptuneRpcEncoder()); // 6.2 编码器
                            channel.pipeline().addLast(new NeptuneRpcDecoder()); // 6.3 解码器
                            // TODO 考虑之后重构成 Codec
                            channel.pipeline().addLast(new NeptuneRpcServerHandler());
                        }
                    });
            future = server.bind(new InetSocketAddress(config.getPort())).sync();
            log.info("[Neptune RPC Server]: 服务器启动成功");
            // 注: 服务器同步等待关闭: 如果在某个地方调用 close 方法, 那么服务器就会直接关闭
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Server]: 服务器出现异常", e);
        }
    }

    /**
     * <h3>关闭服务器</h3>
     */
    public void close() {
        try {
            worker.shutdownGracefully();
            boss.shutdownGracefully();
            future.channel().close().sync();
            // TODO 对外提供的服务都应该下线
            future.channel().close();
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Server]: 服务器关闭出现异常");
        }
        log.info("[Neptune RPC Server]: 服务器关闭");
    }
    public NeptuneRpcServerConfig getServerConfig(){
        return this.config;
    }

    /**
     * <h3>服务端注册服务</h3>
     */
    public void registerService(NeptuneServiceWrapper wrapper){
        if (Objects.isNull(wrapper) || Objects.isNull(wrapper.getService())){
            log.error("[neptune rpc server]: service wrapper or service is null");
            return;
        }
        // 0. 获取目标服务
        Object target = wrapper.getService();
        // 1. 检查提供的服务是否实现接口或者实现过多个接口: 仅允许暴露的服务实现单个接口
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0){
            log.error("[neptune rpc server]: export service don't implement interface");
            return;
        }
        if (interfaces.length > 1){
            log.error("[neptune rpc server]: export service implements multiple interfaces");
            return;
        }
        // 3. 将每个服务端提供的所有接口-实现类全部保存在哈希表中: key: interface value: service 包装类
        NeptuneRpcServerCache.Service.registerService(interfaces[0].getName(), wrapper);
        // 4. 将每个服务端提供的所有接口全部转换成对应的地址然后异步注册到注册中心
        NeptuneRpcServerCache.URLS.add(getUrl(wrapper));
        // TODO 限流和 token 之后再做
    }

    /**
     * <h3>异步地将缓存在本地的 URL 地址添加到注册中心</h3>
     */
    private void registryServices(){
        log.info("[neptune rpc server async task]: async thread start");
        new Thread(new AsyncRegistryTask()).start();
    }

    private final class AsyncRegistryTask implements Runnable{
        @Override
        public void run() {
            // 1. 检查服务端缓存的 URL 是否为空
            if (!CollectionUtil.isEmpty(NeptuneRpcServerCache.URLS)){
                // 2. 遍历服务端缓存的 URL 然后注册到注册中心
                NeptuneRpcServerCache.URLS.forEach(url -> {
                    log.info("[neptune rpc server async thread]: register service url start - {}", url.toString(PROVIDER));
                    registry.register(url);
                });
                // TODO 3. 所有服务端缓存的 URL 都注册到注册中心后就可以直接移除, 否则会造成内存泄漏
            }
        }
    }

    /**
     * <h3>生成 URL</h3>
     */
    private NeptuneURL getUrl(NeptuneServiceWrapper wrapper){
        NeptuneURL url = new NeptuneDefaultURL();
        // 1. 从配置中获取服务器端口号
        url.setPort(config.getPort());
        // 2. 从配置中获取服务器所在 IP 地址
        url.setAddress(config.getAddress());
        // 3. 从配置中获取服务器名称
        url.setApplicationName(config.getApplication());
        // 4. 从配置中获取服务器对外提供的服务
        url.setServiceName(wrapper.getService().getClass().getInterfaces()[0].getName());
        // 5. 添加其他附加参数
        url.getParams().put(NeptuneURL.group, wrapper.getGroup());
        url.getParams().put(NeptuneURL.limit, wrapper.getLimit());
        return url;
    }

}
