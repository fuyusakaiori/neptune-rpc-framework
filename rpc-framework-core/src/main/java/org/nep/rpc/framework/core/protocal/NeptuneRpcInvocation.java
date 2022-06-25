package org.nep.rpc.framework.core.protocal;

import lombok.Data;
import lombok.ToString;

import java.lang.reflect.Method;

/**
 * <h3>请求需要的参数都保存在这个类</h3>
 * <h3>1. 不要直接使用 Method / Class, 会导致整个数据报很大</h3>
 */
@Data
@ToString
public class NeptuneRpcInvocation {
    // 0. 请求序列号
    private String uuid;
    // 1. 调用的目标方法
    private String targetMethod;
    // 2. 调用的目标类
    private String targetClass;
    // 3. 方法携带的参数
    private Object[] args;
    // 4. 响应结果
    private Object response;

}
