package org.nep.rpc.framework.core.handler;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcBeanCache;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocal.NeptuneRpcProtocol;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

/**
 * <h3>Neptune RPC 服务器消息处理器</h3>
 * <h3>注: 服务器和客户端通信仅采用固定的协议通信, 所以只有单个处理器</h3>
 * TODO 考虑之后扩展多种格式的协议
 */
@Slf4j
public class NeptuneRpcServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * <h3>处理器读事件</h3>
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        // 1. 从解码器中获取到的消息转换成协议的形式
        NeptuneRpcProtocol protocol =  (NeptuneRpcProtocol) message;
        log.debug("message: {}", protocol);
        // 2. 取出消息中的消息体, 然后将其反序列化; 暂时采用 json
        NeptuneRpcInvocation invocation = JSON.parseObject(new String(protocol.getContent()), NeptuneRpcInvocation.class);
        log.debug("invocation: {}", invocation);
        // 3. 从服务端容器中取出缓存的接口
        Object target = NeptuneRpcBeanCache.getBean(invocation.getTargetClass());
        log.debug("target: {}", target);
        // 4. 获取目标类中的所有方法
        Method[] methods = target.getClass().getDeclaredMethods();
        log.debug("methods: {}", methods.length);
        // 5. 开始匹配方法然后调用
        Object result = null;
        for (Method method : methods) {
            // 注: 如果只比较方法名, 那么很有可能出现方法重载的情况, 最后导致方法调错
            if(method.getName().equals(invocation.getTargetMethod())){
                result = method.invoke(target, invocation.getArgs());
                break;
            }
        }
        // 6. 序列化结果写回给客户端; 暂时采用 json
        invocation.setResponse(result);
        ctx.writeAndFlush(new NeptuneRpcProtocol(protocol.getProtocolVersion(), protocol.getSerializer(),
                JSON.toJSONString(invocation).getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * <h3>处理连接建立事件</h3>
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("[Neptune RPC]: 正在建立连接...");
        super.channelActive(ctx);
    }

    /**
     * <h3>处理异常事件</h3>
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        // 如果这个连接已经建立了, 那么就断开
        if (channel.isActive())
            ctx.close();
    }
}
