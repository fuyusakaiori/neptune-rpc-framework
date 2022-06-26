package org.nep.rpc.framework.core.common.config;

import lombok.Data;
import lombok.ToString;

/**
 * <h3>服务端配置类</h3>
 */
@Data
@ToString
public class NeptuneRpcServerConfig {
    // 服务端使用的端口号
    private Integer port;
    // 服务端使用的注册中心地址
    private String registry;
    // 服务端的名称
    private String application;

}
