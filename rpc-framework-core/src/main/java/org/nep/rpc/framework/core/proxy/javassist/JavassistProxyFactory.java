package org.nep.rpc.framework.core.proxy.javassist;

import org.nep.rpc.framework.core.proxy.ProxyFactory;

@SuppressWarnings("unchecked")
public class JavassistProxyFactory implements ProxyFactory {

    @Override
    public <T> T getProxy(Class<T> clazz) {
        try {
            return (T) NeptuneProxy.newProxyInstance(clazz.getClassLoader(), clazz, new JavassistProxy(clazz));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
