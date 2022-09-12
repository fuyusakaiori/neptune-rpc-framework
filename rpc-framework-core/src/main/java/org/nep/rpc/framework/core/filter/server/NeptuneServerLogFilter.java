package org.nep.rpc.framework.core.filter.server;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

@Slf4j
public class NeptuneServerLogFilter extends NeptuneServerFilter {
    @Override
    protected void filter(NeptuneRpcInvocation invocation) {
        log.info("[neptune rpc server filter chain]" +
                         invocation.getAttachments().get("c_app_name") + " invoke " +
                         invocation.getServiceName() + Separator.WELL + invocation.getMethodName());
    }
}
