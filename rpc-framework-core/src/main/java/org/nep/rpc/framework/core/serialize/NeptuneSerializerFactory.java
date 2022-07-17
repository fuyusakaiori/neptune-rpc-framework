package org.nep.rpc.framework.core.serialize;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>序列化工程</h3>
 */
@Slf4j
public class NeptuneSerializerFactory {
    private static final Map<Integer, INeptuneSerializer> SERIALIZER_CACHE = new HashMap<>();

    static {
        SERIALIZER_CACHE.put(0, new NeptuneJdkSerializer());
        SERIALIZER_CACHE.put(1, new NeptuneHessianSerializer());
        SERIALIZER_CACHE.put(2, new NeptuneGsonSerializer());
        SERIALIZER_CACHE.put(3, new NeptuneJackSonSerializer());
        SERIALIZER_CACHE.put(4, new NeptuneKryoSerializer());
    }

    public static INeptuneSerializer getSerializer(byte type){
       return SERIALIZER_CACHE.get((int) type);
    }

}
