package org.nep.rpc.framework.core.common.constant;

import org.apache.curator.RetryPolicy;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>通用常量</h3>
 */
public class CommonConstant {
    // 服务调用超时时间
    public static final int CALL_TIME_OUT = 10;
    // 基本数据类型转包装类
    public static final Map<String, String> PRIMITIVE_TO_WRAPPER = new HashMap<>();
}
