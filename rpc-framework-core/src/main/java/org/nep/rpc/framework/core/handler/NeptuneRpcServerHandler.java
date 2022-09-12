package org.nep.rpc.framework.core.handler;

import cn.hutool.core.util.ArrayUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.filter.server.NeptuneServerLogFilter;
import org.nep.rpc.framework.core.filter.server.NeptuneTokenFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponse;
import org.nep.rpc.framework.core.protocol.NeptuneRpcResponseCode;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerType;
import org.nep.rpc.framework.core.server.NeptuneServiceWrapper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * <h3>Neptune RPC 服务器消息处理器</h3>
 * <h3>注: 服务器和客户端通信仅采用固定的协议通信, 所以只有单个处理器</h3>
 * TODO 考虑之后扩展多种格式的协议
 */
@Slf4j
public class NeptuneRpcServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * <h3>服务端过滤链</h3>
     */
    private static final NeptuneServerFilter filter = new NeptuneServerLogFilter()
                         .setNextFilter(new NeptuneTokenFilter());


    /**
     * <h3>处理器读事件</h3>
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception {
        NeptuneRpcResponse response = new NeptuneRpcResponse();
        // 1. 从解码器中获取到的消息转换成协议的形式
        NeptuneRpcProtocol protocol =  (NeptuneRpcProtocol) message;
        log.debug("[neptune rpc server handler]: handle message - {}", protocol);
        // 2. 获取序列化算法
        INeptuneSerializer serializer = PropertyBootStrap.getServerConfig().getSerializer();
        if (protocol.getSerializer() != NeptuneSerializerType.getSerializerCode(serializer)){
            log.error("[neptune rpc server handler]: server's serializer is not same with client's serializer");
            throw new RuntimeException("[neptune rpc server handler]: server's serializer is not same with client's serializer");
        }
        // 3. 取出消息中的消息体, 然后将其反序列化
        NeptuneRpcInvocation invocation = serializer.deserialize(protocol.getContent(), NeptuneRpcInvocation.class);
        log.debug("[neptune rpc server handler]: handle message deserialize - {}", invocation);
        // 注: 调用过滤链处理客户端的请求
        filter.execute(invocation);
        // 4. 从服务端容器中取出缓存的接口
        NeptuneServiceWrapper wrapper = NeptuneRpcServerCache.Service.getService(invocation.getServiceName());
        // 5. 如果缓存中不存在对应的接口, 那么就直接返回, 并且告诉客户端不存在
        if (Objects.isNull(wrapper) || Objects.isNull(wrapper.getService())){
            response.setUuid(invocation.getUuid());
            response.setCode(NeptuneRpcResponseCode.FAIL.getCode());
            response.setMessage(NeptuneRpcResponseCode.FAIL.getMessage());
            protocol.setContent(serializer.serialize(response));
            ctx.writeAndFlush(protocol);
            log.error("[neptune rpc server handler]: client call service is null");
            return;
        }
        Object target = wrapper.getService();
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
        // 8. 序列化结果写回给客户端
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
        // 1. 检查调用的方法名和当前方法名是否相同
        if (!method.getName().equals(invocation.getMethodName()))
            return false;
        // 2. 检查调用的方法参数数量和当前方法的参数数量是否相同
        if (method.getParameterCount() != invocation.getArgs().length)
            return false;
        // 3. 检查调用的方法参数是否当前方法的方法参数类型一致
        return ArrayUtil.equals(invocation.getTypes(), method.getParameterTypes());
    }

    /**
     * <h3>处理连接建立事件</h3>
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("[neptune rpc server]: server is handle connect event...");
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
