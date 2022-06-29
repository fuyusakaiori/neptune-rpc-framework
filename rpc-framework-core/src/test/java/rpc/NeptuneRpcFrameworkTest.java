package rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.Test;
import org.nep.rpc.framework.core.client.NeptuneRpcClient;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxy;
import org.nep.rpc.framework.registry.service.zookeeper.client.NeptuneZookeeperClient;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache.SEND_MESSAGE_QUEUE;

@Slf4j
@SuppressWarnings("unchecked")
public class NeptuneRpcFrameworkTest
{

    @Test
    public void dynamicTest(){

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

    @Test
    public void overloadTest() throws InvocationTargetException, IllegalAccessException {
        log.debug("type: {}", Integer.class.getName());
        log.debug("{}", null + "");
        String methodName = "method";
        Object[] args = new Object[]{1};
        Class<NeptuneRpcFrameworkTest> clazz = NeptuneRpcFrameworkTest.class;
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (checkMethod(method, methodName, args)){
                method.invoke(new NeptuneRpcFrameworkTest(), args);
                break;
            }
        }
    }

    public boolean checkMethod(Method method, String methodName, Object[] args){
        Map<String, String> map = new HashMap<>();
        map.put("int", "java.lang.Integer");
        if (!method.getName().equals(methodName))
            return false;
        if (method.getParameterCount() != args.length)
            return false;
        List<String> target = Arrays.stream(args)
                        .map(arg -> arg.getClass().getTypeName())
                        .collect(Collectors.toList());
        List<String> source = Arrays.stream(method.getParameterTypes())
                                       .map(parameter -> parameter.isPrimitive() ? map.get(parameter.getTypeName()): parameter.getTypeName())
                                       .collect(Collectors.toList());
        return CollectionUtil.containsAll(target, source);
    }

    public void method(int value){
        log.info("int value: {}", value);
    }

    public void method(String value){
        log.info("String value: {}", value);
    }

    @Test
    public void propertiesTest(){
        NeptuneRpcServerConfig serverConfig = PropertyBootStrap.loadServerConfiguration();
        log.debug("sever config application: {}", serverConfig.getApplication());
        log.debug("sever config registry address: {}", serverConfig.getConfig().getRegistry());
        log.debug("sever config registry connectTime: {}", serverConfig.getConfig().getConnectTime());
        log.debug("sever config registry sessionTime: {}", serverConfig.getConfig().getSessionTime());
        log.debug("sever config registry nameSpace: {}", serverConfig.getConfig().getNamespace());
        log.debug("sever config registry policy: {}", serverConfig.getConfig().getRetryPolicy());
        log.debug("sever config port: {}", serverConfig.getPort());
        NeptuneZookeeperClient client = new NeptuneZookeeperClient(serverConfig);
        client.createNode("/p1");

    }
}
