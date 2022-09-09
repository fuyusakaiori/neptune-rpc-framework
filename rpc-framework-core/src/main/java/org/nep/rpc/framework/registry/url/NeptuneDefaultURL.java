package org.nep.rpc.framework.registry.url;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.Separator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>默认的结点格式</h3>
 */
@Slf4j
@Data
public class NeptuneDefaultURL implements NeptuneURL {

    /**
     * <h3>提供的服务名称（接口名称）</h3>
     */
    private String serviceName;

    /**
     * <h3>提供服务的系统的名称（服务提供者的名字）</h3>
     */
    private String applicationName;
    /**
     * <h3>IP 地址</h3>
     */
    private String address;
    /**
     * <h3>端口号</h3>
     */
    private int port;
    /**
     * <h3>权重</h3>
     */
    private int weight;

    /**
     * <h3>附加参数</h3>
     */
    private final Map<String, Object> params = new ConcurrentHashMap<>();

    /**
     * <h3>消费者和提供者的路径对象转换为字符串数据</h3>
     */
    public String toString(){
        return applicationName + Separator.SEMICOLON
                       + serviceName + Separator.SEMICOLON
                       + address + Separator.SEMICOLON
                       + port + Separator.SEMICOLON
                       + weight + Separator.SEMICOLON
                       + System.currentTimeMillis();
    }

    /**
     * <h3>消费者和提供者的路径对象转换为字符串路径</h3>
     */
    public String toString(String role){
        return Separator.SLASH + serviceName + role + Separator.SLASH
                       + applicationName + Separator.COLON + address + Separator.COLON + port;
    }

    @Override
    public boolean equals(Object source) {
        if (this == source)
            return true;
        if (source == null || getClass() != source.getClass())
            return false;
        NeptuneDefaultURL url = (NeptuneDefaultURL) source;
        return port == url.port
                       && serviceName.equals(url.serviceName)
                       && applicationName.equals(url.applicationName)
                       && address.equals(url.address);
    }
}
