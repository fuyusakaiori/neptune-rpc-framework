package org.nep.rpc.framework.core.proxy;

public interface ProxyFactory {

    <T> T getProxy(Class<T> clazz);
}
