package org.nep.rpc.framework.registry;

import org.nep.rpc.framework.registry.url.NeptuneURL;

/**
 * <h3>服务订阅</h3>
 * <h3>1. 服务消费者可以订阅服务</h3>
 * <h3>2. 服务消费可以取消订阅服务</h3>
 */
public interface NeptuneServiceSubscribe {

    void subscribe(NeptuneURL url);

    void unsubscribe(NeptuneURL url);
}
