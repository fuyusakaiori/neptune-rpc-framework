package org.nep.rpc.framework.core.proxy.jdk;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.core.proxy.ProxyFactory;

import java.lang.reflect.Proxy;

@Slf4j
@SuppressWarnings("unchecked")
public class JdkDynamicProxyFactory implements ProxyFactory {
    @Override
    public <T> T getProxy(NeptuneRpcReference reference) {
        Class<?> target = reference.getTarget();
        return (T) Proxy.newProxyInstance(
                target.getClassLoader(), new Class[]{target}, new JdkDynamicProxy(reference));
    }
}
