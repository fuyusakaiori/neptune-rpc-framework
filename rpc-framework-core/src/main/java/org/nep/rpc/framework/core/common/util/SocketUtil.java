package org.nep.rpc.framework.core.common.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class SocketUtil {

    private static final String DEFAULT_ADDRESS = "127.0.0.1";

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

}
