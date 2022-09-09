package org.nep.rpc.framework.core.router.round;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;

import java.util.List;

/**
 * <h3>加权轮询</h3>
 */
public class NeptuneWeightRoundRobinLoadBalance extends AbstractNeptuneRpcLoadBalance {

    // 起始权重
    private static int weight;

    /**
     * <h3>注: 感觉权重都一样的情况没有必要额外判断, 没啥区别</h3>
     */
    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 计算总权重
        int weightSum = invokers.stream()
                                .mapToInt(NeptuneRpcInvoker::getFixedWeight)
                                .sum();
        // 2. 加权轮询
        int offset = weight++ % weightSum;
        for (NeptuneRpcInvoker invoker : invokers) {
            if (offset <= invoker.getFixedWeight())
                return invoker;
            offset -= invoker.getFixedWeight();
        }
        return null;
    }
}
