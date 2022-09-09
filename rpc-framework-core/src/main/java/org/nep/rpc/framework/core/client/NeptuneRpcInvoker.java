package org.nep.rpc.framework.core.client;

import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.nep.rpc.framework.registry.url.NeptuneURL;

/**
 * <h3>建立连接相关的封装类</h3>
 */
@Data
@ToString
@Accessors(chain = true)
public class NeptuneRpcInvoker {

    /**
     * <h3>调用的服务的 URL</h3>
     */
    private NeptuneURL url;

    /**
     * <h3>调用的服务器的名称</h3>
     */
    private String applicationName;

    /**
     * <h3>调用的服务名</h3>
     */
    private String serviceName;

    /**
     * <h3>调用的服务器端口号</h3>
     */
    private int port;
    /**
     * <h3>调用的服务器的 IP 地址</h3>
     */
    private String address;

    /**
     * <h3>调用的服务器的固定权重</h3>
     */
    private int fixedWeight;
    /**
     * <h3>调用的服务器的动态权重</h3>
     */
    private int dynamicWeight;

    /**
     * <h3>调用的服务器所属分组</h3>
     */
    private String group;

    private ChannelFuture future;
}
