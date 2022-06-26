package org.nep.rpc.framework.core.common.constant;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>通用常量</h3>
 */
public class CommonConstant {
    // 服务器端口号
    public static final int DEFAULT_SERVER_PORT = 4396;
    // 服务器地址
    public static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
    // 服务调用超时时间
    public static final int CALL_TIME_OUT = 10;
    // 基本数据类型转包装类
    public static final Map<String, String> PRIMITIVE_TO_WRAPPER = new HashMap<>();
    // 连接超时时间
    public static final int CONNECT_TIME_OUT = 15 * 1000;
    // 会话保持时间
    public static final int SESSION_KEEP_TIME = 60 * 1000;
    // 默认命名空间
    public static final String DEFAULT_NAMESPACE = "/";
    // 默认重试策略
    public static final RetryPolicy DEFAULT_RETRY_POLICY = new ExponentialBackoffRetry(1000, 10);
}
