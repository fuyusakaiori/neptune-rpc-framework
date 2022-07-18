package org.nep.rpc.framework.core.server;

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
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcServerHandler;
import org.nep.rpc.framework.core.neptune.NeptuneRpcService;
import org.nep.rpc.framework.core.protocol.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.registry.service.RegistryService;
import org.nep.rpc.framework.registry.service.zookeeper.NeptuneZookeeperRegister;
import org.nep.rpc.framework.registry.url.DefaultURL;
import org.nep.rpc.framework.registry.url.URL;

import java.net.InetSocketAddress;

/**
 * <h3>Neptune RPC 服务器</h3>
 */
@Slf4j
public class NeptuneRpcServer {
    // 负责处理连接事件的循环事件组
    private EventLoopGroup boss;
    // 负责处理其他事件的循环事件组
    private EventLoopGroup worker;
    private ChannelFuture future;
    // 注册中心
    private RegistryService registryService;
    // 服务端
    private ServerBootstrap server;
    // 服务器端配置类
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
        registryService = new NeptuneZookeeperRegister(config.getConfig());
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
        // TODO 注: 缓存对外提供的接口 硬编码, 用于测试使用
        registryClass(new NeptuneRpcService());
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
            registryService.cancel(getUrl(new Class[]{NeptuneRpcService.class}));
            future.channel().close();
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Server]: 服务器关闭出现异常");
        }
        log.info("[Neptune RPC Server]: 服务器关闭");
    }

    /**
     * <h3>1. 将对外提供的接口缓存</h3>
     * <h3>2. 避免每次反射调用的时候都去查询</h3>
     * <h3>注: 这里目前主要用于测试使用</h3>
     */
    private void registryClass(Object target){
        // 1. 服务提供者不会直接将实现类暴露出来, 而是通过接口的形式对外提供, 所以实现类必须实现接口, 才是对外提供的服务
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0){
            log.error("[Neptune RPC Server]: 被调用的类没有实现接口");
            return;
        }
        // 2. thrift 框架好像每个对外提供的实现类也只会是实现一个接口, 暂时不清楚为什么只能有一个接口
        if (interfaces.length > 1){
            log.error("[Neptune RPC Server]: 被调用的类只能有一个接口实现");
            return;
        }
        // 3. 如果符合条件, 那么就将对外提供的接口名字和实现类对象放入缓存中
        NeptuneRpcServerCache.registerService(interfaces[0].getName(), target);
        // 4. 将对外提供的服务 (接口 / 类) 生成对应的 URL 后存储在本地缓存中
        NeptuneRpcServerCache.registerServiceUrl(getUrl(interfaces));
    }

    /**
     * <h3>异步地将缓存在本地的 URL 地址添加到注册中心</h3>
     */
    private void registryServices(){
        new Thread(new AsyncRegistryTask()).start();
        log.debug("[Neptune RPC Server]: 服务端异步线程启动");
    }

    private final class AsyncRegistryTask implements Runnable{
        @Override
        public void run() {
            if (NeptuneRpcServerCache.hasServicesUrl()){
                for (URL url : NeptuneRpcServerCache.getServiceUrls()) {
                    registryService.register(url);
                }
            }
        }
    }

    /**
     * <h3>生成 URL</h3>
     */
    private URL getUrl(Class<?>[] interfaces){
        URL url = new DefaultURL();
        // 1. 从配置中获取服务器端口号
        url.setPort(config.getPort());
        // 2. 从配置中获取服务器所在 IP 地址
        url.setAddress(config.getAddress());
        // 3. 从配置中获取服务器名称
        url.setApplicationName(config.getApplication());
        // 4. 从配置中获取服务器对外提供的服务
        url.setServiceName(interfaces[0].getName());
        return url;
    }

}
