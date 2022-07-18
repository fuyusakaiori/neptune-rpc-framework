package org.nep.rpc.framework.core.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class NeptuneUtil {

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

    private static final Map<String, Class<?>> PRIMITIVE_MAP = new HashMap<>();

    static {
        PRIMITIVE_MAP.put(Integer.class.getTypeName(), Integer.TYPE);
        PRIMITIVE_MAP.put(Boolean.class.getTypeName(), Boolean.TYPE);
        PRIMITIVE_MAP.put(Long.class.getTypeName(), Long.TYPE);
        PRIMITIVE_MAP.put(Double.class.getTypeName(), Double.TYPE);
        PRIMITIVE_MAP.put(Float.class.getTypeName(), Float.TYPE);
        PRIMITIVE_MAP.put(Character.class.getTypeName(), Character.TYPE);
        PRIMITIVE_MAP.put(Byte.class.getTypeName(), Byte.TYPE);
        PRIMITIVE_MAP.put(Short.class.getTypeName(), Short.TYPE);
    }

    /**
     * <h3>获取本机 IP 地址</h3>
     */
    public static String getLocalHost(){
        String address = DEFAULT_ADDRESS;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("[Neptune RPC SocketUtil]: 获取本机 IP 地址异常");
        }
        return address;
    }

    public static Class<?> getPrimitive(String wrapper){
        return PRIMITIVE_MAP.get(wrapper);
    }

}
