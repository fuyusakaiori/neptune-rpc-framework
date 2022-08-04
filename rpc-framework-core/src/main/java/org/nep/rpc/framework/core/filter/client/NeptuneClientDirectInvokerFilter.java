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
        // 1. 获取 IP 地址
        String url = String.valueOf(invocation.getAttachments().get(URL));
        // 2. 如果 IP 地址为空, 那么直接返回
        if (StrUtil.isEmpty(url)){
            log.error("[Neptune RPC Filter]: 获取的 IP 地址为空");
            return;
        }
        // 3. 根据 IP 地址分流
        invokers.removeIf(invoker -> !url.equals(
                invoker.getAddress() + Separator.COLON + invoker.getPort()));
        // 4. 检查分流后的调用者集合是否为空
        if (CollectionUtil.isEmpty(invokers)){
            log.info("[Neptune RPC Filter]: IP 地址不匹配 -> 没有可以调用的服务");
        }
    }
}
