package org.nep.rpc.framework.registry.listener;

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

    }
}
