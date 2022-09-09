package org.nep.rpc.framework.core.common.resource;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.hash.NeptuneConsistentHashLoadBalance;
import org.nep.rpc.framework.core.router.random.NeptuneSimpleRandomLoadBalance;
import org.nep.rpc.framework.core.router.random.NeptuneWeightRandomLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneSimpleRoundRobinLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneSmoothRoundRobinLoadBalance;
import org.nep.rpc.framework.core.router.round.NeptuneWeightRoundRobinLoadBalance;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

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

    private static final String SERIALIZE_PREFIX = "org.nep.rpc.framework.core.serialize";

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
            return serverConfig;
        }
        serverConfig = new NeptuneRpcServerConfig();
        try {
            PropertiesLoader.loadConfiguration();
            // 1. 直接获取本机 IP 地址 InetAddress.getLocalHost().getHostAddress()
            serverConfig.setAddress(ADDRESS);
            log.info("[Neptune RPC Configuration] server config address: {}", serverConfig.getAddress());
            // 2. 获取资源文件中配置端口号
            serverConfig.setPort(PropertiesLoader.getIntegerValue(SERVER_PORT));
            log.info("[Neptune RPC Configuration] server config port: {}", serverConfig.getPort());
            // 3. 获取资源文件中配置的服务名
            serverConfig.setApplication(PropertiesLoader.getStringValue(APPLICATION_NAME));
            log.info("[Neptune RPC Configuration] server config application: {}", serverConfig.getApplication());
            // 4. 获取资源文件中注册中心的相关配置
            serverConfig.setConfig(loadNeptuneRpcRegisterConfiguration());
            // 5. 获取配置序列化算法
            serverConfig.setSerializer(loadNeptuneRpcSerializer());
        } catch (Exception e) {
            throw new RuntimeException("[Neptune RPC Configuration]: 服务器端加载配置文件出现异常", e);
        }
        return serverConfig;
    }

    /**
     * <h3>加载客户端配置</h3>
     */
    public static NeptuneRpcClientConfig loadClientConfiguration(){
        if (Objects.nonNull(clientConfig)){
            return clientConfig;
        }
        try {
            PropertiesLoader.loadConfiguration();
        } catch (Exception e) {
            throw new RuntimeException("[Neptune RPC Configuration]: 客户端加载配置文件出现异常", e);
        }
        clientConfig = new NeptuneRpcClientConfig();
        clientConfig.setPort(PropertiesLoader.getIntegerValue(SERVER_PORT));
        log.info("[Neptune RPC Configuration] client config port: {}", clientConfig.getPort());
        clientConfig.setAddress(ADDRESS);
        log.info("[Neptune RPC Configuration] client config port: {}", clientConfig.getAddress());
        clientConfig.setApplicationName(PropertiesLoader.getStringValue(APPLICATION_NAME));
        log.info("[Neptune RPC Configuration] client config application: {}", clientConfig.getApplicationName());
        clientConfig.setProxy(PropertiesLoader.getStringValue(PROXY_TYPE));
        log.info("[Neptune RPC Configuration] client config proxy: {}", clientConfig.getProxy());
        clientConfig.setSerializer(loadNeptuneRpcSerializer());
        clientConfig.setRegisterConfig(loadNeptuneRpcRegisterConfiguration());
        clientConfig.setLoadBalanceStrategy(loadNeptuneRpcLoadBalance());
        log.debug("config: {}", clientConfig);
        return clientConfig;
    }

    /**
     * <h3>加载注册中心</h3>
     */
    private static NeptuneRpcRegisterConfig loadNeptuneRpcRegisterConfiguration(){
        NeptuneRpcRegisterConfig config = new NeptuneRpcRegisterConfig();
        String address = PropertiesLoader.getStringValue(REGISTER_ADDRESS);
        if (address != null){
            config.setAddress(address);
            log.info("[Neptune RPC PropertiesBootStrap]: config registry address: {}", address);
        }
        Integer connectTime = PropertiesLoader.getIntegerValue(REGISTER_CONFIG_CONNECT_TIME);
        if (connectTime != null) {
            config.setConnectTime(connectTime);
            log.info("[Neptune RPC PropertiesBootStrap]: config registry connectTime: {}", connectTime);
        }
        Integer sessionTime = PropertiesLoader.getIntegerValue(REGISTER_CONFIG_SESSION_TIME);
        if (sessionTime != null){
            config.setSessionTime(sessionTime);
            log.info("[Neptune RPC PropertiesBootStrap]: config registry sessionTime: {}", sessionTime);
        }
        String namespace = PropertiesLoader.getStringValue(REGISTER_CONFIG_NAMESPACE);
        if (namespace != null){
            config.setNamespace(namespace);
            log.info("[Neptune RPC PropertiesBootStrap]: config registry namespace: {}", namespace);
        }
        return config;
    }

    /**
     * <h3>加载序列化策略</h3>
     */
    private static INeptuneSerializer loadNeptuneRpcSerializer() {
        INeptuneSerializer serializer = null;
        // 1. 获取采用的序列化算法名称
        String serializeName = PropertiesLoader.getStringValue(SERIALIZE_TYPE);
        // 2. 检查序列化算法名称是否为空
        if (StrUtil.isEmpty(serializeName)) {
            throw new RuntimeException("[neptune rpc configuration]: load configuration serializer name is null");
        }
        log.info("[neptune rpc configuration] load configuration serializer name is - {}", serializeName);
        // 3. 获取类加载器
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (!serializeName.contains(SERIALIZE_PREFIX)){
                serializeName = SERIALIZE_PREFIX + Separator.DOT + serializeName;
            }
            // 4. 反射加载
            Class<?> clazz = classLoader.loadClass(serializeName);
            serializer = (INeptuneSerializer) clazz.getConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("[neptune rpc configuration]: load configuration serializer occurred error");
        }
        return serializer;
    }

    /**
     * <h3>加载路由策略</h3>
     */
    private static INeptuneRpcLoadBalance loadNeptuneRpcLoadBalance(){
        INeptuneRpcLoadBalance strategy = null;
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
                default:
                    return new NeptuneSmoothRoundRobinLoadBalance();
            }
        } catch (Exception e) {
            throw new RuntimeException("[neptune rpc configuration]: load configuration strategy occurred error");
        }
    }



}
