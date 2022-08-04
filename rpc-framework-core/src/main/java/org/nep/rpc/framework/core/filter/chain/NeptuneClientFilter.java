package org.nep.rpc.framework.core.filter.chain;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

public abstract class NeptuneClientFilter
{

    private NeptuneClientFilter nextFilter;

    public NeptuneClientFilter setNextFilter(NeptuneClientFilter nextFilter) {
        this.nextFilter = nextFilter;
        return this;
    }

    public void execute(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation){
        if (nextFilter != null){
            nextFilter.filter(invokers, invocation);
        }
    }

    protected abstract void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation);
}
