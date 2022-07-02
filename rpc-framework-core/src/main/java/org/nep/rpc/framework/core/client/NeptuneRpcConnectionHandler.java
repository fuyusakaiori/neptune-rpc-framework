package org.nep.rpc.framework.core.client;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * <h3>负责处理连接相关</h3>
 */
@Slf4j
public class NeptuneRpcConnectionHandler {

    private static Bootstrap client;

    private static INeptuneRpcLoadBalance loadBalance;

    public static void init(Bootstrap client, INeptuneRpcLoadBalance loadBalance){
        NeptuneRpcConnectionHandler.client = client;
        NeptuneRpcConnectionHandler.loadBalance = loadBalance;
    }

    /**
     * <h3>负责建立连接</h3>
     */
    public static void connect(String serviceName, String path){
        // 1. 参数校验
        if (client == null)
            throw new RuntimeException("[Neptune RPC Client]: 客户端未启动");
        if (StrUtil.isEmpty(serviceName) || StrUtil.isEmpty(path)){
            log.error("[Neptune RPC Client]: 传入的服务名和结点路径格式为空");
            return;
        }
        log.debug("service: {}, path: {}", serviceName, path);
        // 2. 获取启动必要的端口号和 IP 地址
        String[] ipAndPort = path.split(":");
        if ( ipAndPort.length != 2){
            log.error("[Neptune RPC Client]: 传入的结点路径格式存在问题");
            return;
        }
        int port = Integer.parseInt(ipAndPort[1]);
        String address = ipAndPort[0];
        // 3. 准备建立连接
        try {
            ChannelFuture future = client.connect(new InetSocketAddress(address, port)).sync();
            NeptuneRpcInvoker wrapper = new NeptuneRpcInvoker();
            wrapper.setPort(port);
            wrapper.setAddress(address);
            wrapper.setFuture(future);
            // 4. 记录当前建立的连接
            NeptuneRpcClientCache.Connection.connect(serviceName, wrapper);
            log.debug("[Neptune RPC Client]: 服务端连接建立成功");
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Client]: 服务端连接建立失败", e);
        }
    }

    /**
     * <h3>负责断开连接</h3>
     */
    public static void disconnect(String serviceName, String path){
        if (serviceName == null || path == null){
            log.debug("[Neptune RPC Client]: 服务名和结点的路径为空");
            return;
        }
        NeptuneRpcInvoker wrapper =
                NeptuneRpcClientCache.Connection.disconnect(serviceName, path);
        if (wrapper == null){
            log.error("[Neptune RPC Client]: 需要关闭的连接不存在");
            return;
        }
        try {
            ChannelFuture future = wrapper.getFuture();
            future.channel().closeFuture().sync();
            log.debug("[Neptune RPC Client]: 服务连接:[端口号: {}, IP地址: {}] 已经关闭", wrapper.getPort(), wrapper.getAddress());
            future.channel().close();
        } catch (InterruptedException e) {
            log.error("[Neptune RPC Client]: 客户端关闭和服务端连接时出现异常", e);
        }
    }

    /**
     * <h3>负责获取连接的包装类</h3>
     */
    public static NeptuneRpcInvoker channelWrapper(String service){
        // 1. 参数校验
        if (StrUtil.isEmpty(service)){
            log.error("[Neptune RPC Client]: 传入的服务名为空, 无法查找对应的服务");
            return null;
        }
        // 2. 获取服务提供者
        List<NeptuneRpcInvoker> providers = NeptuneRpcClientCache.Connection.providers(service);
        if (CollectionUtil.isEmpty(providers)){
            log.warn("[Neptune RPC Client]: 当前服务并没有任何服务器正在提供服务");
            return null;
        }
        // 3. 采用最简单的策略实现负载均衡: 随机选择
        return loadBalance.select(providers);
    }

}
