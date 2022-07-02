package org.nep.rpc.framework.core.router.round;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.INeptuneRpcLoadBalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h3>完全轮询</h3>
 */
@Slf4j
public class NeptuneSimpleRoundRobinLoadBalance extends AbstractNeptuneRpcLoadBalance implements INeptuneRpcLoadBalance {

    // 计数值: 暂时没有考虑并发的情况, 或者说暂时没有考虑原子性
    private static int index;

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers) {
        if (index == invokers.size()){
            log.info("[Neptune RPC Router]: 服务器重新开始新一次的轮询");
            index = 0;
        }
        return invokers.get(index++);
    }
}
