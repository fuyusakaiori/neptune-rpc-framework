package org.nep.rpc.framework.registry.url;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * <h3>默认的结点格式</h3>
 */
@Slf4j
@Data
@ToString
public class DefaultURL implements URL {

    public static final String SEMICOLON = ";";

    /**
     * <h3>注册的服务名称</h3>
     */
    private String serviceName;

    /**
     * <h3>调用服务的客户端名称; 服务提供者的 {@code applicationName} 和 {@code serviceName} 相同</h3>
     */
    private String applicationName;

    private String address;

    private String port;

    /**
     * <h3>服务其它参数: </h3>
     * <h3>1. 服务权重</h3>
     * <h3>2. ...</h3>
     */
    private Map<String, Object> parameters;

    /**
     * <h3>服务提供者 URL 转为字符串 => 作为数据存储在结点中</h3>
     */
    public String toProviderString(){
        return applicationName + SEMICOLON
                       + serviceName + SEMICOLON
                       + address + SEMICOLON
                       + port + SEMICOLON
                       + System.currentTimeMillis();
    }

    /**
     * <h3>服务消费者 URL 转为字符串 => 作为数据存储在结点中</h3>
     */
    public String toConsumerString(){
        return applicationName + SEMICOLON
                + serviceName + SEMICOLON
                + port + SEMICOLON
                + System.currentTimeMillis();
    }

    public void addParameter(String key, Object value){
        parameters.putIfAbsent(key, value);
    }

}
