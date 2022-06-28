package org.nep.rpc.framework.core.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NeptuneRpcRegisterConfig {

    private static final String REGISTER_CONFIG_CONNECT_STRING = "127.0.0.1:2181";
    private static final int REGISTER_CONFIG_CONNECT_TIME = 15 * 1000;
    private static final int REGISTER_CONFIG_SESSION_TIME = 60 * 1000;
    private static final String REGISTER_CONFIG_NAMESPACE = "/neptune";

    // 注册中心地址
    private String registry = REGISTER_CONFIG_CONNECT_STRING;
    // 注册中心连接时间
    private int connectTime = REGISTER_CONFIG_CONNECT_TIME;
    // 注册中心会话保持时间
    private int sessionTime = REGISTER_CONFIG_SESSION_TIME;
    // 注册中心命名空间
    private String namespace = REGISTER_CONFIG_NAMESPACE;
    // 服务端使用注册中心采取的重试策略
    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);

}
