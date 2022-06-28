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
    // 异步加载等待时间
    public static final int ASYNC_REGISTRY_TIME_OUT = 5;
}
