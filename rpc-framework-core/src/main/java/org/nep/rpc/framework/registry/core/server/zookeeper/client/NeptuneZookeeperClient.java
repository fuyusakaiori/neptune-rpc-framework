package org.nep.rpc.framework.registry.core.server.zookeeper.client;

import cn.hutool.core.collection.CollectionUtil;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.registry.core.server.zookeeper.listener.NodeChangeListener;
import org.nep.rpc.framework.registry.core.server.zookeeper.listener.ChildrenNodeListener;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <h3>Neptune RPC Client</h3>
 */
@Slf4j
@ToString
public class NeptuneZookeeperClient extends AbstractZookeeperClient {

    private final CuratorFramework zookeeperClient;

    public NeptuneZookeeperClient(NeptuneRpcRegisterConfig config) {
        super(config);
        this.zookeeperClient = CuratorFrameworkFactory.builder()
                                       .connectString(getConnectString())
                                       .connectionTimeoutMs(getConnectTime())
                                       .sessionTimeoutMs(getSessionTime())
                                       .retryPolicy(getRetryPolicy())
                                       .namespace(getNamespace())
                                       .build();
        // 注: 启动客户端
        if (zookeeperClient != null){
            zookeeperClient.start();
            log.info("[Neptune RPC Zookeeper]: Zookeeper 客户端启动");
        }
    }

    @Override
    public CuratorFramework getClient() {
       return zookeeperClient;
    }

    @Override
    public void closeClient() {
        zookeeperClient.close();
    }

    @Override
    public String getNodeData(String path) {
        try {
            byte[] result = zookeeperClient.getData().forPath(path);
            if (result != null)
                return new String(result);
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: GetNodeData 获取结点数据出现错误", e);
        }
        return null;
    }

    @Override
    public List<String> getChildrenNode(String path) {
        try {
            List<String> children = zookeeperClient.getChildren().forPath(path);
            if (!CollectionUtil.isEmpty(children))
                return children;
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: GetChildrenNode 获取子结点出现错误", e);
        }
        return null;
    }

    @Override
    public Stat getNodeStatus(String path) {
        Stat status = new Stat();
        try {
            byte[] result = zookeeperClient.getData().storingStatIn(status).forPath(path);
            if (result != null)
                log.info("[Neptune RPC Zookeeper]: GetNodeStatus 获取到的数据 {}", new String(result));
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: GetNodeStatus 获取结点状态出现错误", e);
        }
        return status;
    }

    @Override
    public void createNode(String path) {
        createNode(path, "", CreateMode.PERSISTENT);
    }

    @Override
    public void createNodeWithData(String path, String data) {
        if (data == null){
            log.warn("[Neptune RPC Zookeeper]: GetNodeStatus 携带数据不可以为空");
            return;
        }
        createNode(path, data, CreateMode.PERSISTENT);
    }

    @Override
    public void createNodeWithMode(String path, CreateMode mode) {
        if (mode == null){
            log.warn("[Neptune RPC Zookeeper]: GetNodeStatus 结点类型不可以为空");
            return;
        }
        createNode(path, "", mode);
    }

    /**
     * <h3>注: 多级路径注意父结点是否存在</h3>
     */
    @Override
    public void createNode(String path, String data, CreateMode mode) {
        if (data == null){
            log.warn("[Neptune RPC Zookeeper]: GetNodeStatus 携带数据不可以为空");
            return;
        }
        if (mode == null){
            log.warn("[Neptune RPC Zookeeper]: GetNodeStatus 结点类型不可以为空");
            return;
        }
        try {
            String result = zookeeperClient
                               .create().creatingParentsIfNeeded()
                               .withMode(mode)
                               .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            log.debug("[Neptune RPC Zookeeper]: CreateNode 创建的结点 {}", result);
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: CreateNode 创建结点出错", e);
        }
    }

    @Override
    public void setNodeData(String path, String data) {
        try {
            Stat status = zookeeperClient.setData()
                                  .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            log.debug("[Neptune RPC Zookeeper]: SetNodeData {}", status);
        } catch (Exception e) {
            log.debug("[Neptune RPC Zookeeper]: SetNodeData 设置结点数据出现错误", e);
        }
    }

    @Override
    public void setNodeDataWithVersion(String path, String data, int version) {
        try {
            Stat status = zookeeperClient.setData().withVersion(version)
                                .forPath(path, data.getBytes(StandardCharsets.UTF_8));
            log.debug("[Neptune RPC Zookeeper]: SetNodeDataWithVersion {}", status);
        } catch (Exception e) {
            log.debug("[Neptune RPC Zookeeper]: SetNodeDataWithVersion 设置结点数据出现错误", e);
        }
    }

    @Override
    public void deleteNode(String path) {
        try {
            zookeeperClient.delete().forPath(path);
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: DeleteNode 删除结点出现异常", e);
        }
    }

    @Override
    public void deleteNodeCallBack(String path, BackgroundCallback callback) {
        try {
            zookeeperClient.delete()
                    .guaranteed().inBackground(callback).forPath(path);
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: DeleteNodeCallBack 删除结点出现异常", e);
        }
    }

    @Override
    public void addNodeWatcher(String path) {
        // 1. 封装成结点
        CuratorCache curatorCache = CuratorCache.builder(zookeeperClient, path).build();
        // 2. 创建监听器
        CuratorCacheListener listener = CuratorCacheListener.builder()
                                                .forChanges(new NodeChangeListener()).build();
        // 3. 添加监听器
        curatorCache.listenable().addListener(listener);
        // 4. 启动监听器
        curatorCache.start();
    }

    @Override
    public void addChildrenNodeWatcher(String path) {
        // 1. 构建监听根结点
        CuratorCache curatorCache = CuratorCache.build(zookeeperClient, path);
        // 2. 构建监听器
        CuratorCacheListener.builder()
                .forInitialized(()->{

                })
                .forAll(((type, old, cur) -> {

                }))
                .forCreates(data -> {

                })
                .forChanges((old, cur) -> {

                })
                .forDeletes(data -> {

                })
                .forCreatesAndChanges((old, cur) -> {

                });
        CuratorCacheListener listener =
                CuratorCacheListener.builder()
                        .forPathChildrenCache(
                                path, zookeeperClient, new ChildrenNodeListener()).build();
        // 3. 添加监听器然后启动
        curatorCache.listenable().addListener(listener);
        curatorCache.start();
        // TODO 4. 不要关闭监听器, 否则启动之后就会立刻关闭, 之后考虑下如何关闭监听器
    }

    @Override
    public boolean existNode(String path) {
        try {
           return zookeeperClient.checkExists().forPath(path) != null;
        } catch (Exception e) {
            log.error("[Neptune RPC Zookeeper]: ExistNode 判断结点是否存在出现异常");
        }
        return false;
    }
}
