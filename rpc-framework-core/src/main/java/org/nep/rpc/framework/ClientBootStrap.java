package org.nep.rpc.framework;

import org.nep.rpc.framework.core.client.NeptuneRpcClient;
import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.interfaces.IDataService;

public class ClientBootStrap {

    public static void main(String[] args) {
        // 1. 启动客户端
        NeptuneRpcClient neptuneRpcClient = new NeptuneRpcClient();
        neptuneRpcClient.startNeptune();
        // 2. 获取动态代理类
        NeptuneRpcReference reference = neptuneRpcClient.getReference();
        IDataService dataService = reference.remoteCall(IDataService.class);
        // 3. 调用方法执行远程过程调用
        for (int index = 0; index < 10; index++) {
            dataService.send(114514);
        }
        // 4. 关闭客户端
        neptuneRpcClient.closeNeptune();
    }

}
