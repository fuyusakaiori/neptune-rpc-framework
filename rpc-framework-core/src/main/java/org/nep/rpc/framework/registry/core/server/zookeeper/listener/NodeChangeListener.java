package org.nep.rpc.framework.registry.core.server.zookeeper.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCacheListenerBuilder;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.constant.Separator;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <h3>监听根结点</h3>
 */
@Slf4j
public class NodeChangeListener implements CuratorCacheListenerBuilder.ChangeListener {
    @Override
    public void event(ChildData old, ChildData cur) {
        // 1. 获取变更后的数据和路径
        String path = cur.getPath();
        String curData = new String(cur.getData(), StandardCharsets.UTF_8);
        log.debug("path: {}, cur: {}", path, curData);
        // 2. 根据服务获取所有的提供者
        String[] split = path.split(Separator.SLASH);
        String service = split[1];
        String ipAndPort = split[3];
        List<NeptuneRpcInvoker> providers = NeptuneRpcClientCache.Connection.providers(service);
        log.debug("service: {}, providers: {}", service, providers);
        // 3. 判断哪个是需要更新的
        for (NeptuneRpcInvoker provider : providers) {
            if (ipAndPort.equals(provider.getAddress() + Separator.COLON + provider.getPort())){
                provider.setFixedWeight(Integer.parseInt(curData.split(Separator.SEMICOLON)[5]));
            }
            // TODO 有待考虑
            provider.setDynamicWeight(provider.getFixedWeight());
        }

    }
}
