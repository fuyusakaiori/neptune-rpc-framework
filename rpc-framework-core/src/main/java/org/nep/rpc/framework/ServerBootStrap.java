package org.nep.rpc.framework;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.server.NeptuneRpcServer;

import java.util.Scanner;

/**
 * <h3>服务端启动类</h3>
 */
@Slf4j
public class ServerBootStrap {
    public static void main(String[] args) {
        NeptuneRpcServer neptuneRpcServer = new NeptuneRpcServer();
        neptuneRpcServer.startNeptune();
    }
}
