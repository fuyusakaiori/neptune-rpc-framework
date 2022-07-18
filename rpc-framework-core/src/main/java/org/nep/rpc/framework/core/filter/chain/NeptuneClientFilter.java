package org.nep.rpc.framework.core.filter.chain;

import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

public abstract class NeptuneClientFilter
{

    private NeptuneClientFilter nextFilter;

    public void setNextFilter(NeptuneClientFilter nextFilter) {
        this.nextFilter = nextFilter;
    }

    public void execute(NeptuneRpcInvocation invocation){
        if (nextFilter != null){
            nextFilter.filter(invocation);
        }
    }

    protected abstract void filter(NeptuneRpcInvocation invocation);
}
