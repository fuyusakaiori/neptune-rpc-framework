package org.nep.rpc.framework.core.client;

import io.netty.channel.ChannelFuture;
import lombok.Data;
import lombok.ToString;

/**
 * <h3>建立连接相关的封装类</h3>
 */
@Data
@ToString
public class NeptuneRpcConnectionWrapper {
    private int port;
    private String address;
    private ChannelFuture future;
}
