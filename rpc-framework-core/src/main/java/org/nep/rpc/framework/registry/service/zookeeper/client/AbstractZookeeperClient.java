package org.nep.rpc.framework.registry.service.zookeeper.client;

import lombok.Data;
import lombok.ToString;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.nep.rpc.framework.core.common.config.NeptuneRpcRegisterConfig;
import org.nep.rpc.framework.core.common.config.NeptuneRpcServerConfig;

import java.util.List;

/**
 * <h3>zookeeper 客户端模板</h3>
 * <h3>1. 提供子类的基本模板</h3>
 * <h3>2. 封装原生方法</h3>
 */
@Data
@ToString
public abstract class AbstractZookeeperClient {
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

    AbstractZookeeperClient(){
    }

    public AbstractZookeeperClient(NeptuneRpcServerConfig config) {
        if (config == null)
            throw new RuntimeException("[Neptune RPC Server]: 注册中心配置文件为空");
        NeptuneRpcRegisterConfig registerConfig = config.getConfig();
        this.connectString = registerConfig.getRegistry();
        this.connectTime = registerConfig.getConnectTime();
        this.sessionTime = registerConfig.getSessionTime();
        this.retryPolicy = registerConfig.getRetryPolicy();
        this.namespace = registerConfig.getNamespace();
    }

    /**
     * <h3>创建客户端</h3>
     */
    public abstract CuratorFramework getClient();

    /**
     * <h3>关闭客户端</h3>
     */
    public abstract void closeClient();

    /**
     * <h3>获取结点携带的数据</h3>
     */
    public abstract String getNodeData(String path);

    /**
     * <h3>获取所有子结点/h3>
     */
    public abstract List<String> getChildrenNode(String path);

    /**
     * <h3>获取结点的属性</h3>
     */
    public abstract Stat getNodeStatus(String path);

    /**
     * <h3>1. 默认创建持久化结点</h3>
     * <h3>2. 结点不携带任何数据</h3>
     */
    public abstract void createNode(String path);

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

    /**
     * <h3>更新结点数据</h3>
     */
    public abstract void setNodeData(String path, String data);

    /**
     * <h3>根据版本更新结点数据</h3>
     */
    public abstract void setNodeDataWithVersion(String path, String data, int version);

    /**
     * <h3>删除结点</h3>
     */
    public abstract void deleteNode(String path);

    /**
     * <h3>删除结点后执行回调函数</h3>
     */
    public abstract void deleteNodeCallBack(String path, BackgroundCallback callback);

    /**
     * <h3>添加监听器</h3>
     * <h3>1. 监听单个结点</h3>
     * <h3>2. 监听结点的所有子结点</h3>
     * <h3>3. 监听当前结点及其子结点</h3>
     * <h3>4. 标准监听器</h3>
     * <h3>注: CuratorCache 还提供了三种方式去监听, 这里还是按照老方式进行提供</h3>
     */
    public abstract void addNodeWatcher(String path);

    public abstract void addChildrenNodeWatcher(String path);

    public abstract void addTreeNodeWatcher(String path);

    public abstract void addStanderWatcher(String path);

    public abstract boolean existNode(String path);

}
