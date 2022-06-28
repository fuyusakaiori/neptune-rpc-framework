package org.nep.rpc.framework.core.common.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nep.rpc.framework.core.common.constant.CommonConstant.DEFAULT_SERVER_PORT;

/**
 * <h3>初始化配置类并返回</h3>
 */
@Slf4j
public class PropertyBootStrap {

    private static final String SERVER_PORT = "neptune.server.port";
    private static final String REGISTER_ADDRESS = "neptune.register.address";
    private static final String APPLICATION_NAME = "neptune.application.name";
    private static final String PROXY_TYPE = "neptune.proxy.type";
    private static final String REGISTER_CONFIG_CONNECT_TIME = "neptune.register.connect.time";
    private static final String REGISTER_CONFIG_SESSION_TIME = "neptune.register.session.time";
    private static final String REGISTER_CONFIG_NAMESPACE = "neptune.register.namespace";
    private static final String REGISTER_CONFIG_RETRY_POLICY = "neptune.register.retry.policy";


    /**
     * <h3>返回服务器端的配置类</h3>
     */
    public static NeptuneRpcServerConfig loadServerConfiguration(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (Exception e) {
            throw new RuntimeException("[Neptune RPC Configuration]: 服务器端加载配置文件出现异常", e);
        }
        NeptuneRpcServerConfig config = new NeptuneRpcServerConfig();
        // 注: 因为方法返回的是包装类, 但是字段是基本数据类型, 所以判断下
        config.setPort(PropertiesLoader.getInt(SERVER_PORT));
        // 注: 注册中心配置
        config.setConfig(loadNeptuneRpcRegisterConfiguration());
        config.setApplication(PropertiesLoader.getString(APPLICATION_NAME));
        log.debug("config: {}", config);
        return config;
    }

    /**
     * <h3>返回客户端配置类</h3>
     */
    public static NeptuneRpcClientConfig loadClientConfiguration(){
        try {
            PropertiesLoader.loadConfiguration();
        } catch (Exception e) {
            throw new RuntimeException("[Neptune RPC Configuration]: 客户端加载配置文件出现异常", e);
        }
        NeptuneRpcClientConfig config = new NeptuneRpcClientConfig();
        config.setRegistry(PropertiesLoader.getString(REGISTER_ADDRESS));
        config.setApplication(PropertiesLoader.getString(APPLICATION_NAME));
        config.setProxy(PropertiesLoader.getString(PROXY_TYPE));
        return config;
    }

    private static NeptuneRpcRegisterConfig loadNeptuneRpcRegisterConfiguration(){
        NeptuneRpcRegisterConfig config = new NeptuneRpcRegisterConfig();
        String address = PropertiesLoader.getString(REGISTER_ADDRESS);
        if (address != null)
            config.setRegistry(address);
        Integer connectTime = PropertiesLoader.getInt(REGISTER_CONFIG_CONNECT_TIME);
        if (connectTime != null)
            config.setConnectTime(connectTime);
        Integer sessionTime = PropertiesLoader.getInt(REGISTER_CONFIG_SESSION_TIME);
        if (sessionTime != null)
            config.setSessionTime(sessionTime);
        String namespace = PropertiesLoader.getString(REGISTER_CONFIG_NAMESPACE);
        if (namespace != null)
            config.setNamespace(namespace);
        String policyName = PropertiesLoader.getString(REGISTER_CONFIG_RETRY_POLICY);
        if (policyName != null){
            // TODO 动态读取重试策略
        }
        return config;
    }

    // TODO 如何实现动态地将重试策略读取到内存中进行配置
    private static RetryPolicy loadRetryPolicy(String policyName){
        try {
            // 1. 获取类加载器
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            // 2. 根据全限定名加载类
            Class<?> clazz = classLoader.loadClass(policyName);
            // 3. 生成实例
            List<Integer> params = PropertiesLoader.getValues(REGISTER_CONFIG_RETRY_POLICY + ".")
                                           .stream()
                                           .map(Integer::parseInt)
                                           .collect(Collectors.toList());
            Constructor<?>[] constructors = clazz.getConstructors();
            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount() == params.size()){
                    Object[] args = params.toArray();
                    log.debug("args: {}", Arrays.toString(args));
                    // TODO 读取到的参数不是按照顺序填入构造函数中的, 这里暂时存在问题
                    return (RetryPolicy) constructor.newInstance(args);
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            log.error("[Neptune RPC Configuration]: 加载注册中心重试策略出现错误", e);
        }
        return null;
    }



}