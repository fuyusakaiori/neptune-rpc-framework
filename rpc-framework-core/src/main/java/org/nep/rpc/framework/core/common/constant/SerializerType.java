package org.nep.rpc.framework.core.common.constant;

/**
 * <h3>序列化算法类型</h3>
 */
public enum SerializerType {

    SERIALIZER_JDK(0, "Jdk 序列化"),
    SERIALIZER_HESSIAN(1, "Hessian 序列化"),
    SERIALIZER_GSON(2, "Gson 序列化"),
    SERIALIZER_JACKSON(3, "JackSon 序列化"),
    SERIALIZER_KRYO(4, "Kryo 序列化");

    // 1. 序列化算法代号
    private byte code;
    // 2. 序列化算法名称
    private String name;

    SerializerType(int code, String name)
    {
        this.code = (byte) code;
        this.name = name;
    }

    public byte getCode()
    {
        return code;
    }

    public void setCode(byte code)
    {
        this.code = code;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }


}
