package org.nep.rpc.framework.core.filter.chain;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

@Slf4j
public abstract class NeptuneServerFilter {

    private NeptuneServerFilter nextFilter;

    public void setNextFilter(NeptuneServerFilter nextFilter) {
        this.nextFilter = nextFilter;
    }

    public void execute(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation){
        if (nextFilter != null){
            nextFilter.filter(invokers, invocation);
        }
    }

    protected abstract void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation);
}
