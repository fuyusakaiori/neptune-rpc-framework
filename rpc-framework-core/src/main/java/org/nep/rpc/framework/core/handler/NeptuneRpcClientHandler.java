package org.nep.rpc.framework.core.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocal.NeptuneRpcProtocol;

import static org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache.RESPONSE_CACHE;

@Slf4j
public class NeptuneRpcClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        NeptuneRpcProtocol protocol = (NeptuneRpcProtocol) message;
        log.debug("protocol: {}", protocol);
        NeptuneRpcInvocation invocation = JSON.parseObject(protocol.getContent(), NeptuneRpcInvocation.class);
        log.debug("invocation: {}", invocation);
        if (!RESPONSE_CACHE.containsKey(invocation.getUuid())){
            log.error("[Neptune RPC Client]: 服务端响应错误");
            return;
        }
        RESPONSE_CACHE.put(invocation.getUuid(), invocation);
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (ctx.channel().isActive())
            ctx.close();
    }
}
