package org.nep.rpc.framework.registry.url;

/**
 * <h3>存储在注册中心结点的格式</h3>
 */
public interface URL {

    String toProviderString();

    String toConsumerString();


}
