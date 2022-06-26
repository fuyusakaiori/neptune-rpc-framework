package org.nep.rpc.framework.core.common.config;

import lombok.Data;

@Data
public class NeptuneRpcClientConfig {
    // 客户端使用的注册中心地址
    private String registry;
    // 客户端名称
    private String application;
    // 客户端使用动态代理模式
    private String proxy;
}
