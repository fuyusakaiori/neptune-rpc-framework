package org.nep.rpc.framework.core.common.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>容器</h3>
 */
@Slf4j
public class NeptuneRpcServerCache {
    // 存放对外提供的接口
    private static final Map<String, Object> SERVER_CACHE = new ConcurrentHashMap<>();

    public static Object getFromCache(String name) {
        if (name == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 不能使用为空的 Key 进行查询");
            return null;
        }
        return SERVER_CACHE.get(name);
    }

    public static void registryInCache(String name, Object obj) {
        if (name == null || obj == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 插入的键值对不能为空");
            return;
        }
        SERVER_CACHE.put(name, obj);
    }

    public static Object removeFromCache(String name) {
        if (name == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 不能使用为空的 Key 进行查询");
            return null;
        }
        return SERVER_CACHE.remove(name);
    }
}
