package org.nep.rpc.framework.registry.core.server.zookeeper.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListenerBuilder;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.constant.Separator;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * <h3>监听根结点</h3>
 */
@Slf4j
public class NodeChangeListener implements CuratorCacheListenerBuilder.ChangeListener {

    private static final int serviceNameIndex = 1;

    private static final int nodeNameIndex = 3;

    @Override
    public void event(ChildData old, ChildData cur) {
        // 1. 获取变更后的数据和路径
        String path = cur.getPath();
        String curData = new String(cur.getData(), StandardCharsets.UTF_8);
        // 2. 根据服务获取所有的提供者
        String[] split = path.split(Separator.SLASH);
        String serviceName = split[serviceNameIndex];
        String nodeName = split[nodeNameIndex];
        if (StrUtil.isEmpty(serviceName) || StrUtil.isEmpty(nodeName)){
            log.error("[neptune rpc zookeeper watcher node listener] service name or node name is null");
            return;
        }
        List<NeptuneRpcInvoker> providers = NeptuneRpcClientCache.Connection.providers(serviceName);
        if (CollectionUtil.isEmpty(providers)){
            log.error("[neptune rpc zookeeper watcher node listener] provider list is empty");
            return;
        }
        // 3. 判断哪个是需要更新的
        for (NeptuneRpcInvoker provider : providers) {
            if (nodeName.equals(provider.getAddress() + Separator.COLON + provider.getPort())){
                provider.setFixedWeight(Integer.parseInt(curData.split(Separator.SEMICOLON)[5]));
            }
            provider.setDynamicWeight(provider.getFixedWeight());
        }

    }
}
