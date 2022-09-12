package org.nep.rpc.framework.core.filter.client;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.constant.Separator;
import org.nep.rpc.framework.core.filter.chain.NeptuneClientFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

@Slf4j
public class NeptuneClientDirectInvokerFilter extends NeptuneClientFilter {

    private static final String URL = "url";

    @Override
    protected void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 获取客户端调用的服务端地址: ip:port
        String url = String.valueOf(invocation.getAttachments().get(URL));
        // 2. 如果调用的服务端地址为空, 那么直接返回
        if (StrUtil.isEmpty(url)){
            log.error("[neptune rpc client filter chain]: invocation url is null");
            return;
        }
        // 3. 如果调用集合中的服务端地址不符合客户端发起的请求, 那么直接移除: 同一台服务器上可以有多个进程
        invokers.removeIf(invoker -> !url.equals(
                invoker.getAddress() + Separator.COLON + invoker.getPort()));
        // 4. 检查分流后的调用者集合是否为空
        if (CollectionUtil.isEmpty(invokers)){
            log.info("[neptune rpc client filter chain]: can't find invoker to call");
        }
    }
}
