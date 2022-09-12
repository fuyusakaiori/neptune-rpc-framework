package rpc;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.serialize.NeptuneJdkSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerType;
import org.nep.rpc.framework.interfaces.INeptuneService;
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
        invocation.setMethodName(null);
        invocation.setServiceName(null);
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
    @DisplayName(value = "配置文件加载测试")
    public void loadPropertiesTest(){
        PropertyBootStrap.loadServerConfiguration();
        PropertyBootStrap.loadClientConfiguration();
    }

    @Test
    @DisplayName(value = "数组内容比较测试")
    public void arraysCompareTest(){
        Class<?>[] types1 = new Class<?>[]{int.class, String.class};
        Class<?>[] types2 = new Class<?>[]{int.class, int.class};
        Class<?>[] types3 = new Class<?>[]{int.class, Integer.class};
        System.out.println(Integer.TYPE);
        System.out.println(Arrays.equals(types2, types3));
        System.out.println(Integer.class.getTypeName());
        System.out.println(Integer.TYPE.getTypeName());
    }

    @Test
    public void hashCodeTest(){
        Set<Integer> first = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        Set<Integer> second = new HashSet<>(Arrays.asList(2, 3, 5));
        System.out.println(CollectionUtil.disjunction(first, second));
    }

    @Test
    public void serializeCodeTest(){
        int serializerCode = NeptuneSerializerType.getSerializerCode(new NeptuneJdkSerializer());
        System.out.println(serializerCode);
    }

    @Test
    public void newProxyClassNameTest(){
        test("");
        System.out.println("yes");
    }

    public void test(String str){

    }


}
