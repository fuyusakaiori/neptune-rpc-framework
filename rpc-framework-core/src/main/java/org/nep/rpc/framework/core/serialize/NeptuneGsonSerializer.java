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

    private static final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(()->{
        return new GsonBuilder()
                       .registerTypeAdapter(Class.class, new ClassSerializer())
                       .create();
    });

    @Override
    public byte[] serialize(Object source) {
        return GSON_LOCAL.get().toJson(source).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        return GSON_LOCAL.get().fromJson(new String(source, StandardCharsets.UTF_8), clazz);
    }

    private static class ClassSerializer implements JsonSerializer<Class<?>>, JsonDeserializer<Class<?>>{
        @Override
        public Class<?> deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Class<?> type = null;
            try {
                type = Class.forName(jsonElement.getAsString());
            } catch (ClassNotFoundException e) {
                log.error("[Neptune RPC Gson]: {}", "Gson 反序列化出现异常", e);
            }
            return type;
        }

        @Override
        public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.getName());
        }
    }
}
