package org.nep.rpc.framework.core.common.resource;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>获取配置文件中的内容</h3>
 */
@Slf4j
public class PropertiesLoader {
    private static final Properties properties = new Properties();
    // 缓存键值对
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    // 配置文件路径
    private static final String serverPath = "neptune-server.properties";
    private static final String clientPath = "neptune-client.properties";

    private static ClassLoader getClassLoader(){
        return PropertiesLoader.class.getClassLoader();
    }

    /**
     * <h3>加载配置文件</h3>
     */
    public static void loadConfiguration(){
        try {
            ClassLoader classLoader = getClassLoader();
            properties.load(classLoader.getResourceAsStream(serverPath));
            properties.load(classLoader.getResourceAsStream(clientPath));
        } catch (IOException e) {
            throw new RuntimeException("[Neptune RPC PropertiesLoader]: 配置文件加载出现异常", e);
        }
    }

    /**
     * <h3>获取字符串</h3>
     */
    public static String getStringValue(String key){
        if (cache.containsKey(key))
            return cache.get(key);
        String value = properties.getProperty(key);
        if (value != null)
            cache.put(key, value);
        return value;
    }

    /**
     * <h3>获取整数</h3>
     */
    public static Integer getIntegerValue(String key){
        if (cache.containsKey(key))
            return Integer.parseInt(cache.get(key));
        String value = properties.getProperty(key);
        if (value == null)
            return null;
        cache.put(key, value);
        return Integer.valueOf(value);
    }

}
