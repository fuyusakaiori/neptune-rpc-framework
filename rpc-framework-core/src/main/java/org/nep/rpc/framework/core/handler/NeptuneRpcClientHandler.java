package org.nep.rpc.framework.core.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerFactory;

@Slf4j
public class NeptuneRpcClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        NeptuneRpcProtocol protocol = (NeptuneRpcProtocol) message;
        log.debug("protocol: {}", protocol);
        INeptuneSerializer serializer = NeptuneSerializerFactory.getSerializer(protocol.getSerializer());
        NeptuneRpcInvocation invocation = serializer.deserialize(protocol.getContent(), NeptuneRpcInvocation.class);
        log.debug("invocation: {}", invocation);
        if (NeptuneRpcClientCache.Windows.match(invocation.getUuid()))
            NeptuneRpcClientCache.Windows.put(invocation.getUuid(), invocation);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            ctx.close();
    }
}
