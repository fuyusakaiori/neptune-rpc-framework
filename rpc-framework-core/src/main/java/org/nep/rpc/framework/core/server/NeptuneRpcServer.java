package org.nep.rpc.framework.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.common.constant.ServerConfigConstant;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcServerHandler;
import org.nep.rpc.framework.core.neptune.DataService;
import org.nep.rpc.framework.core.protocal.NeptuneRpcFrameDecoder;
import org.nep.rpc.framework.registry.service.RegistryService;
import org.nep.rpc.framework.registry.service.zookeeper.NeptuneZookeeperRegister;
import org.nep.rpc.framework.registry.url.DefaultURL;
import org.nep.rpc.framework.registry.url.URL;

import java.net.InetSocketAddress;

import static org.nep.rpc.framework.core.common.constant.CommonConstant.*;

/**
 * <h3>Neptune RPC 服务器</h3>
 */
@Slf4j
public class NeptuneRpcServer {

    static {
        PRIMITIVE_TO_WRAPPER.put("int", Integer.class.getName());
        PRIMITIVE_TO_WRAPPER.put("float", Float.class.getName());
        PRIMITIVE_TO_WRAPPER.put("double", Double.class.getName());
        PRIMITIVE_TO_WRAPPER.put("boolean", Boolean.class.getName());
        PRIMITIVE_TO_WRAPPER.put("byte", Byte.class.getName());
        PRIMITIVE_TO_WRAPPER.put("short", Short.class.getName());
        PRIMITIVE_TO_WRAPPER.put("long", Long.class.getName());
        PRIMITIVE_TO_WRAPPER.put("char", Character.class.getName());
    }
    // 负责处理连接事件的循环事件组
    private EventLoopGroup boss;
    // 负责处理其他事件的循环事件组
    private EventLoopGroup worker;
    private ChannelFuture future;
    // 注册中心
    private final RegistryService registryService;
    // 服务器端配置类
    private final NeptuneRpcServerConfig config;

    public NeptuneRpcServer(){
        this(PropertyBootStrap.loadServerConfiguration());
    }

    public NeptuneRpcServer(NeptuneRpcServerConfig config){
        this.config = config;
        this.registryService = new NeptuneZookeeperRegister(config.getRegistry());
    }


    /**
     * <h3>启动服务器</h3>
     */
    public void startNeptune() {
        ServerBootstrap server = new ServerBootstrap();
        // 1. 初始化事件循环组
        boss = new NioEventLoopGroup(ServerConfigConstant.BOSS_THREAD_COUNT);
        worker = new NioEventLoopGroup(ServerConfigConstant.WORKER_THREAD_COUNT);
        // 2. 配置参数
        server.option(ChannelOption.TCP_NODELAY, true)  // 2.1 禁用 Nagle 算法
                .option(ChannelOption.SO_BACKLOG, ServerConfigConstant.BACK_LOG_SIZE) // 2.2 服务器端是单线程处理, 所以会有等待队列
                .option(ChannelOption.SO_SNDBUF, ServerConfigConstant.SEND_BUFFER_SIZE) // 2.3 发送方缓冲区大小
                .option(ChannelOption.SO_RCVBUF, ServerConfigConstant.RECEIVE_BUFFER_SIZE) // 2.4 接收方缓冲区大小
                .option(ChannelOption.SO_KEEPALIVE, true); // 2.5 如果超过两个小时没有数据发送, 那么就会发送探测报文
        // TODO 注: 缓存对外提供的接口 硬编码, 用于测试使用
        registryClass(new DataService());
        // 3. 启动服务器
        try {
            server.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            // 4. 添加处理器
                            channel.pipeline().addLast(new NeptuneRpcFrameDecoder()); // 4.1 定长解码器 防止黏包和半包问题
                            channel.pipeline().addLast(new NeptuneRpcEncoder()); // 4.2 编码器
                            channel.pipeline().addLast(new NeptuneRpcDecoder()); // 4.3 解码器
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
        }finally {
            // 5. 关闭服务器
            close();
        }
    }

    /**
     * <h3>关闭服务器</h3>
     */
    private void close() {
        worker.shutdownGracefully();
        boss.shutdownGracefully();
        future.channel().close();
        log.info("[Neptune RPC Server]: 服务器关闭");
    }

    /**
     * <h3>1. 将对外提供的接口缓存</h3>
     * <h3>2. 避免每次反射调用的时候都去查询</h3>
     * TODO <h3>3. 等待处理: 利用反射将提供的服务全部注册到哈希表中</h3>
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
        NeptuneRpcServerCache.registryInCache(interfaces[0].getName(), target);
        // 4. 向注册中心添加 URL
        NeptuneRpcServerCache.registerInCache(getUrl(config, interfaces));
    }

    private URL getUrl(NeptuneRpcServerConfig config, Class<?>[] interfaces){
        URL url = new DefaultURL();
        url.setPort(config.getPort());
        url.setApplicationName(config.getApplication());
        url.setAddress(DEFAULT_SERVER_ADDRESS);
        url.setServiceName(interfaces[0].getName());
        return url;
    }

}
