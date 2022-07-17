package org.nep.rpc.framework.core.protocol;

import lombok.Data;
import lombok.ToString;
import org.nep.rpc.framework.core.serialize.NeptuneSerializerType;

import java.io.Serializable;

import static org.nep.rpc.framework.core.common.constant.Protocol.*;

/**
 * <h3>Neptune RPC 请求协议体/h3>
 * <h3>协议 = 魔数 + 版本号 + 序列化算法 + 消息类型 + 请求序列号 + 消息长度 + 消息正文</h3>
 */
@Data
@ToString
public class NeptuneRpcProtocol implements Serializable {

    private static final long serialVersionUID = 5359096060555795690L;

    // 1. 魔数 7B
    private String magicNumber;
    // 2. 版本号 1B
    private byte protocolVersion;
    // 3. 序列化算法 1B
    private byte serializer;
    // 5. 消息体长度
    private int contentLength;
    // 6. 消息体 => 内容利用 NeptuneRpcInvocation 保存
    private byte[] content;

    public NeptuneRpcProtocol(byte[] content) {
        this(PROTOCOL_VERSION, NeptuneSerializerType.SERIALIZER_KRYO.getCode(), content);
    }

    public NeptuneRpcProtocol(byte[] content, byte serializer){
        this(PROTOCOL_VERSION, serializer, content);
    }

    public NeptuneRpcProtocol(byte protocolVersion, byte[] content){
        this(protocolVersion, NeptuneSerializerType.SERIALIZER_KRYO.getCode(), content);
    }

    public NeptuneRpcProtocol(byte protocolVersion, byte serializer, byte[] content) {
        this.magicNumber = MAGIC_NUMBER;
        this.protocolVersion = protocolVersion;
        this.serializer = serializer;
        this.contentLength = content.length;
        this.content = content;
    }

}
