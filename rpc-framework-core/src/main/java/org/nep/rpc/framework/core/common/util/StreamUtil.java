package org.nep.rpc.framework.core.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.IOException;

/**
 * <h3>负责关闭流</h3>
 */
@Slf4j
public class StreamUtil {

    public static void close(Closeable closeable){
        try {
            if (closeable != null)
                closeable.close();
        } catch (IOException e) {
            log.error("[Neptune RPC Serialize]: JDK 关闭流对象时出现异常", e);
        }
    }

}
