package org.nep.rpc.framework.core.proxy.jdk;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponseCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * <h3>JDK 动态代理</h3>
 */
@Slf4j
public class JdkDynamicProxy implements InvocationHandler {
    // 超时时间
    private static final int CALL_TIME_OUT = 10;

    private static final Map<String, String> PRIMITIVE_MAP = new HashMap<>();

    // 代理的目标对象
    private final Class<?> clazz;

    public JdkDynamicProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * <h3>1. 动态代理的过程也会去执行 invoke 方法</h3>
     * <h3>2. 问题: ConcurrentHashMap 不允许存放空键值对</h3>
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 填充发送的请求
        NeptuneRpcInvocation invocation = new NeptuneRpcInvocation();
        invocation.setArgs(args);
        invocation.setMethodName(method.getName());
        invocation.setServiceName(clazz.getName());
        invocation.setTypes(method.getParameterTypes());
        invocation.setUuid(RandomUtil.randomNumbers(6));
        // 2. 把即将要发送的请求的序列号填充到哈希表中, 确保接收的时候是对应的
        NeptuneRpcClientCache.Windows.put(invocation.getUuid(), "");
        // 3. 把将要发送的请求放在消息队列中, 然后让异步线程来获取
        NeptuneRpcClientCache.MessageQueue.send(invocation);
        // 4. 动态代理对象等待返回结果
        LocalDateTime begin = LocalDateTime.now();
        log.debug("begin: {}", begin);
        // 5. 如果当前时间始终在超时时间之前, 那么就持续轮询, 看是否有结果返回
        while (LocalDateTime.now().isBefore(begin.plusMinutes(CALL_TIME_OUT))){
            Object result = NeptuneRpcClientCache.Windows.get(invocation.getUuid());
            if (result instanceof NeptuneRpcResponse){
                log.info("[Neptune RPC Client]: 服务器响应 {}", result);
                NeptuneRpcResponse response = (NeptuneRpcResponse) result;
                if (NeptuneRpcResponseCode.FAIL.getCode() == response.getCode())
                    throw new RuntimeException(response.getMessage());
                return response.getBody();
            }
        }
        // 6. 如果超时那么就直接抛出异常
        throw new TimeoutException("[Neptune RPC Client]: 调用超时");
    }

}
