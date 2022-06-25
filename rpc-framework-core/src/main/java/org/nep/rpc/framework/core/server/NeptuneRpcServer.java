package org.nep.rpc.framework.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcBeanCache;
import org.nep.rpc.framework.core.common.constant.ServerConfigConstant;
import org.nep.rpc.framework.core.handler.NeptuneRpcDecoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcEncoder;
import org.nep.rpc.framework.core.handler.NeptuneRpcServerHandler;
import org.nep.rpc.framework.core.neptune.DataService;
import org.nep.rpc.framework.core.protocal.NeptuneRpcFrameDecoder;

import java.net.InetSocketAddress;

import static org.nep.rpc.framework.core.common.constant.CommonConstant.DEFAULT_SERVER_PORT;

/**
 * <h3>Neptune RPC 服务器</h3>
 */
@Slf4j
public class NeptuneRpcServer {

    private final int port;

    private EventLoopGroup boss;

    private EventLoopGroup worker;

    public NeptuneRpcServer(){
        this(DEFAULT_SERVER_PORT);
    }

    public NeptuneRpcServer(int port){
        this.port = port;
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
            server.bind(new InetSocketAddress(port)).sync();
            log.info("[Neptune RPC Server]: 服务器启动成功");
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Server]: 服务器出现异常", e);
        }
    }

    private void close() {
        // 5. 关闭线程池
        worker.shutdownGracefully();
        boss.shutdownGracefully();
        log.info("[Neptune RPC Server]: 服务器关闭");
    }

    /**
     * <h3>1. 将对外提供的接口缓存</h3>
     * <h3>2. 避免每次反射调用的时候都去查询</h3>
     * <h3>TODO 为什么服务端提供的类必须实现接口?</h3>
     */
    private void registryClass(Object target){
        // TODO 1. 扫描对外提供的服务中的所有类
        // 2. 判断这个类是否实现接口, 如果没有实现接口, 那么就没有办法采用 JDK 动态代理
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if (interfaces.length == 0){
            log.error("[Neptune RPC Server]: 被调用的类没有实现接口");
            return;
        }
        if (interfaces.length > 1){
            log.error("[Neptune RPC Server]: 被调用的类只能有一个接口实现");
            return;
        }
        // 3. 如果符合条件, 那么就将类的名字和实例注册到哈希表中, 类似于 Spring 注册中心
        NeptuneRpcBeanCache.registryBean(interfaces[0].getName(), target);
    }

}
