package org.nep.rpc.framework.registry;

import org.nep.rpc.framework.registry.url.NeptuneURL;

/**
 * <h3>服务注册</h3>
 * <h3>1. 服务提供者在注册中心注册</h3>
 * <h3>2. 服务提供者在注册中心注销</h3>
 */
public interface NeptuneServiceRegistry {

    void register(NeptuneURL url);

    void cancel(NeptuneURL url);

}
