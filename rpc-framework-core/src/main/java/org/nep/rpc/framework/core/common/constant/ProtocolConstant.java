package org.nep.rpc.framework.core.common.constant;

/**
 * <h3>协议常量</h3>
 */
public class ProtocolConstant {
    // 1. 魔数
    public static final String MAGIC_NUMBER = "NEPTUNE";
    // 2. 版本号
    public static final byte PROTOCOL_VERSION = 1;
    // 3. 序列化算法
    public static final byte SERIALIZER_PROTOBUF = 0;
    public static final byte SERIALIZER_HESSIAN = 1;
    public static final byte SERIALIZER_JSON = 2;
    public static final byte SERIALIZER_JBOSS = 3;
    public static final byte SERIALIZER_KRYO = 4;
    public static final byte SERIALIZER_AVRO = 5;
}
