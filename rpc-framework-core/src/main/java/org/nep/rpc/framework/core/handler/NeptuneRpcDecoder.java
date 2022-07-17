package org.nep.rpc.framework.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.Protocol;
import org.nep.rpc.framework.core.protocol.NeptuneRpcProtocol;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <h3>Neptune RPC 解码器</h3>
 * <h3>1. 按照写入缓冲区的顺序从缓冲区中读取出来</h3>
 * TODO <h3>2. 如果魔数字段或者版本字段之类的不合理, 那么应该要进行处理</h3>
 */
@Slf4j
public class NeptuneRpcDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        // 1. 读取魔数字段 7B
        String magicNumber = buf.readBytes(Protocol.MAGIC_NUMBER.length()).toString(StandardCharsets.UTF_8);
        // 2. 读取协议版本号 1B
        byte protocolVersion = buf.readByte();
        // 3. 读取采用的序列化算法类型 1B
        byte serializer = buf.readByte();
        // 4. 读取消息体的长度字段 4B
        int contentLength = buf.readInt();
        // 5. 读取正文消息体
        byte[] content = new byte[contentLength];
        buf.readBytes(content, 0, contentLength);
        // 6. 将从缓冲区中接收到的数据填充到协议中, 然后交给服务器处理器进行处理; 不在这里进行反序列化
        out.add(new NeptuneRpcProtocol(protocolVersion, serializer, content));
    }
}
