package org.nep.rpc.framework.core.proxy.javassist;

import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.core.proxy.ProxyFactory;

@SuppressWarnings("unchecked")
public class JavassistProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(NeptuneRpcReference reference) {
        try {
            Class<?> target = reference.getTarget();
            return (T) NeptuneProxy.newProxyInstance(target.getClassLoader(), target, new JavassistProxy(reference));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
