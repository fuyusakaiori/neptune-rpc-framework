package org.nep.rpc.framework.core;

import org.nep.rpc.framework.core.client.NeptuneRpcClient;
import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.interfaces.IDataService;

public class ClientBootStrap {

    public static void main(String[] args) {
        NeptuneRpcClient neptuneRpcClient = new NeptuneRpcClient();
        neptuneRpcClient.startNeptune();
        NeptuneRpcReference reference = neptuneRpcClient.getReference();
        IDataService dataService = reference.remoteCall(IDataService.class);
        for (int index = 0; index < 10; index++) {
            dataService.send("Hello Neptune RPC");
        }
        neptuneRpcClient.closeNeptune();
    }

}
