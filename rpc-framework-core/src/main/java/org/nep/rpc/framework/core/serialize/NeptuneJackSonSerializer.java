package org.nep.rpc.framework.core.serialize;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class NeptuneJackSonSerializer implements INeptuneSerializer {

    private static final ThreadLocal<ObjectMapper> MAPPER_LOCAL =
            ThreadLocal.withInitial(ObjectMapper::new);

    @Override
    public byte[] serialize(Object source) {
        log.info("[neptune rpc serializer]: jackson serialize start");
        try {
            byte[] target = MAPPER_LOCAL.get().writeValueAsBytes(source);
            log.info("[neptune rpc serializer]: jackson serialize end");
            return target;
        } catch (JsonProcessingException e) {
            log.error("[neptune rpc serializer]: jackson serialize occurred error", e);
            return null;
        }
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        log.info("[neptune rpc serializer]: jackson deserialize start");
        try {
            T target = MAPPER_LOCAL.get().readValue(source, clazz);
            log.info("[neptune rpc serializer]: jackson deserialize end");
            return target;
        } catch (IOException e) {
            log.error("[neptune rpc serializer]: jackson deserialize occurred error", e);
            return null;
        }
    }
}
