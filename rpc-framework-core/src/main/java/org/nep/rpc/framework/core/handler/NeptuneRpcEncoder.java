package org.nep.rpc.framework.core.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.protocal.NeptuneRpcProtocol;

import java.nio.charset.StandardCharsets;

/**
 * <h3>Neptune RPC 编码器</h3>
 * <h3>1. MessageToByteEncoder: 将消息体转换为字节数据</h3>
 * <h3>2. NeptuneRpcProtocol: 编码器接收到这个消息体之后就会将其转换为字节数据</h3>
 */
@Slf4j
public class NeptuneRpcEncoder extends MessageToByteEncoder<NeptuneRpcProtocol> {

    @Override
    protected void encode(ChannelHandlerContext ctx, NeptuneRpcProtocol message, ByteBuf buf) throws Exception {
        log.debug("[Neptune RPC Server]: 服务器正在对消息进行编码 {}", message);
        log(buf);
        buf.writeBytes(message.getMagicNumber().getBytes(StandardCharsets.UTF_8));
        buf.writeByte(message.getProtocolVersion());
        buf.writeByte(message.getSerializer());
        buf.writeInt(message.getContentLength());
        buf.writeBytes(message.getContent());
        log(buf);
    }

    private static void log(ByteBuf buf){
        int length = buf.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0: 1) + 4;
        StringBuilder message = new StringBuilder(rows * 80 * 2)
                                        .append("read index").append(buf.readerIndex())
                                        .append("write index").append(buf.writerIndex())
                                        .append(" capacity").append(buf.capacity())
                                        .append(StringUtil.NEWLINE);
        ByteBufUtil.appendPrettyHexDump(message, buf);
        System.out.println(message);
    }
}
