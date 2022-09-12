package org.nep.rpc.framework.core.common.resource;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.proxy.ProxyFactory;
import org.nep.rpc.framework.core.proxy.javassist.JavassistProxyFactory;
import org.nep.rpc.framework.core.proxy.jdk.JdkDynamicProxyFactory;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.hash.NeptuneConsistentHashLoadBalance;
import org.nep.rpc.framework.core.router.random.NeptuneSimpleRandomLoadBalance;
import org.nep.rpc.framework.core.router.random.NeptuneWeightRandomLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneSimpleRoundRobinLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneSmoothRoundRobinLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneWeightRoundRobinLoadBalance;
import org.nep.rpc.framework.core.serialize.*;

import java.lang.reflect.Constructor;
import java.util.Objects;


/**
 * <h3>初始化配置类并返回</h3>
 */
@Slf4j
public class PropertyBootStrap {
    private static final String APPLICATION_NAME = "neptune.application.name";
    private static final String ADDRESS = "127.0.0.1";
    //========================================== 序列化协议 ==========================================
    private static final String SERIALIZE_TYPE = "neptune.serialize.type";

    //========================================== 注册中心配置 ==========================================
    private static final String REGISTER_ADDRESS = "neptune.register.address";
    private static final String REGISTER_CONFIG_CONNECT_TIME = "neptune.register.connect.time";
    private static final String REGISTER_CONFIG_SESSION_TIME = "neptune.register.session.time";
    private static final String REGISTER_CONFIG_NAMESPACE = "neptune.register.namespace";

    //========================================== 客户端配置 ==========================================
    private static final String PROXY_TYPE = "neptune.client.proxy.type";
    private static final String BALANCE_POLICY = "neptune.client.balance.policy";


    //========================================== 服务端配置 ==========================================
    private static final String SERVER_PORT = "neptune.server.port";

    @Getter
    private static NeptuneRpcServerConfig serverConfig;

    @Getter
    private static NeptuneRpcClientConfig clientConfig;

    /**
     * <h3>加载服务端配置</h3>
     */
    public static NeptuneRpcServerConfig loadServerConfiguration(){
        if (Objects.nonNull(serverConfig)){
            log.warn("[neptune rpc configuration]: server configuration already has loaded");
            return serverConfig;
        }
        serverConfig = new NeptuneRpcServerConfig();
        try {
            PropertiesLoader.loadConfiguration();
            // 1. 直接获取本机 IP 地址 InetAddress.getLocalHost().getHostAddress()
            serverConfig.setAddress(ADDRESS);
            log.info("[neptune rpc configuration] server configuration loading address: {}", serverConfig.getAddress());

            // 2. 获取资源文件中配置端口号
            serverConfig.setPort(PropertiesLoader.getIntegerValue(SERVER_PORT));
            log.info("[neptune rpc configuration] server configuration loading port: {}", serverConfig.getPort());

            // 3. 获取资源文件中配置的服务名
            serverConfig.setApplication(PropertiesLoader.getStringValue(APPLICATION_NAME));
            log.info("[neptune rpc configuration] server configuration loading application: {}", serverConfig.getApplication());

            // 4. 获取资源文件中注册中心的相关配置
            serverConfig.setConfig(loadNeptuneRpcRegisterConfiguration());
            log.info("[neptune rpc configuration] server configuration loading register");

            // 5. 获取配置序列化算法
            serverConfig.setSerializer(loadNeptuneRpcSerializer());
            log.info("[neptune rpc configuration] server configuration loading serializer");
        } catch (Exception e) {
            throw new RuntimeException("[neptune rpc configuration]: server configuration loading occurred error", e);
        }
        return serverConfig;
    }

    /**
     * <h3>加载客户端配置</h3>
     */
    public static NeptuneRpcClientConfig loadClientConfiguration(){
        if (Objects.nonNull(clientConfig)){
            log.warn("[neptune rpc configuration]: client configuration already has loaded");
            return clientConfig;
        }
        try {
            PropertiesLoader.loadConfiguration();
            clientConfig = new NeptuneRpcClientConfig();

            clientConfig.setPort(PropertiesLoader.getIntegerValue(SERVER_PORT));
            log.info("[neptune rpc configuration] client configuration loading port: {}", clientConfig.getPort());

            clientConfig.setAddress(ADDRESS);
            log.info("[neptune rpc configuration] client configuration loading address: {}", clientConfig.getAddress());

            clientConfig.setApplicationName(PropertiesLoader.getStringValue(APPLICATION_NAME));
            log.info("[neptune rpc configuration] client configuration loading application: {}", clientConfig.getApplicationName());

            clientConfig.setProxyFactory(loadNeptuneProxyFactory());
            log.info("[neptune rpc configuration] client configuration loading proxy: {}", clientConfig.getProxyFactory());

            clientConfig.setSerializer(loadNeptuneRpcSerializer());
            log.info("[neptune rpc configuration] client configuration loading serializer: {}", clientConfig.getSerializer());

            clientConfig.setRegisterConfig(loadNeptuneRpcRegisterConfiguration());
            log.info("[neptune rpc configuration] client configuration loading register: {}", clientConfig.getRegisterConfig());

            clientConfig.setLoadBalanceStrategy(loadNeptuneRpcLoadBalance());
            log.info("[neptune rpc configuration] client configuration loading load balance: {}", clientConfig.getLoadBalanceStrategy());

        } catch (Exception e) {
            throw new RuntimeException("[neptune rpc configuration]: client configuration loading occurred error", e);
        }

        return clientConfig;
    }

