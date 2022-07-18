package org.nep.rpc.framework.registry.core.client;

import org.apache.curator.framework.CuratorFramework;

import java.util.List;

public interface NeptuneRegistryClient {

    /**
     * <h3>获取客户端</h3>
     */
    CuratorFramework getClient();

    /**
     * <h3>关闭客户端</h3>
     */
    void closeClient();

    /**
     * <h3>创建结点</h3>
     */
    void createNode(String path);

    /**
     * <h3>获取所有子结点</h3>
     */
    List<String> getChildrenNode(String path);

    /**
     * <h3>更新结点</h3>
     */
    void setNodeData(String path, String data);

    /**
     * <h3>删除结点</h3>
     */
    void deleteNode(String path);

    /**
     * <h3>检查结点</h3>
     */
    boolean existNode(String path);

    /**
     * <h3>监听子结点</h3>
     */
    void addChildrenNodeWatcher(String path);

}
