package org.nep.rpc.framework.registry.service;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcClientCache;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.registry.url.URL;

import java.util.List;

/**
 * <h3>抽象注册中心: 提供注册中心的基本实现</h3>
 */
@Slf4j
public abstract class AbstractRegister implements RegistryService {


    @Override
    public void register(URL url) {
        NeptuneRpcServerCache.registerServiceUrl(url);
    }

    @Override
    public void cancel(URL url) {
        NeptuneRpcServerCache.cancelServiceUrl(url);
    }

    @Override
    public void subscribe(URL url) {
        NeptuneRpcClientCache.Services.subscribe(url);
    }

    @Override
    public void unSubscribe(URL url) {
        NeptuneRpcClientCache.Services.cancel(url);
    }

    public abstract void beforeSubscribe(URL url);

    public abstract void afterSubscribe(URL url);

    public abstract List<String> providers(String serviceName);

    public abstract List<String> getServiceAllNodes();

}
