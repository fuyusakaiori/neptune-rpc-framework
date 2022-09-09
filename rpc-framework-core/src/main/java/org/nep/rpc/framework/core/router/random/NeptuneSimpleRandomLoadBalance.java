package org.nep.rpc.framework.core.router.random;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * <h3>简单随机算法</h3>
 */
public class NeptuneSimpleRandomLoadBalance extends AbstractNeptuneRpcLoadBalance {

    private final Random random = new Random();
    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        return invokers.get(random.nextInt(invokers.size()));
    }
}
