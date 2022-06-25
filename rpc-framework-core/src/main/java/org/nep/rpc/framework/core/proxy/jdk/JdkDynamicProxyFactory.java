package org.nep.rpc.framework.core.proxy.jdk;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

@Slf4j
@SuppressWarnings("unchecked")
public class JdkDynamicProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(), new Class[]{clazz}, new JdkDynamicProxy(clazz));
    }
}
