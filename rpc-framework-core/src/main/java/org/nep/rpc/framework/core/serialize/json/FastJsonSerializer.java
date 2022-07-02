package org.nep.rpc.framework.core.serialize.json;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.nio.charset.StandardCharsets;

/**
 * <h3>FastJson 序列化</h3>
 */
@Slf4j
public class FastJsonSerializer implements INeptuneSerializer {
    @Override
    public <T> byte[] serialize(T source) {
        log.debug("source: {}", source);
        return JSON.toJSONString(source).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        String message = new String(source, StandardCharsets.UTF_8);
        log.debug("message: {}", message);
        return JSON.parseObject(message, clazz);
    }
}
