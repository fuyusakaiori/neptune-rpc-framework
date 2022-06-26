package org.nep.rpc.framework.registry.service;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.registry.url.URL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <h3>抽象注册中心: 提供注册中心的基本实现</h3>
 */
@Slf4j
public abstract class AbstractRegister implements RegistryService {

    // 存储已经注册的服务的结点地址 需要考虑并发吗?
    private static final Set<URL> PROVIDER_URL_SET = new HashSet<>();

    @Override
    public void register(URL url) {
        PROVIDER_URL_SET.add(url);
    }

    @Override
    public void cancel(URL url) {
        PROVIDER_URL_SET.remove(url);
    }

    @Override
    public void subscribe(URL url) {

    }

    @Override
    public void unSubscribe(URL url) {

    }


}
