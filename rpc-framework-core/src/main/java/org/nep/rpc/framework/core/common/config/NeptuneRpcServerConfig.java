package org.nep.rpc.framework.core.common.config;

import lombok.Data;
import lombok.ToString;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.net.InetAddress;

/**
 * <h3>服务端配置类</h3>
 */
@Data
@ToString
public class NeptuneRpcServerConfig {
    // 服务端使用的端口号
    private Integer port;
    // 服务端使用的 IP 地址
    private String address;
    // 服务端的名称
    private String application;
    // 服务端注册中心的配置
    private NeptuneRpcRegisterConfig config;
    // 服务端采用的序列化算法
    private INeptuneSerializer serializer;

}
