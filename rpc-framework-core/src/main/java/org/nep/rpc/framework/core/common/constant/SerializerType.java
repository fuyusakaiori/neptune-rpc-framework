package org.nep.rpc.framework.core.common.constant;

import lombok.Data;

/**
 * <h3>序列化算法类型</h3>
 */
public enum SerializerType {

    SERIALIZER_JDK(0, "Jdk 序列化"),
    SERIALIZER_HESSIAN(1, "Hessian 序列化"),
    SERIALIZER_JSON(2, "Json 序列化"),
    SERIALIZER_KRYO(3, "Kryo 序列化"),
    SERIALIZER_PROTOBUF(4, "Protobuf 序列化"),
    SERIALIZER_AVRO(5, "Avro 序列化"),
    SERIALIZER_JBOSS(6, "Jboss 序列化");

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
