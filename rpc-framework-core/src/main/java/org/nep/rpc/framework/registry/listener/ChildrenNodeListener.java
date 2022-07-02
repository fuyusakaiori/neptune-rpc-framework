package org.nep.rpc.framework.registry.listener;

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



    @Override
    public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        PathChildrenCacheEvent.Type type = event.getType();
        log.debug("[Neptune RPC Zookeeper]: 处理的事件类型: {}", type);
        if (!PathChildrenCacheEvent.Type.INITIALIZED.equals(type)){
            // 1. 获取新增的结点的路径和数据
            String path = event.getData().getPath();
            String data = new String(event.getData().getData());
            log.debug("path: {}, data: {}", path, data);
            // 2. 客户端和服务器建立连接
            String[] split = path.split("/");
            String service = split[1];
            String ipAndPort = split[3];
            // 3. 处理事件
            if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(type)){
                handleChildAddEvent(service, ipAndPort);
            }else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(type)){
                handleChildUpdateEvent(service, ipAndPort, data);
            }else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(type)){
                handleChildRemoveEvent(service, ipAndPort);
            }else{
                log.info("[Neptune RPC Zookeeper]: 可能发生连接断开、连接重连、建立连接三种类型的事件, 不做任何处理");
            }
        }
    }

    /**
     * <h3>处理子结点新增事件: 服务发现</h3>
     */
    private void handleChildAddEvent(String service, String ipAndPort){
        NeptuneRpcConnectionHandler.connect(service, ipAndPort);
        // 注: 新增的子结点不需要去监听, 只需要通过当前这个父结点就可以监听到
        log.debug("[Neptune RPC Zookeeper]: 子结点新增事件处理完成");
    }

    /**
     * <h3>处理子结点更新事件</h3>
     */
    private void handleChildUpdateEvent(String service, String ipAndPort, String data){

    }

    /**
     * <h3>处理结点删除事件: 服务下线</h3>
     */
    private void handleChildRemoveEvent(String service, String ipAndPort){
        NeptuneRpcConnectionHandler.disconnect(service, ipAndPort);
        log.debug("[Neptune RPC Zookeeper]: 子结点删除事件处理完成");
    }
}
