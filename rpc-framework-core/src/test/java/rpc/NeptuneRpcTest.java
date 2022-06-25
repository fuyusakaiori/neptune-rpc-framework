package rpc;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.nep.rpc.framework.core.neptune.DataService;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxy;
import org.nep.rpc.framework.interfaces.IDataService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.nep.rpc.framework.core.common.cache.NeptuneRpcMessageCache.RESPONSE_CACHE;
import static org.nep.rpc.framework.core.common.cache.NeptuneRpcMessageCache.SEND_MESSAGE_QUEUE;

@Slf4j
@SuppressWarnings("unchecked")
public class NeptuneRpcTest {

    @Test
    public void dynamicTest(){
        IHelloService proxy = getProxy(HelloService.class);
        log.debug("proxy: {}", proxy);
    }

    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                this.getClass().getClassLoader(), clazz.getInterfaces(), new JdkDynamicProxy(clazz));
    }

    @Test
    public void messageQueueTest() throws InterruptedException {
        NeptuneRpcInvocation invocation = new NeptuneRpcInvocation();
        invocation.setArgs(null);
        invocation.setTargetMethod(null);
        invocation.setTargetClass(null);
        invocation.setUuid(RandomUtil.randomNumbers(6));
        SEND_MESSAGE_QUEUE.put(invocation);
    }

    @Test
    public void jsonTest(){
        System.out.println(JSON.toJSONString("Hello Neptune RPC").getBytes().length);
    }
}
