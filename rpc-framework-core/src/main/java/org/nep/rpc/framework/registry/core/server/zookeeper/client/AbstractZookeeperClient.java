package org.nep.rpc.framework.registry.core.server.zookeeper.client;

import lombok.Data;
import lombok.ToString;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.registry.core.client.NeptuneRegistryClient;

import java.util.List;

/**
 * <h3>zookeeper 客户端模板</h3>
 * <h3>1. 提供子类的基本模板</h3>
 * <h3>2. 封装原生方法</h3>
 */
@Data
@ToString
public abstract class AbstractZookeeperClient implements NeptuneRegistryClient {
    // 连接 zookeeper 服务端的地址: IP + 端口号
    private String connectString;
    // 连接超时时间
    private int connectTime;
    // 会话保持时间
    private int sessionTime;
    // 重试策略
    private RetryPolicy retryPolicy;
    // 命名空间
    private String namespace;
    public AbstractZookeeperClient(NeptuneRpcRegisterConfig config) {
        if (config == null)
            throw new RuntimeException("[neptune rpc zookeeper client]: zookeeper configuration is null");
        this.connectString = config.getAddress();
        this.connectTime = config.getConnectTime();
        this.sessionTime = config.getSessionTime();
        this.retryPolicy = config.getRetryPolicy();
        this.namespace = config.getNamespace();
    }

    @Override
    public CuratorFramework getClient() {
        return null;
    }

    @Override
    public void closeClient() {

    }

    @Override
    public void createNode(String path) {

    }

    @Override
    public List<String> getChildrenNode(String path) {
        return null;
    }

    @Override
    public void setNodeData(String path, String data) {

    }

    @Override
    public void deleteNode(String path) {

    }

    @Override
    public void addChildrenNodeWatcher(String path) {

    }

    public boolean existNode(String path){
        return false;
    }

    //====================================== 获取根结点相关方法 ======================================

    /**
     * <h3>获取结点携带的数据</h3>
     */
    public abstract String getNodeData(String path);

    /**
     * <h3>获取结点的属性</h3>
     */
    public abstract Stat getNodeStatus(String path);

    //====================================== 创建结点相关方法 ======================================

    /**
     * <h3>1. 默认创建持久化结点</h3>
     * <h3>2. 结点携带数据</h3>
     */
    public abstract void createNodeWithData(String path, String data);

    /**
     * <h3>1. 指定创建的结点类型</h3>
     * <h3>2. 默认不携带任何数据</h3>
     */
    public abstract void createNodeWithMode(String path, CreateMode mode);

    public abstract void createNode(String path, String data, CreateMode mode);

    //====================================== 更新结点相关方法 ======================================

    /**
     * <h3>根据版本更新结点数据</h3>
     */
    public abstract void setNodeDataWithVersion(String path, String data, int version);

    //====================================== 删除结点相关方法 ======================================

    /**
     * <h3>删除结点后执行回调函数</h3>
     */
    public abstract void deleteNodeCallBack(String path, BackgroundCallback callback);

    //====================================== 监听结点相关方法 ======================================

    public abstract void addNodeWatcher(String path);

}
