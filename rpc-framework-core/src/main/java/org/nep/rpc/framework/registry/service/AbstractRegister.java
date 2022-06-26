package org.nep.rpc.framework.registry.service;

import lombok.extern.slf4j.Slf4j;
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
        NeptuneRpcServerCache.registerInCache(url);
    }

    @Override
    public void cancel(URL url) {
        NeptuneRpcServerCache.removeFromCache(url);
    }

    @Override
    public void subscribe(URL url) {

    }

    @Override
    public void unSubscribe(URL url) {

    }

    public abstract void beforeSubscribe(URL url);

    public abstract void afterSubscribe(URL url);

    public abstract List<String> getServiceAllNodes();

}
