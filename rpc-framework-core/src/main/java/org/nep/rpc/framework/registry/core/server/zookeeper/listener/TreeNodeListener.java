package org.nep.rpc.framework.registry.core.server.zookeeper.listener;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

/**
 * <h3>监听根结点和子结点</h3>
 */
public class TreeNodeListener implements TreeCacheListener {
    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception
    {
        if (TreeCacheEvent.Type.NODE_ADDED.equals(event.getType())){

        }else if (TreeCacheEvent.Type.NODE_UPDATED.equals(event.getType())){

        }else if (TreeCacheEvent.Type.NODE_REMOVED.equals(event.getType())){

        }else{

        }
    }
}
