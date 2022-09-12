package org.nep.rpc.framework.core.filter.client;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.filter.chain.NeptuneClientFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

@Slf4j
public class NeptuneClientGroupFilter extends NeptuneClientFilter {

    private static final String GROUP = "group";

    @Override
    protected void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 获取客户端请求中携带的分组参数
        String group = String.valueOf(invocation.getAttachments().get(GROUP));
        // 2. 如果客户端没有携带分组参数就直接返回
        if (StrUtil.isEmpty(group)){
            log.error("[neptune rpc client filter chain]: group name is null");
            return;
        }
        // 2. 根据分组分流: 移除不是这个分组的调用
        invokers.removeIf(invoker -> !group.equals(invoker.getGroup()));
        // 3. 如果分流后调用集合为空, 那么就没有可以调用的内容
        if (CollectionUtil.isEmpty(invokers)){
            log.info("[neptune rpc client filter chain]: can't find invoker to call");
        }
    }
}
