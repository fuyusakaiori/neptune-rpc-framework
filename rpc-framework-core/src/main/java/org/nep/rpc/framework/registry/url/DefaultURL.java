package org.nep.rpc.framework.registry.url;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.Separator;

/**
 * <h3>默认的结点格式</h3>
 */
@Slf4j
@Data
@ToString
public class DefaultURL implements NeptuneURL {

    /**
     * <h3>对外提供的接口或者类名称</h3>
     */
    private String serviceName;

    /**
     * <h3>消费者或者提供者的服务名称</h3>
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

    private int weight;

    /**
     * <h3>服务提供者 URL 转为字符串 => 作为数据存储在结点中</h3>
     */
    public String toProviderString(){
        return applicationName + Separator.SEMICOLON
                       + serviceName + Separator.SEMICOLON
                       + address + Separator.SEMICOLON
                       + port + Separator.SEMICOLON
                       + weight + Separator.SEMICOLON
                       + System.currentTimeMillis();
    }

    /**
     * <h3>服务消费者 URL 转为字符串 => 作为数据存储在结点中</h3>
     */
    public String toConsumerString(){
        return applicationName + Separator.SEMICOLON
                + serviceName + Separator.SEMICOLON
                + port + Separator.SEMICOLON
                + System.currentTimeMillis();
    }

    @Override
    public boolean equals(Object source) {
        if (this == source)
            return true;
        if (source == null || getClass() != source.getClass())
            return false;
        DefaultURL url = (DefaultURL) source;
        return port == url.port
                       && serviceName.equals(url.serviceName)
                       && applicationName.equals(url.applicationName)
                       && address.equals(url.address);
    }
}
