package org.nep.rpc.framework.core.common.resource;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.config.NeptuneRpcClientConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;

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
        config.setRegistry(PropertiesLoader.getString(REGISTER_ADDRESS));
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



}
