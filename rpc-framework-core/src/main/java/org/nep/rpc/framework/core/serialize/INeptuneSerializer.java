package org.nep.rpc.framework.core.serialize;

public interface INeptuneSerializer
{


    <T> byte[] serialize(T source);

    <T> T deserialize(byte[] source, Class<T> clazz);

}
