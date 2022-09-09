package org.nep.rpc.framework.core.router.random;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;

import java.util.List;
import java.util.Random;

/**
 * <h3>加权随机算法</h3>
 */
public class NeptuneWeightRandomLoadBalance extends AbstractNeptuneRpcLoadBalance {
    private final Random random = new Random();

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        // 1. 服务所有提供者的总权重
        int weightSum = 0;
        // 2. 如果所有服务提供者的权重都相同, 那么就随机选一个
        boolean isSame = true;
        // 3. 计算总权重的同时判断权重是否相同
        for (int index = 0; index < invokers.size(); index++) {
            // 3.1 获取提供者的权重
            int weight = invokers.get(index).getFixedWeight();
            // 3.2 检查当前提供者的权重是否和上一个相同, 如果不同那么就继续加
            if (isSame && index > 0  && weight != invokers.get(index - 1).getFixedWeight()){
                isSame = false;
            }
            weightSum += weight;
        }
        // 4. 必须所有权重都不相同, 才进入加权随机算法
        if (!isSame && weightSum > 0){
            // 5. 随机生成权重
            int offset = random.nextInt(weightSum);
            for (NeptuneRpcInvoker invoker : invokers) {
                // 6. 如果随机生成的权重小于当前提供者的权重, 那么直接返回
                if (offset <= invoker.getFixedWeight())
                    return invoker;
                // 7. 如果大于那么就减去当前提供者权重
                offset -= invoker.getFixedWeight();
            }
        }
        // 8. 如果所有权重都相同, 就采用完全随机算法选一个
        return invokers.get(random.nextInt(invokers.size()));
    }
}
