package org.nep.rpc.framework.core.common.cache;

import cn.hutool.core.collection.CollectionUtil;
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
    // 本服务器提供的所有服务: key: 服务名 value: 服务对应的实体类
    private static final Map<String, Object> SERVICES = new ConcurrentHashMap<>();

    // 存储已经注册的服务的结点地址: 此后采用异步线程将集合中的地址全部注册到注册中心去
    private static final Set<URL> SERVICE_URLS = new HashSet<>();

    public static boolean hasServicesUrl(){
        return CollectionUtil.isNotEmpty(SERVICE_URLS);
    }

    public static Set<URL> getServiceUrls(){
        return SERVICE_URLS;
    }

    public static void cancelServiceUrl(URL url){
        if (url == null){
            log.error("[Neptune RPC Server]: 需要下线的服务地址不存在");
            return;
        }
        SERVICE_URLS.remove(url);
    }

    public static void registerServiceUrl(URL url){
        if (url == null){
            log.error("[Neptune RPC Server]: 需要注册的服务地址不可以为空");
            return;
        }
        SERVICE_URLS.add(url);
    }

    public static Object getService(String serviceName) {
        if (serviceName == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 不能使用为空的 Key 进行查询");
            return null;
        }
        return SERVICES.get(serviceName);
    }

    public static void registerService(String serviceName, Object service) {
        if (serviceName == null || service == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 插入的键值对不能为空");
            return;
        }
        SERVICES.put(serviceName, service);
    }

    public static Object cancelService(String serviceName) {
        if (serviceName == null){
            log.error("[Neptune RPC Server]: ConcurrentHashMap 不能使用为空的 Key 进行查询");
            return null;
        }
        return SERVICES.remove(serviceName);
    }
}
