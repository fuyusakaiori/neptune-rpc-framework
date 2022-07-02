package org.nep.rpc.framework.core.serialize.protobuf;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

/**
 * <h3>Protobuf 序列化</h3>
 * TODO <h3>使用起来比较麻烦, 之后进行补充</h3>
 */
@Slf4j
public class ProtobufSerializer implements INeptuneSerializer
{
    @Override
    public byte[] serialize(Object source)
    {
        return new byte[0];
    }

    @Override
    public Object deserialize(byte[] source)
    {
        return null;
    }
}
