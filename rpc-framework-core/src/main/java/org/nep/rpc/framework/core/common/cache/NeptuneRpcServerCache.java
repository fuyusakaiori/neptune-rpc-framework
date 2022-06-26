package org.nep.rpc.framework.core.common.cache;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.registry.url.URL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>容器</h3>
 */
@Slf4j
public class NeptuneRpcServerCache {
    // 存放对外提供的接口
    private static final Map<String, Object> SERVER_CACHE = new ConcurrentHashMap<>();

    // 存储已经注册的服务的结点地址; 1.需要考虑并发吗? 2.为什么需要存储在本地?
    private static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    public static boolean removeFromCache(URL url){
        if (url == null)
            return false;
        return PROVIDER_URL_SET.remove(url);
    }

    public static void registerInCache(URL url){
        if (url == null)
            return;
        PROVIDER_URL_SET.add(url);
    }

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
