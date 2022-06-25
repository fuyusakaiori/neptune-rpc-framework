package org.nep.rpc.framework.core;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.server.NeptuneRpcServer;

/**
 * <h3>启动类</h3>
 */
@Slf4j
public class ServerBootStrap
{

    public static void main(String[] args) {
        new NeptuneRpcServer().startNeptune();
    }
}
