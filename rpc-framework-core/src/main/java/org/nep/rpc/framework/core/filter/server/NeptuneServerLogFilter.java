package org.nep.rpc.framework.core.filter.server;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

@Slf4j
public class NeptuneServerLogFilter extends NeptuneServerFilter {
    @Override
    protected void filter(NeptuneRpcInvocation invocation) {
        log.info(invocation.getAttachments().get("c_app_name") + " 正在调用 -----> "
                         + invocation.getServiceName() + Separator.WELL + invocation.getMethodName());
    }
}
