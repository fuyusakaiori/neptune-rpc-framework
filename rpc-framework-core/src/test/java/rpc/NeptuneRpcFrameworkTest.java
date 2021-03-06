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
import org.nep.rpc.framework.core.serialize.NeptuneSerializerType;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerFactory;
import org.nep.rpc.framework.registry.core.server.zookeeper.client.NeptuneZookeeperClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("unchecked")
public class NeptuneRpcFrameworkTest
{

    @Test
    public void dynamicTest(){

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

//        // 1. ??????????????????
//        PathChildrenCache childrenCache = new PathChildrenCache(neptune, "/p1", true);
//        // 2. ???????????????
//        childrenCache.getListenable().addListener((client, event)->{
//            // 2.1 ??????????????????????????????????????????
//            // 2.2 ????????????????????????????????????????????????, ???????????????????????????????????????
//            if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(event.getType())){
//                log.info("???????????????: {}", event.getData().getData());
//            }else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(event.getType())){
//                log.info("??????????????????");
//            }else if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(event.getType())){
//                log.info("???????????????: {}", event.getData());
//            }else {
//                log.info("?????????????????????????????????????????????, ??????????????????");
//            }
//        });
//        // 3. ???????????????
//        childrenCache.start();
//        TimeUnit.MINUTES.sleep(2);

        NeptuneRpcClientConfig config = PropertyBootStrap.loadClientConfiguration();
        log.debug("config: {}", config);
        NeptuneZookeeperClient client = new NeptuneZookeeperClient(config.getRegisterConfig());
        client.addChildrenNodeWatcher("/org.nep.rpc.framework.interfaces.IDataService/provider");
        System.in.read();
    }

    @Test
    @DisplayName(value = "?????????????????????")
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
    @DisplayName(value = "????????????????????????")
    public void loadPropertiesTest(){
        PropertyBootStrap.loadServerConfiguration();
        PropertyBootStrap.loadClientConfiguration();
    }

    @Test
    @DisplayName(value = "????????????????????????")
    public void arraysCompareTest(){
        Class<?>[] types1 = new Class<?>[]{int.class, String.class};
        Class<?>[] types2 = new Class<?>[]{int.class, int.class};
        Class<?>[] types3 = new Class<?>[]{int.class, Integer.class};
        System.out.println(Integer.TYPE);
        System.out.println(Arrays.equals(types2, types3));
        System.out.println(Integer.class.getTypeName());
        System.out.println(Integer.TYPE.getTypeName());
    }


}
