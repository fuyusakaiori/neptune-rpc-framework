package org.nep.rpc.framework.registry.service.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.registry.service.AbstractRegister;
import org.nep.rpc.framework.registry.service.RegistryService;
import org.nep.rpc.framework.registry.service.zookeeper.client.AbstractZookeeperClient;
import org.nep.rpc.framework.registry.service.zookeeper.client.NeptuneZookeeperClient;
import org.nep.rpc.framework.registry.url.URL;

import java.util.List;

/**
 * <h3>Neptune RPC Register</h3>
 */
@Slf4j
public class NeptuneZookeeperRegister extends AbstractRegister implements RegistryService {
    private final AbstractZookeeperClient zookeeperClient;
    // 消费者
    private static final String CONSUMER = "/consumer";
    // 提供者
    private static final String PROVIDER = "/provider";
    // 分隔符
    private static final String SLASH = "/";
    private static final String COLON = ":";

    public NeptuneZookeeperRegister(NeptuneRpcRegisterConfig config) {
        this.zookeeperClient = new NeptuneZookeeperClient(config);
    }

    /**
     * <h3>注: 因为在创建客户端的时候就设置好了命名空间, 所以不需要判断根目录是否存在</h3>
     */
    @Override
    public void register(URL url) {
        // 1. 将原本的路径转换为字符串, 作为数据存储在结点中
        String data = url.toProviderString();
        // 2. 检查结点是否存在
        String path = toProviderPath(url);
        if (zookeeperClient.existNode(path)){
            log.debug("[Neptune RPC Zookeeper]: Register 结点之前已经存在, 重新更新");
            // 2.1 如果注册中心没有结点的话, 那么直接创建
            zookeeperClient.deleteNode(path);
        }
        // 2.2 如果注册中心已经存在结点了, 那么删除后重新创建; 因为 path 是不包含时间戳的, 但是 data 是由时间戳的, 同一个服务不同时间注册的 data 是不同的
        zookeeperClient.createNodeWithData(path, data);
        // 3. 存储在哈希表中
        super.register(url);
    }

    @Override
    public void cancel(URL url) {
        zookeeperClient.deleteNode(toProviderPath(url));
        super.cancel(url);
    }

    @Override
    public void subscribe(URL url) {
        // 1. 将原本的路径转换为字符串, 作为数据存储在结点中
        String data = url.toConsumerString();
        String path = toConsumerPath(url);
        // 2. 检查结点是否存在
        if (zookeeperClient.existNode(path)){
            log.debug("[Neptune RPC Zookeeper]: Subscribe 结点之前已经存在, 重新更新");
            zookeeperClient.deleteNode(path);
        }
        zookeeperClient.createNode(path, data, CreateMode.EPHEMERAL);
        super.subscribe(url);
    }

    @Override
    public void unSubscribe(URL url) {
        zookeeperClient.deleteNode(toConsumerPath(url));
        super.unSubscribe(url);
    }

    @Override
    public void beforeSubscribe(URL url) {

    }

    /**
     * <h3>订阅服务之后就需要监听这个服务, 防止发生变动</h3>
     */
    @Override
    public void afterSubscribe(URL url) {
        log.debug("path: {}", SLASH + url.getServiceName() + PROVIDER);
        zookeeperClient.addChildrenNodeWatcher(SLASH + url.getServiceName() + PROVIDER);
    }

    /**
     * <h3>根据服务名找到所有提供这个服务的服务器</h3>
     */
    @Override
    public List<String> providers(String serviceName) {
        if (serviceName == null){
            log.error("[Neptune RPC Zookeeper]: 服务名不可以为空");
            return null;
        }
        String path = SLASH + serviceName + PROVIDER;
        log.debug("path: {}", path);
        return zookeeperClient.getChildrenNode(path);
    }

    @Override
    public List<String> getServiceAllNodes() {
        return null;
    }

    /**
     * <h3>把 URL 转换为服务提供者结点的名称</h3>
     */
    private String toProviderPath(URL url){
        log.debug("url: {}", url);
        return SLASH + url.getServiceName() + PROVIDER + SLASH
                       + url.getAddress() + COLON + url.getPort();
    }

    /**
     * <h3>把 URL 转换为服务消费者结点的名称</h3>
     */
    private String toConsumerPath(URL url){
        log.debug("url: {}", url);
        return SLASH + url.getServiceName() + CONSUMER + SLASH
                + url.getApplicationName() + COLON + url.getAddress() + COLON + url.getPort();
    }
}
