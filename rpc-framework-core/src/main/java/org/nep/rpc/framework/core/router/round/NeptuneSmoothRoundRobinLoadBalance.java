package org.nep.rpc.framework.core.router.round;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;

import java.util.List;

/**
 * <h3>平滑加权轮询</h3>
 */
public class NeptuneSmoothRoundRobinLoadBalance extends AbstractNeptuneRpcLoadBalance {

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 计算服务提供者的总动态权重
        int weightSum = invokers.stream()
                                .mapToInt(NeptuneRpcInvoker::getFixedWeight)
                                .sum();
        // 2. 找到权重最大的服务提供者
        NeptuneRpcInvoker target = null;
        for (NeptuneRpcInvoker invoker : invokers) {
            if (target == null || invoker.getDynamicWeight() > target.getDynamicWeight()){
                target = invoker;
            }
        }
        // 3. 选中的服务提供者减少动态权重
        if (target != null){
            target.setDynamicWeight(target.getDynamicWeight() - weightSum);
        }
        // 4. 更新每个服务提供者的动态权重
        for (NeptuneRpcInvoker invoker : invokers) {
            // 注: 将每个服务提供者的动态权重 = 当前的动态权重 + 固定权重
            invoker.setDynamicWeight(invoker.getDynamicWeight() + invoker.getFixedWeight());
        }
        return target;
    }
}
