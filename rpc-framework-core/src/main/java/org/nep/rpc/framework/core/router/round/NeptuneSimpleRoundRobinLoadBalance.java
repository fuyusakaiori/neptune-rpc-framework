package org.nep.rpc.framework.core.router.round;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <h3>完全轮询</h3>
 */
@Slf4j
public class NeptuneSimpleRoundRobinLoadBalance extends AbstractNeptuneRpcLoadBalance implements INeptuneRpcLoadBalance {

    private static final AtomicLong cnt = new AtomicLong(0);

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        return invokers.get((int)(cnt.getAndIncrement() % invokers.size()));
    }
}
