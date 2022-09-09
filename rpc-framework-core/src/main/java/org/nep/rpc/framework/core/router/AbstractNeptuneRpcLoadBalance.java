package org.nep.rpc.framework.core.router;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;
import java.util.Objects;

@Slf4j
public abstract class AbstractNeptuneRpcLoadBalance implements INeptuneRpcLoadBalance {

    private static final int ONLY_ONE_INVOKER = 1;

    @Override
    public NeptuneRpcInvoker select(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 如果服务的提供者集合为空, 那么就不需要轮询了
        if (CollectionUtil.isEmpty(invokers) || Objects.isNull(invocation)){
            log.debug("[neptune rpc load balance]: load balance invokers or invocation is empty");
            return null;
        }
        // 2. 如果服务的提供者集合仅有一个元素, 那么直接返回就可以了
        if (invokers.size() == ONLY_ONE_INVOKER){
            return invokers.get(0);
        }
        // 3. 实现负载均衡
        return doSelect(invokers, invocation);
    }

    public abstract NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation);


}
