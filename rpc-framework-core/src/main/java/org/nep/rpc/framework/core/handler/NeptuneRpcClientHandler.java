package org.nep.rpc.framework.core.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponseCode;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerFactory;

@Slf4j
public class NeptuneRpcClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        NeptuneRpcProtocol protocol = (NeptuneRpcProtocol) message;
        INeptuneSerializer serializer = NeptuneSerializerFactory.getSerializer(protocol.getSerializer());
        NeptuneRpcResponse response = serializer.deserialize(protocol.getContent(), NeptuneRpcResponse.class);
        log.info("response: {}", response);
        if (response == null)
            throw new RuntimeException("[Neptune RPC Client]: 没有收到响应消息");
        NeptuneRpcClientCache.Windows.put(response.getUuid(), response);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            ctx.close();
    }
}
