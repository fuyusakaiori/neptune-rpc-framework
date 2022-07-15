package org.nep.rpc.framework.core.serialize;

public interface INeptuneSerializer
{

    byte[] serialize(Object source);

    <T> T deserialize(byte[] source, Class<T> clazz);

}
