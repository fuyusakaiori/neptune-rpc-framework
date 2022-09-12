package org.nep.rpc.framework.registry.core.server.zookeeper.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.nep.rpc.framework.core.client.NeptuneRpcConnectionHandler;

/**
 * <h3>监听所有子结点</h3>
 */
@Slf4j
public class ChildrenNodeListener implements PathChildrenCacheListener {

    private static final int serviceNameIndex = 1;

    private static final int nodeNameIndex = 3;

    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        // 1. 获取事件类型
        PathChildrenCacheEvent.Type type = event.getType();
        log.info("[neptune rpc zookeeper watcher children listener]: watcher event type - {}", type);
        // 2. 如果是连接初始化事件, 就直接返回不进行任何处理
        if (PathChildrenCacheEvent.Type.INITIALIZED.equals(type)){
            log.warn("[neptune rpc zookeeper watcher]: watcher event type is initialized, don't handle");
            return;
        }
        // 2. 获取发生事件的结点的路径和数据
        String path = event.getData().getPath();
        String data = new String(event.getData().getData());
        log.info("[neptune rpc zookeeper watcher children listener]: watcher node path - {}, node data - {}", path, data);
        // 3. 分割路径获取对应的服务和结点
        String[] split = path.split("/");
        String serviceName = split[serviceNameIndex];
        // 注: nodeName: applicationName:localhost:port
        String nodeName = split[nodeNameIndex];
        // 4. 处理事件
        if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(type)){
            // 5. 如果发生的是新增事件, 那么就调用新增事件处理器
            handleChildAddEvent(serviceName, nodeName);
        }else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(type)){
            // 6. 如果发生的是结点更新事件, 那么就调用更新事件处理器
            handleChildUpdateEvent(serviceName, nodeName, data);
        }else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(type)){
            // 7. 如果发生的是结点删除事件, 那么就调用删除事件处理器
            handleChildRemoveEvent(serviceName, nodeName);
        }else{
            log.info("[neptune rpc zookeeper watcher children listener]: watcher other event type, don't handle");
        }
    }

    /**
     * <h3>处理子结点新增事件: 服务发现</h3>
     */
    private void handleChildAddEvent(String serviceName, String nodeName){
        log.info("[neptune rpc zookeeper watcher children listener]: watcher add event handle start");
        // TODO 建立连接后应该考虑将连接放入缓存中
        NeptuneRpcConnectionHandler.connect(serviceName, nodeName);
        // 注: 新增的子结点不需要去监听, 只需要通过当前这个父结点就可以监听到
        log.info("[neptune rpc zookeeper watcher children listener]: watcher add event handle successfully");
    }

    /**
     * <h3>处理子结点更新事件</h3>
     */
    private void handleChildUpdateEvent(String serviceName, String nodeName, String data){

    }

    /**
     * <h3>处理结点删除事件: 服务下线</h3>
     */
    private void handleChildRemoveEvent(String serviceName, String nodeName){
        log.info("[neptune rpc zookeeper watcher children listener]: watcher remove event handle start");
        NeptuneRpcConnectionHandler.disconnect(serviceName, nodeName);
        log.info("[neptune rpc zookeeper watcher children listener]: watcher remove event handle successfully");
    }
}
