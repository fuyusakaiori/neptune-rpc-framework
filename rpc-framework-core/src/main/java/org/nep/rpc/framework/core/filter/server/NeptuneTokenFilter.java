package org.nep.rpc.framework.core.filter.server;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

public class NeptuneTokenFilter extends NeptuneServerFilter {

    private static final String TOKEN = "token";

    @Override
    protected void filter(NeptuneRpcInvocation invocation) {
        // 1. 获取 token
        String token = String.valueOf(invocation.getAttachments().get(TOKEN));
        // TODO 2. 获取服务的 token

    }
}
