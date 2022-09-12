package org.nep.rpc.framework.core.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.filter.chain.NeptuneClientFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientDirectInvokerFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientGroupFilter;
import org.nep.rpc.framework.core.filter.client.NeptuneClientLogFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

@Slf4j
public class NeptuneRpcClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        // 1. 将读取到的对象强制转换
        NeptuneRpcProtocol protocol = (NeptuneRpcProtocol) message;
        // 2. 使用自己的序列化算法进行反序列化而不是去查询
        INeptuneSerializer serializer = PropertyBootStrap.getClientConfig().getSerializer();
        // 3. 反序列化: 如果服务端和客户端的序列化算法不匹配, 那么就会反序列化失败
        NeptuneRpcResponse response = serializer.deserialize(protocol.getContent(), NeptuneRpcResponse.class);
        log.info("[neptune rpc client handler]: handle message - {}", response);
        // 4. 如果反序列化后的响应为空, 直接抛出异常
        if (response == null)
            throw new RuntimeException("[neptune rpc client handler]: handle message is null");
        NeptuneRpcClientCache.Windows.put(response.getUuid(), response);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            ctx.close();
    }
}
