package org.nep.rpc.framework.core.common.cache;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>容器</h3>
 */
@Slf4j
public class NeptuneRpcBeanCache{
    // 存放对外提供的接口
    private static final Map<String, Object> providerClassMap = new ConcurrentHashMap<>();

    public static Object getBean(String name) {
        return providerClassMap.get(name);
    }

    public static void registryBean(String name, Object obj) {
        providerClassMap.put(name, obj);
    }

    public static void removeBean(String name) {
        providerClassMap.remove(name);
    }
}
