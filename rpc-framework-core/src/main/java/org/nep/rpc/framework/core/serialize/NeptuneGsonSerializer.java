package org.nep.rpc.framework.core.serialize;

import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/**
 * <h3>FastJson 序列化</h3>
 */
@Slf4j
public class NeptuneGsonSerializer implements INeptuneSerializer {

    private static final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(()-> new GsonBuilder()
                   .registerTypeAdapter(Class.class, new ClassSerializer())
                   .create());

    @Override
    public byte[] serialize(Object source) {
        log.info("[neptune rpc serializer]: gson serialize start");
        byte[] target = GSON_LOCAL.get().toJson(source).getBytes(StandardCharsets.UTF_8);
        log.info("[neptune rpc serializer]: gson serialize end");
        return target;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        log.info("[neptune rpc serializer]: gson deserialize start");
        T target = GSON_LOCAL.get().fromJson(new String(source, StandardCharsets.UTF_8), clazz);
        log.info("[neptune rpc serializer]: gson deserialize end");
        return target;
    }

    private static class ClassSerializer implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>>{
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            Class<?> type = null;
            try {
                type = Class.forName(jsonElement.getAsString());
                return type;
            } catch (ClassNotFoundException e) {
                log.error("[neptune rpc serializer]: gson deserialize occurred error" , e);
                return null;
            }
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }
}
