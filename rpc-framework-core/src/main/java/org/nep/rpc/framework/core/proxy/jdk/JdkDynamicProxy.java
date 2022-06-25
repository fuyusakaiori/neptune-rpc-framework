package org.nep.rpc.framework.core.proxy.jdk;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.CommonConstant;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static org.nep.rpc.framework.core.common.cache.NeptuneRpcMessageCache.RESPONSE_CACHE;
import static org.nep.rpc.framework.core.common.cache.NeptuneRpcMessageCache.SEND_MESSAGE_QUEUE;
import static org.nep.rpc.framework.core.common.constant.CommonConstant.CALL_TIME_OUT;

/**
 * <h3>JDK 动态代理</h3>
 */
@Slf4j
public class JdkDynamicProxy implements InvocationHandler {

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
        invocation.setTargetMethod(method.getName());
        invocation.setTargetClass(clazz.getName());
        invocation.setUuid(RandomUtil.randomNumbers(6));
        // 2. 通知异步线程可以发送了
        RESPONSE_CACHE.put(invocation.getUuid(), "");
        SEND_MESSAGE_QUEUE.put(invocation);
        // 3. 动态代理对象等待返回结果
        LocalDateTime begin = LocalDateTime.now();
        log.debug("begin: {}", begin);
        // 4. 如果当前时间始终在超时时间之前, 那么就持续轮询, 看是否有结果返回
        while (LocalDateTime.now().isBefore(begin.plusMinutes(CALL_TIME_OUT))){
            Object result = RESPONSE_CACHE.get(invocation.getUuid());
            if (result instanceof NeptuneRpcInvocation){
                log.debug("[Neptune RPC Client]: 服务器响应 {}", result);
                return ((NeptuneRpcInvocation) result).getResponse();
            }
        }
        // 5. 如果超时那么就直接抛出异常
        throw new TimeoutException("[Neptune RPC Client]: 调用超时");
    }

}
