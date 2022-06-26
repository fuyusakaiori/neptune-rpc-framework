package org.nep.rpc.framework.registry.url;

import cn.hutool.core.collection.CollectionUtil;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;

/**
 * <h3>默认的结点格式</h3>
 */
@Slf4j
@Data
@ToString
public class DefaultURL implements URL {

    public static final String SEPERATOR = ";";
    /**
     * <h3>服务名称</h3>
     */
    private String applicationName;

    /**
     * <h3>调用的接口名称</h3>
     */
    private String serviceName;

    /**
     * <h3>服务其它参数: </h3>
     * <h3>1. 服务 IP 地址</h3>
     * <h3>2. 服务端口号</h3>
     * <h3>3. 服务权重</h3>
     * <h3>4. ...</h3>
     */
    private Map<String, Object> params;

    public DefaultURL(String applicationName, String serviceName) {
        this(applicationName, serviceName, Collections.emptyMap());
    }

    public DefaultURL(String applicationName, String serviceName, Map<String, Object> params) {
        this.applicationName = applicationName;
        this.serviceName = serviceName;
        this.params = params;
    }

    /**
     * <h3>URL 转换为提供者的结点名称</h3>
     */
    public String toProviderString(){
        // 1. 获取服务的端口号和 IP 地址
        String host = String.valueOf(params.get("host"));
        String port = String.valueOf(params.get("port"));
        // 2. 转换为字符串 为什么这里用 StringBuilder 会提示换成 String?
        return applicationName + SEPERATOR
                       + serviceName + SEPERATOR
                       + host + SEPERATOR
                       + port + SEPERATOR
                       + System.currentTimeMillis();
    }

    /**
     * <h3>URL 转换为消费者的结点名称</h3>
     */
    public String toConsumerString(){
        // 1. 只需要获取端口号 为什么不需要 IP 地址呢?
        String port = String.valueOf(params.get("port"));
        // 2. 转换成字符串
        return applicationName + SEPERATOR
                + serviceName + SEPERATOR
                + port + SEPERATOR
                + System.currentTimeMillis();
    }

    public void addParameter(String key, Object value){
        params.putIfAbsent(key, value);
    }

}
