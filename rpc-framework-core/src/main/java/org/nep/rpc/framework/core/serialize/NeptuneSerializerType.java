package org.nep.rpc.framework.core.serialize;

/**
 * <h3>序列化算法类型</h3>
 */
public enum NeptuneSerializerType
{

    SERIALIZER_JDK(0, "JdkSerializer"),
    SERIALIZER_HESSIAN(1, "HessianSerializer"),
    SERIALIZER_GSON(2, "GsonSerializer"),
    SERIALIZER_JACKSON(3, "JackSonSerializer"),
    SERIALIZER_KRYO(4, "KryoSerializer");

    // 序列化算法代号
    private final byte code;
    // 序列化算法名称
    private final String name;

    NeptuneSerializerType(int code, String name) {
        this.code = (byte) code;
        this.name = name;
    }

    public byte getCode()
    {
        return code;
    }

    public String getName()
    {
        return name;
    }


}
