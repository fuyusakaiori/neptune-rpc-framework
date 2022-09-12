package org.nep.rpc.framework.core.proxy.javassist;

import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponseCode;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;

/**
 * <h3>Javassist 动态代理</h3>
 */
@Slf4j
public class JavassistProxy implements InvocationHandler {

    /**
     * <h3>超时时间</h3>
     */
    private static final int CALL_TIME_OUT = 10;


    /**
     * <h3>代理的目标对象接口</h3>
     */
    private final NeptuneRpcReference reference;

    public JavassistProxy(NeptuneRpcReference reference) {
        this.reference = reference;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 1. 填充发送的请求
        NeptuneRpcInvocation invocation = new NeptuneRpcInvocation();
        invocation.setArgs(args);
        invocation.setMethodName(method.getName());
        invocation.setServiceName(reference.getTarget().getName());
        invocation.setAttachments(reference.getAttachments());
        invocation.setTypes(method.getParameterTypes());
        invocation.setUuid(RandomUtil.randomNumbers(6));
        // 2. 把即将要发送的请求的序列号填充到哈希表中, 确保接收的时候是对应的
        NeptuneRpcClientCache.Windows.put(invocation.getUuid(), "");
        // 3. 把将要发送的请求放在消息队列中, 然后让异步线程来获取
        NeptuneRpcClientCache.MessageQueue.send(invocation);
        // 4. 动态代理对象等待返回结果
        LocalDateTime begin = LocalDateTime.now();
        log.info("[neptune rpc client proxy] client proxy start time - {}", begin);
        // 5. 如果当前时间始终在超时时间之前, 那么就持续轮询, 看是否有结果返回
        while (LocalDateTime.now().isBefore(begin.plusMinutes(CALL_TIME_OUT))){
            Object result = NeptuneRpcClientCache.Windows.get(invocation.getUuid());
            if (result instanceof NeptuneRpcResponse){
                NeptuneRpcResponse response = (NeptuneRpcResponse) result;
                log.info("[neptune rpc client proxy]: client receive response - {}", response);
                if (NeptuneRpcResponseCode.FAIL.getCode() == response.getCode())
                    throw new RuntimeException(response.getMessage());
                return response.getBody();
            }
        }
        // 6. 如果超时那么就直接抛出异常
        throw new TimeoutException("[neptune rpc client proxy]: client remote call timeout");
    }
}
