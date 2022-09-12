package org.nep.rpc.framework.registry.core.server.zookeeper;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.registry.AbstractNeptuneRegister;
import org.nep.rpc.framework.registry.core.server.zookeeper.client.NeptuneZookeeperClient;
import org.nep.rpc.framework.registry.core.server.zookeeper.client.AbstractZookeeperClient;
import org.nep.rpc.framework.registry.url.NeptuneDefaultURL;
import org.nep.rpc.framework.registry.url.NeptuneURL;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h3>Neptune RPC Register</h3>
 */
@Slf4j
public class NeptuneZookeeperRegistry extends AbstractNeptuneRegister {

    // 消费者
    private static final String CONSUMER = "/consumer";
    // 提供者
    private static final String PROVIDER = "/provider";

    private final AbstractZookeeperClient zookeeperClient;

    public NeptuneZookeeperRegistry(NeptuneRpcRegisterConfig config) {
        this.zookeeperClient = new NeptuneZookeeperClient(config);
    }

    /**
     * <h3>服务注册（服务上线）</h3>
     */
    @Override
    public void register(NeptuneURL url) {
        // 1. 路径对象转换为路径字符串
        String path = url.toString(PROVIDER);
        log.info("[neptune rpc zookeeper] provider path - {}", path);
        // 2. 检查路径结点是否存在
        if (zookeeperClient.existNode(path)){
            // 2.1 如果结点已经存在, 那么就删除后重新更新 -> 结点的数据是带有时间戳的, 所以有必要重新更新
            log.info("[neptune rpc zookeeper] provider path is already exist, update again");
            // 2.2 删除结点
            zookeeperClient.deleteNode(path);
        }
        // 3. 创建服务提供者的结点: 采用临时结点注册
        zookeeperClient.createNode(path, url.toString(), CreateMode.EPHEMERAL);
        log.info("[neptune rpc zookeeper] provider register successfully");
    }

    /**
     * <h3>服务下线</h3>
     */
    @Override
    public void cancel(NeptuneURL url) {
        // 1. 删除结点
        zookeeperClient.deleteNode(url.toString(PROVIDER));
        log.info("[neptune rpc zookeeper] provider cancel successfully");
    }

    /**
     * <h3>服务订阅: 客户端注册结点到服务路径下</h3>
     */
    @Override
    public void subscribe(NeptuneURL url) {
        // 1. 路径对象转换为路径字符串
        String path = url.toString(CONSUMER);
        log.info("[neptune rpc zookeeper] consumer path - {}", path);
        // 2. 检查结点是否存在
        if (zookeeperClient.existNode(path)){
            log.info("[neptune rpc zookeeper]: consumer path is already exist, update again");
            zookeeperClient.deleteNode(path);
        }
        // 3. 创建服务订阅的结点: 采用临时结点
        zookeeperClient.createNode(path, url.toString(), CreateMode.EPHEMERAL);
        // 4. 服务订阅后添加到哈希表中: 哈希表中记录订阅的服务, 不是服务提供者
        NeptuneRpcClientCache.Service.subscribe(url);
    }

    /**
     * <h3>服务取消订阅</h3>
     */
    @Override
    public void unsubscribe(NeptuneURL url) {
        // 1. 删除消费者结点
        zookeeperClient.deleteNode(url.toString(CONSUMER));
        // 2. 移除订阅的路径
        NeptuneRpcClientCache.Service.unsubscribe(url);
    }

    /**
     * <h3>查询提供服务的所有服务端</h3>
     */
    @Override
    public List<String> lookup(String serviceName) {
        // 1. 校验查询的服务路径是否为空
        if (StrUtil.isEmpty(serviceName)){
            log.error("[neptune rpc zookeeper]: lookup service is empty or null");
            // 注: 不要直接返回空值, 否则会出现空指针异常
            return Collections.emptyList();
        }
        // 2. 拼接订阅的服务的路径
        String path = Separator.SLASH + serviceName + PROVIDER;
        // 3. 获取订阅的服务的路径下的所有子结点: 所有提供服务的服务端
        return zookeeperClient.getChildrenNode(path);
    }

    /**
     * <h3>服务订阅的前置处理: 钩子函数</h3>
     */
    @Override
    public void beforeSubscribe(String serviceName) {
        log.info("[neptune rpc zookeeper]: before subscribe service start...");


        log.info("[neptune rpc zookeeper]: before subscribe service end...");
    }

    /**
     * <h3>服务订阅后的后置处理: 钩子函数</h3>
     * <h3>监听订阅的结点</h3>
     */
    @Override
    public void afterSubscribe(String serviceName) {
        log.info("[neptune rpc zookeeper]: after subscribe service start...");
        // 1. 拼接注册的服务的根路径
        String root = Separator.SLASH + serviceName + PROVIDER;
        // 2. 监听服务提供者路径下所有的子结点: 是否有结点新增、删除、更新
        zookeeperClient.addChildrenNodeWatcher(root);
        // 3. 查询服务的所有提供者
        List<String> providers = providers(serviceName);
        // 4. 监听每个子结点的变化
        providers.forEach(path -> zookeeperClient.addNodeWatcher(root + Separator.SLASH + path));
        log.info("[neptune rpc zookeeper]: after subscribe service end...");
    }

    /**
     * <h3>缓存查询得到的对象是 NeptuneInvoker => 转换成对应的服务注册的路径</h3>
     */
    private List<String> providers(String serviceName){
        return NeptuneRpcClientCache.Connection.providers(serviceName).stream().map(invoker -> {
            NeptuneDefaultURL url = new NeptuneDefaultURL();
            url.setApplicationName(invoker.getApplicationName());
            url.setAddress(invoker.getAddress());
            url.setPort(invoker.getPort());
            url.setServiceName(invoker.getServiceName());
            url.setWeight(invoker.getFixedWeight());
            return url.toString(PROVIDER);
        }).collect(Collectors.toList());
    }

}
