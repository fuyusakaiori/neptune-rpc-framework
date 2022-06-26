package org.nep.rpc.framework.registry.url;

import java.util.Map;

/**
 * <h3>存储在注册中心结点的格式</h3>
 */
public interface URL {

    String getApplicationName();

    String getServiceName();

    String getAddress();

    String getPort();

    Map<String, Object> getParameters();

    String toProviderString();

    String toConsumerString();


}
