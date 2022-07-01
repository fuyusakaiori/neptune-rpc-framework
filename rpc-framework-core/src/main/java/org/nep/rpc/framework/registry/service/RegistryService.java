package org.nep.rpc.framework.registry.service;

import org.nep.rpc.framework.registry.url.URL;

/**
 * <h3>注册中心: zookeeper</h3>
 * <h3>1. 提供服务注册</h3>
 * <h3>2. 提供服务下线</h3>
 * <h3>3. 提供服务的订阅 / 发现以及取消订阅</h3>
 */
public interface RegistryService {

    /**
     * <h3>1. 服务注册</h3>
     * <h3>2. 如果网络出现故障, 那么需要采取超时重连</h3>
     * <h3>3. 注册的服务需要持久化的文件中存储; 可以考虑存储在数据库中吗? 只有持久化结点需要存储?</h3>
     * <h3>4. URL 格式是自定义的, 不是 Java 自带的 URL</h3>
     */
    void register(URL url);

    /**
     * <h3>1. 服务下线</h3>
     */
    void cancel(URL url);

    /**
     * <h3>1. 订阅服务</h3>
     */
    void subscribe(URL url);

    /**
     * <h3>1. 取消订阅</h3>
     */
    void unSubscribe(URL url);


}
