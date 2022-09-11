package org.nep.rpc.framework.core.serialize;

public interface INeptuneSerializer {

    String gson = "gson";

    String hessian = "hessian";

    String kryo = "kryo";

    String jackson = "jackson";

    String jdk = "jdk";

    byte[] serialize(Object source);

    <T> T deserialize(byte[] source, Class<T> clazz);

}
