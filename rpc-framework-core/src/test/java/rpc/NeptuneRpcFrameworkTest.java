package rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerType;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxy;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerFactory;
import org.nep.rpc.framework.registry.service.zookeeper.client.NeptuneZookeeperClient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.stream.Collectors;

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
        invocation.setMethod(null);
        invocation.setService(null);
        invocation.setUuid(RandomUtil.randomNumbers(6));
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
        log.debug("sever config registry address: {}", serverConfig.getConfig().getAddress());
        log.debug("sever config registry connectTime: {}", serverConfig.getConfig().getConnectTime());
        log.debug("sever config registry sessionTime: {}", serverConfig.getConfig().getSessionTime());
        log.debug("sever config registry nameSpace: {}", serverConfig.getConfig().getNamespace());
        log.debug("sever config registry policy: {}", serverConfig.getConfig().getRetryPolicy());
        log.debug("sever config port: {}", serverConfig.getPort());
        NeptuneZookeeperClient client = new NeptuneZookeeperClient(serverConfig.getConfig());
        client.createNode("/p1");
    }

    @Test
    public void randomTest(){
        System.out.println(RandomUtil.randomInt(6));
    }

    @Test
    public void listenerTest() throws Exception
    {
        CuratorFramework neptune = CuratorFrameworkFactory.builder()
                                           .connectString("42.192.84.87:2181")
                                           .namespace("demo")
                                           .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                                           .build();
        neptune.start();
        CuratorCache curatorCache = CuratorCache.builder(neptune, "/p1").build();
        CuratorCacheListener listener = CuratorCacheListener.builder().forPathChildrenCache("/p1", neptune, new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                log.debug("eventxxxxxx: {}", event);
            }
        }).build();
        curatorCache.listenable().addListener(listener);
        curatorCache.start();

//        // 1. 创建监听结点
//        PathChildrenCache childrenCache = new PathChildrenCache(neptune, "/p1", true);
//        // 2. 添加监听器
//        childrenCache.getListenable().addListener((client, event)->{
//            // 2.1 这里可以获取到客户端以及事件
//            // 2.2 可以根据事件类型决定是否需要处理, 但是依然无法获取到旧的数据
//            if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(event.getType())){
//                log.info("现在的数据: {}", event.getData().getData());
//            }else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(event.getType())){
//                log.info("结点发生删除");
//            }else if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(event.getType())){
//                log.info("新增的结点: {}", event.getData());
//            }else {
//                log.info("其余的事件都是和连接相关的事件, 可以自己试试");
//            }
//        });
//        // 3. 启动监听器
//        childrenCache.start();
//        TimeUnit.MINUTES.sleep(2);

        NeptuneRpcClientConfig config = PropertyBootStrap.loadClientConfiguration();
        log.debug("config: {}", config);
        NeptuneZookeeperClient client = new NeptuneZookeeperClient(config.getRegisterConfig());
        client.addChildrenNodeWatcher("/org.nep.rpc.framework.interfaces.IDataService/provider");
        System.in.read();
    }

    @Test
    @DisplayName(value = "序列化算法测试")
    public void serializeTest(){
        NeptuneRpcInvocation invocation = new NeptuneRpcInvocation();
        invocation.setUuid(String.valueOf(RandomUtil.randomInt(6)));
        invocation.setTypes(new Class[]{String.class, Integer.class});
        invocation.setArgs(new Object[]{"message"});
        invocation.setService("INeptuneService.class");
        invocation.setMethod("send");
        INeptuneSerializer jsonSerializer = NeptuneSerializerFactory.getSerializer(
                NeptuneSerializerType.SERIALIZER_GSON.getCode());
        System.out.println(jsonSerializer.deserialize(jsonSerializer.serialize(invocation), NeptuneRpcInvocation.class));
        INeptuneSerializer jacksonSerializer =
                NeptuneSerializerFactory.getSerializer(NeptuneSerializerType.SERIALIZER_JACKSON.getCode());
        System.out.println(jacksonSerializer.deserialize(jacksonSerializer.serialize(invocation), NeptuneRpcInvocation.class));
        INeptuneSerializer jdkSerializer = NeptuneSerializerFactory.getSerializer(
                NeptuneSerializerType.SERIALIZER_JDK.getCode());
        System.out.println(jdkSerializer.deserialize(jdkSerializer.serialize(invocation), NeptuneRpcInvocation.class));
        INeptuneSerializer kryoSerializer = NeptuneSerializerFactory.getSerializer(
                NeptuneSerializerType.SERIALIZER_KRYO.getCode());
        System.out.println(kryoSerializer.deserialize(kryoSerializer.serialize(invocation), NeptuneRpcInvocation.class));
        INeptuneSerializer hessianSerializer = NeptuneSerializerFactory.getSerializer(
                NeptuneSerializerType.SERIALIZER_HESSIAN.getCode());
        System.out.println(hessianSerializer.deserialize(hessianSerializer.serialize(invocation), NeptuneRpcInvocation.class));
    }

    @Test
    @DisplayName(value = "配置文件加载测试")
    public void loadPropertiesTest(){
        PropertyBootStrap.loadServerConfiguration();
        PropertyBootStrap.loadClientConfiguration();
    }


}
