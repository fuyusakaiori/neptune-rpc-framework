package org.nep.rpc.framework.core.filter.chain;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

@Slf4j
public abstract class NeptuneServerFilter {

    private NeptuneServerFilter nextFilter;

    public NeptuneServerFilter setNextFilter(NeptuneServerFilter nextFilter) {
        this.nextFilter = nextFilter;
        return this;
    }

    public void execute(NeptuneRpcInvocation invocation){
        if (nextFilter != null){
            nextFilter.filter(invocation);
        }
    }

    protected abstract void filter(NeptuneRpcInvocation invocation);
}
