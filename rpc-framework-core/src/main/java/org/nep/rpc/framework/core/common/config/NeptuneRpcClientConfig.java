package org.nep.rpc.framework.core.common.config;

import lombok.Data;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

@Data
public class NeptuneRpcClientConfig {
    // 服务端 IP 地址
    private String address;
    // 服务端端口号
    private Integer port;
    // 客户端访问的注册中心
    private NeptuneRpcRegisterConfig registerConfig;
    // 客户端名称
    private String application;
    // 客户端使用动态代理模式
    private String proxy;
    // 客户端采用序列化算法
    private INeptuneSerializer serializer;
}
