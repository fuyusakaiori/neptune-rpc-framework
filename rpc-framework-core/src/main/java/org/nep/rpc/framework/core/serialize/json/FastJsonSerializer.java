package org.nep.rpc.framework.core.serialize.json;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.nio.charset.StandardCharsets;

/**
 * <h3>FastJson 序列化</h3>
 */
@Slf4j
public class FastJsonSerializer implements INeptuneSerializer
{
    @Override
    public byte[] serialize(Object source) {
        return JSON.toJSONString(source).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deserialize(byte[] source) {
        return JSON.parseObject(new String(source));
    }
}