    /**
     * <h3>加载动态代理策略</h3>
     */
    private static ProxyFactory loadNeptuneProxyFactory(){
        String proxyType = PropertiesLoader.getStringValue(PROXY_TYPE);
        if (ProxyFactory.javassist.equals(proxyType)){
            return new JavassistProxyFactory();
        }else if (ProxyFactory.jdk.equals(proxyType)){
            return new JdkDynamicProxyFactory();
        }else{
            throw new RuntimeException("[neptune rpc configuration] proxy type doesn't support");
        }
    }

    /**
     * <h3>加载注册中心</h3>
     */
    private static NeptuneRpcRegisterConfig loadNeptuneRpcRegisterConfiguration(){
        NeptuneRpcRegisterConfig config = new NeptuneRpcRegisterConfig();
        String address = PropertiesLoader.getStringValue(REGISTER_ADDRESS);
        if (Objects.nonNull(address)){
            config.setAddress(address);
            log.info("[neptune rpc configuration]: config registry address: {}", address);
        }
        Integer connectTime = PropertiesLoader.getIntegerValue(REGISTER_CONFIG_CONNECT_TIME);
        if (Objects.nonNull(connectTime)) {
            config.setConnectTime(connectTime);
            log.info("[neptune rpc configuration]: config registry connectTime: {}", connectTime);
        }
        Integer sessionTime = PropertiesLoader.getIntegerValue(REGISTER_CONFIG_SESSION_TIME);
        if (Objects.nonNull(sessionTime)){
            config.setSessionTime(sessionTime);
            log.info("[neptune rpc configuration]: config registry sessionTime: {}", sessionTime);
        }
        String namespace = PropertiesLoader.getStringValue(REGISTER_CONFIG_NAMESPACE);
        if (Objects.nonNull(namespace)){
            config.setNamespace(namespace);
            log.info("[neptune rpc configuration]: config registry namespace: {}", namespace);
        }
        return config;
    }

    /**
     * <h3>加载序列化策略</h3>
     */
    private static INeptuneSerializer loadNeptuneRpcSerializer() {
        // 1. 获取采用的序列化算法名称
        String serializeName = PropertiesLoader.getStringValue(SERIALIZE_TYPE);
        // 2. 检查序列化算法名称是否为空
        if (StrUtil.isEmpty(serializeName)) {
            throw new RuntimeException("[neptune rpc configuration]: load configuration serializer name is null");
        }
        log.info("[neptune rpc configuration] load configuration serializer name is - {}", serializeName);
        // 3. 根据序列化算法名选择序列化算法
        switch (serializeName) {
            case INeptuneSerializer.gson:
                return new NeptuneGsonSerializer();
            case INeptuneSerializer.hessian:
                return new NeptuneHessianSerializer();
            case INeptuneSerializer.jackson:
                return new NeptuneJackSonSerializer();
            case INeptuneSerializer.kryo:
                return new NeptuneKryoSerializer();
            case INeptuneSerializer.jdk:
                return new NeptuneJdkSerializer();
            default:
                throw new RuntimeException("[neptune rpc configuration]: serializer type doesn't not supported");
        }
    }

    /**
     * <h3>加载路由策略</h3>
     */
    private static INeptuneRpcLoadBalance loadNeptuneRpcLoadBalance(){
        // 1. 获取配置的负载均衡策略的全限定名
        String strategyName = PropertiesLoader.getStringValue(BALANCE_POLICY);
        if (StrUtil.isEmpty(strategyName))
            throw new RuntimeException("[neptune rpc configuration]: load configuration strategy name is null");
        log.info("[neptune rpc configuration] load configuration strategy name - {}", strategyName);
        // 2. 直接根据加载的策略名称创建相应的对象: 反射感觉不是特别好
        try {
            switch (strategyName) {
                case INeptuneRpcLoadBalance.consistentHash:
                    return new NeptuneConsistentHashLoadBalance();
                case INeptuneRpcLoadBalance.randomSimple:
                    return new NeptuneSimpleRandomLoadBalance();
                case INeptuneRpcLoadBalance.randomWeight:
                    return new NeptuneSimpleRoundRobinLoadBalance();
                case INeptuneRpcLoadBalance.robinSimple:
                    return new NeptuneWeightRandomLoadBalance();
                case INeptuneRpcLoadBalance.robinWeight:
                    return new NeptuneWeightRoundRobinLoadBalance();
                case INeptuneRpcLoadBalance.robinSmooth:
                    return new NeptuneSmoothRoundRobinLoadBalance();
                default:
                    throw new RuntimeException("[neptune rpc configuration] load balance type doesn't support");
            }
        } catch (Exception e) {
            throw new RuntimeException("[neptune rpc configuration]: load configuration strategy occurred error");
        }
    }



}
