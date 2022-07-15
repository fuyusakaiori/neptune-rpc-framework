package org.nep.rpc.framework.core.serialize;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.hessian.HessianSerializer;
import org.nep.rpc.framework.core.serialize.jdk.JdkSerializer;
import org.nep.rpc.framework.core.serialize.json.GsonSerializer;
import org.nep.rpc.framework.core.serialize.json.JackSonSerializer;
import org.nep.rpc.framework.core.serialize.kryo.KryoSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>序列化工程</h3>
 */
@Slf4j
public class NeptuneSerializerFactory
{

    private static final Map<Integer, INeptuneSerializer> SERIALIZER_CACHE = new HashMap<>();

    static {
        SERIALIZER_CACHE.put(0, new JdkSerializer());
        SERIALIZER_CACHE.put(1, new HessianSerializer());
        SERIALIZER_CACHE.put(2, new GsonSerializer());
        SERIALIZER_CACHE.put(3, new JackSonSerializer());
        SERIALIZER_CACHE.put(4, new KryoSerializer());
    }

    public static INeptuneSerializer getSerializer(byte type){
       return SERIALIZER_CACHE.get((int) type);
    }

}
