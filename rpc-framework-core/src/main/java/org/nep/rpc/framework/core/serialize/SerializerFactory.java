package org.nep.rpc.framework.core.serialize;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.SerializerType;
import org.nep.rpc.framework.core.serialize.hessian.HessianSerializer;
import org.nep.rpc.framework.core.serialize.jdk.JdkSerializer;
import org.nep.rpc.framework.core.serialize.json.FastJsonSerializer;
import org.nep.rpc.framework.core.serialize.kryo.KryoSerializer;
import org.nep.rpc.framework.core.serialize.protobuf.ProtobufSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>序列化工程</h3>
 */
@Slf4j
public class SerializerFactory {

    private static final Map<Integer, INeptuneSerializer> SERIALIZER_CACHE = new HashMap<>();

    static {
        SERIALIZER_CACHE.put(0, new JdkSerializer());
        SERIALIZER_CACHE.put(1, new HessianSerializer());
        SERIALIZER_CACHE.put(2, new FastJsonSerializer());
        SERIALIZER_CACHE.put(3, new KryoSerializer());
        SERIALIZER_CACHE.put(4, new ProtobufSerializer());
    }

    public static INeptuneSerializer getSerializer(byte type){
       return SERIALIZER_CACHE.get((int) type);
    }

}
