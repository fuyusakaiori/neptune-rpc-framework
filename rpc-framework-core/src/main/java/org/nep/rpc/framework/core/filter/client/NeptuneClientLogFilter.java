package org.nep.rpc.framework.core.filter.client;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.common.resource.PropertyBootStrap;
import org.nep.rpc.framework.core.filter.chain.NeptuneClientFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

@Slf4j
public class NeptuneClientLogFilter extends NeptuneClientFilter {


    @Override
    protected void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        invocation.getAttachments().put("c_app_name", PropertyBootStrap.getClientConfig().getApplicationName());
        log.info("[neptune rpc client filter chain]" +
                         invocation.getAttachments().get("c_app_name") + " invoke " +
                         invocation.getServiceName() + Separator.WELL + invocation.getMethodName());
    }
}
