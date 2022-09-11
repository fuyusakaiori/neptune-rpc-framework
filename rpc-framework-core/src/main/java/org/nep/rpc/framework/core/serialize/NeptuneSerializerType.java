package org.nep.rpc.framework.core.serialize;

/**
 * <h3>序列化算法类型</h3>
 */
public enum NeptuneSerializerType {

    SERIALIZER_JDK(0, "JdkSerializer"),
    SERIALIZER_HESSIAN(1, "HessianSerializer"),
    SERIALIZER_GSON(2, "GsonSerializer"),
    SERIALIZER_JACKSON(3, "JackSonSerializer"),
    SERIALIZER_KRYO(4, "KryoSerializer");

    /**
     * <h3>序列化算法编号</h3>
     */
    private final byte code;
    /**
     * <h3>序列化算法名称</h3>
     */
    private final String name;

    NeptuneSerializerType(int code, String name) {
        this.code = (byte) code;
        this.name = name;
    }

    public byte getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static int getSerializerCode(INeptuneSerializer serializer){
        if(serializer instanceof NeptuneJdkSerializer){
            return SERIALIZER_JDK.code;
        }else if (serializer instanceof NeptuneJackSonSerializer){
            return SERIALIZER_JACKSON.code;
        }else if (serializer instanceof NeptuneKryoSerializer){
            return SERIALIZER_KRYO.code;
        }else if (serializer instanceof NeptuneHessianSerializer){
            return SERIALIZER_HESSIAN.code;
        }else if (serializer instanceof NeptuneGsonSerializer){
            return SERIALIZER_GSON.code;
        }
        return -1;
    }

}
