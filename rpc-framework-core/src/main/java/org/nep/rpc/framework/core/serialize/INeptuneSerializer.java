package org.nep.rpc.framework.core.serialize;

public interface INeptuneSerializer
{


    byte[] serialize(Object source);

    Object deserialize(byte[] source);

}
