package org.nep.rpc.framework.core.common.resource;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>获取配置文件中的内容</h3>
 */
@Slf4j
public class PropertiesLoader {
    // 负责加载配置文件
    private static final Properties properties = new Properties();
    // 缓存键值对
    private static final Map<String, String> cache = new ConcurrentHashMap<>();
    // 配置文件路径
    private static final String path = "neptune.properties";

    /**
     * <h3>加载配置文件</h3>
     * <h3>注: 采用懒加载的方式</h3>
     */
    public static void loadConfiguration(){
        try {
            properties.load(PropertiesLoader.class.getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            log.error("[Neptune RPC Configuration]: 加载配置文件的过程出错", e);
        }
    }

    /**
     * <h3>获取字符串 value</h3>
     */
    public static String getString(String key){
        if (cache.containsKey(key))
            return cache.get(key);
        String value = properties.getProperty(key);
        if (value != null)
            return cache.put(key, value);
        return null;
    }

    /**
     * <h3>获取整数</h3>
     */
    public static Integer getInt(String key){
        if (cache.containsKey(key))
            return Integer.parseInt(cache.get(key));
        String value = properties.getProperty(key);
        if (value == null)
            return null;
        cache.put(key, value);
        return Integer.valueOf(value);
    }


}
