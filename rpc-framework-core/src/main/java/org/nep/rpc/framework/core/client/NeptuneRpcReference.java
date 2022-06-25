package org.nep.rpc.framework.core.client;

import org.nep.rpc.framework.core.proxy.ProxyFactory;

public class NeptuneRpcReference {

    private final ProxyFactory proxyFactory;

    public NeptuneRpcReference(ProxyFactory proxyFactory){
        this.proxyFactory = proxyFactory;
    }

    public <T> T remoteCall(Class<T> clazz){
        return proxyFactory.getProxy(clazz);
    }
}
