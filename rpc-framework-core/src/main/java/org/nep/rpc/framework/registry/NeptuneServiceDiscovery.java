package org.nep.rpc.framework.registry;

import java.util.List;

/**
 * <h3>服务发现</h3>
 */
public interface NeptuneServiceDiscovery {

    List<String> lookup(String service);

}
