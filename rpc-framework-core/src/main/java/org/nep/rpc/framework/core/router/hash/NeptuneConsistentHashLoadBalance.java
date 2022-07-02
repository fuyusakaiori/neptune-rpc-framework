package org.nep.rpc.framework.core.router.hash;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;

import java.util.List;

/**
 * <h3>一致性哈希</h3>
 */
public class NeptuneConsistentHashLoadBalance extends AbstractNeptuneRpcLoadBalance {
    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers)
    {
        return null;
    }
}
