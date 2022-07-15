package org.nep.rpc.framework.core.serialize.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.io.IOException;

@Slf4j
public class JackSonSerializer implements INeptuneSerializer {

    private static final ThreadLocal<ObjectMapper> MAPPER_LOCAL =
            ThreadLocal.withInitial(ObjectMapper::new);

    @Override
    public byte[] serialize(Object source) {
        try {
            return MAPPER_LOCAL.get().writeValueAsBytes(source);
        } catch (JsonProcessingException e) {
            log.error("[Neptune RPC Serialize]: {}", "Jackson 序列化异常");
        }
        return null;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        try {
            return MAPPER_LOCAL.get().readValue(source, clazz);
        } catch (IOException e) {
            log.error("[Neptune RPC Serialize]: {}", "Jackson 反序列化异常");
        }
        return null;
    }
}
