package org.nep.rpc.framework.core.common.config;

import lombok.Data;
import lombok.ToString;
import org.nep.rpc.framework.core.proxy.ProxyFactory;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;

@Data
@ToString
public class NeptuneRpcClientConfig {
    // 服务端 IP 地址
    private String address;
    // 服务端端口号
    private Integer port;
    // 服务提供者的名称
    private String applicationName;
    // 客户端使用动态代理模式
    private ProxyFactory proxyFactory;
    // 客户端采用序列化算法
    private INeptuneSerializer serializer;
    // 客户端使用的负载均衡策略
    private INeptuneRpcLoadBalance loadBalanceStrategy;
    // 客户端访问的注册中心
    private NeptuneRpcRegisterConfig registerConfig;
}
