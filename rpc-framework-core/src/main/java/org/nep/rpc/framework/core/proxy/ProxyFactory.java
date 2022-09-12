package org.nep.rpc.framework.core.proxy;

import org.nep.rpc.framework.core.client.NeptuneRpcReference;

public interface ProxyFactory {

    String jdk = "jdk";

    String javassist = "javassist";

    <T> T getProxy(NeptuneRpcReference reference);
}
