package org.nep.rpc.framework.core.client;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.filter.chain.NeptuneClientFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientDirectInvokerFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientGroupFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientLogFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

/**
 * <h3>负责处理连接相关</h3>
 */
@Slf4j
public class NeptuneRpcConnectionHandler {

    private static final int IP_INDEX = 1;

    private static final int PORT_INDEX = 2;

    private static final int PATH_LENGTH = 3;

    /**
     * <h3>客户端过滤链</h3>
     */
    private static final NeptuneClientFilter filter = new NeptuneClientLogFilter()
                                                              .setNextFilter(new NeptuneClientGroupFilter())
                                                              .setNextFilter(new NeptuneClientDirectInvokerFilter());

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
        // 1. 检查客户端是否已经启动
        if (client == null)
            throw new RuntimeException("[neptune rpc client connection handler]: connection handler find client still don't start");
        // 2. 服务名和结点路径是否为空
        if (StrUtil.isEmpty(serviceName) || StrUtil.isEmpty(path)){
            log.error("[neptune rpc client connection handler]: service name or node path is null");
            return;
        }
        // 3. 检验连接是否已经存在
        if (NeptuneRpcClientCache.Connection.isConnect(serviceName, path)){
            log.warn("[neptune rpc client connection handler]: connection handler occurred duplicate connection");
            return;
        }
        // 5. 获取启动必要的端口号和 IP 地址
        String[] partitions = path.split(":");
        // 6. 检查分割后是否合法
        if (partitions.length != PATH_LENGTH){
            log.error("[neptune rpc client connection handler] connection handler path pattern is error");
            return;
        }
        int port = Integer.parseInt(partitions[PORT_INDEX]);
        String address = partitions[IP_INDEX];
        // 6. 准备建立连接
        try {
            ChannelFuture future = client.connect(new InetSocketAddress(address, port)).sync();
            if (!future.isSuccess()){
                log.warn("[neptune rpc client connection handler]: connection handler connected fail ip: {}, port: {}", address, port);
                return;
            }
            NeptuneRpcInvoker invoker = new NeptuneRpcInvoker();
            invoker.setPort(port);
            invoker.setAddress(address);
            invoker.setFuture(future);
            // 7. 记录当前建立的连接
            NeptuneRpcClientCache.Connection.connect(serviceName, invoker);
            log.info("[neptune rpc client connection handler]: connection handler connected successfully ip: {}, port: {}", address, port);
        } catch (InterruptedException e) {
            log.error("[neptune rpc client connection handler]: connection handler connected error", e);
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
     * <h3>负载均衡</h3>
     */
    public static NeptuneRpcInvoker select(NeptuneRpcInvocation invocation){
        // 1. 参数校验
        if (StrUtil.isEmpty(invocation.getServiceName())){
            log.error("[neptune rpc client connection handler]: connection handler select serviceName is null");
            return null;
        }
        // 2. 获取所有提供服务的服务端: 服务名 + 方法名共同决定
        List<NeptuneRpcInvoker> invokers = NeptuneRpcClientCache.Connection.providers(invocation.getServiceName());
        if (CollectionUtil.isEmpty(invokers)){
            log.warn("[neptune rpc client connection handler]: connection handler can't find provider");
            return null;
        }
        // 注: 过滤不满足条件的服务端
        filter.execute(invokers, invocation);
        // 3. 采用最简单的策略实现负载均衡: 随机选择
        return loadBalance.select(invokers, invocation);
    }

}
