package org.nep.rpc.framework.registry;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.registry.url.NeptuneURL;

import java.util.List;

/**
 * <h3>抽象注册中心: 提供注册中心的基本实现</h3>
 */
@Slf4j
public abstract class AbstractNeptuneRegister implements
        NeptuneServiceDiscovery, NeptuneServiceRegistry, NeptuneServiceSubscribe {

    @Override
    public void register(NeptuneURL url) {

    }

    @Override
    public void cancel(NeptuneURL url) {

    }

    @Override
    public void subscribe(NeptuneURL url) {

    }

    @Override
    public void unsubscribe(NeptuneURL url) {

    }

    public abstract void beforeSubscribe(String serviceName);

    public abstract void afterSubscribe(String serviceName);

    public abstract List<String> lookup(String serviceName);

}
