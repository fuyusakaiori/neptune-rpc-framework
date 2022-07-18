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
        NeptuneRpcServerCache.registerServiceUrl(url);
    }

    @Override
    public void cancel(NeptuneURL url) {
        NeptuneRpcServerCache.cancelServiceUrl(url);
    }

    @Override
    public void subscribe(NeptuneURL url) {
        NeptuneRpcClientCache.Services.subscribe(url);
    }

    @Override
    public void unSubscribe(NeptuneURL url) {
        NeptuneRpcClientCache.Services.cancel(url);
    }

    public abstract void beforeSubscribe(NeptuneURL url);

    public abstract void afterSubscribe(NeptuneURL url);

    public abstract List<String> lookup(String serviceName);

}
