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
    @Override
    protected void filter(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 获取分组
        String group = String.valueOf(invocation.getAttachments().get("group"));
        if (StrUtil.isEmpty(group)){
            log.error("[Neptune RPC Filter]: 获取的分组名称结果为空");
            return;
        }
        // 2. 根据分组分流: 移除不是这个分组的调用
        invokers.removeIf(invoker -> !group.equals(invoker.getGroup()));
        // 3. 如果分流后调用集合为空, 那么就没有可以调用的内容
        if (CollectionUtil.isEmpty(invokers)){
            log.info("[Neptune RPC Filter]: 分组不匹配 -> 没有可以调用的服务");
        }
    }
}
