package org.nep.rpc.framework.core.handler;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponseCode;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.nep.rpc.framework.core.common.constant.Common.PRIMITIVE_TO_WRAPPER;

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
        NeptuneRpcResponse response = new NeptuneRpcResponse();
        // 1. 从解码器中获取到的消息转换成协议的形式
        NeptuneRpcProtocol protocol =  (NeptuneRpcProtocol) message;
        log.debug("message: {}", protocol);
        // 2. TODO 后序需要改进 获取序列化算法
        INeptuneSerializer serializer = NeptuneSerializerFactory.getSerializer(protocol.getSerializer());
        // 3. 取出消息中的消息体, 然后将其反序列化; 暂时采用 json
        NeptuneRpcInvocation invocation = serializer.deserialize(protocol.getContent(), NeptuneRpcInvocation.class);
        log.debug("invocation: {}", invocation);
        // 4. 从服务端容器中取出缓存的接口
        Object target = NeptuneRpcServerCache.getService(invocation.getService());
        // 5. 如果缓存中不存在对应的接口, 那么就直接返回, 并且告诉客户端不存在
        if (target == null){
            response.setUuid(invocation.getUuid());
            response.setCode(NeptuneRpcResponseCode.FAIL.getCode());
            response.setMessage("客户端调用的接口不存在");
            protocol.setContent(serializer.serialize(response));
            ctx.writeAndFlush(protocol);
            log.error("[Neptune RPC Server]: 客户端调用的接口不存在");
            return;
        }
        // 6. 获取目标类中的所有方法
        Method[] methods = target.getClass().getDeclaredMethods();
        // 7. 开始匹配方法然后调用
        Object result = null;
        for (Method method : methods) {
            if(checkMethod(method, invocation)){
                result = method.invoke(target, invocation.getArgs());
                break;
            }
        }
        // 8. 序列化结果写回给客户端; 暂时采用 json
        response.setUuid(invocation.getUuid());
        response.setCode(NeptuneRpcResponseCode.SUCCESS.getCode());
        response.setMessage(NeptuneRpcResponseCode.SUCCESS.getMessage());
        response.setBody(result);
        ctx.writeAndFlush(new NeptuneRpcProtocol(protocol.getProtocolVersion(), protocol.getSerializer(),
                serializer.serialize(response)));
    }

    /**
     * <h3>避免调用重载方法: 暂时的解决方案</h3>
     */
    public boolean checkMethod(Method method, NeptuneRpcInvocation invocation){
        if (!method.getName().equals(invocation.getMethod()))
            return false;
        if (method.getParameterCount() != invocation.getArgs().length)
            return false;
        return !ArrayUtil.equals(invocation.getTypes(), method.getParameterTypes());
    }

    /**
     * <h3>处理连接建立事件</h3>
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.debug("[Neptune Server RPC]: 正在建立连接...");
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
