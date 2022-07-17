package org.nep.rpc.framework.core.router.hash;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.common.hash.NeptuneRpcHashFunction;
import org.nep.rpc.framework.core.common.util.SocketUtil;
import org.nep.rpc.framework.core.router.invoker.NeptuneRpcHashInvoker;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.invoker.NeptuneRpcPhysicalInvoker;
import org.nep.rpc.framework.core.router.invoker.NeptuneRpcVirtualInvoker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <h3>一致性哈希</h3>
 */
@Slf4j
public class NeptuneConsistentHashLoadBalance extends AbstractNeptuneRpcLoadBalance {
    // 哈希环
    private final SortedMap<Long, NeptuneRpcVirtualInvoker> ring = new TreeMap<>();
    // TODO 更新真实结点集合
    private final Set<NeptuneRpcPhysicalInvoker> physicalInvokerSet = new HashSet<>();
    // 哈希函数
    private final NeptuneRpcHashFunction hashFunction;

    private final int virtualInvokerCount;
    public NeptuneConsistentHashLoadBalance(NeptuneRpcHashFunction hashFunction, int virtualInvokerCount) {
        if (hashFunction == null)
            throw new IllegalArgumentException("[Neptune RPC Router]: 哈希函数不可以为空");
        if (virtualInvokerCount <= 0)
            throw new IllegalArgumentException("[Neptune RPC Router]: 添加虚拟结点数量不可以为 0");
        this.hashFunction = hashFunction;
        this.virtualInvokerCount = virtualInvokerCount;
    }

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers) {
        // 1. 获取新注册的结点
        List<NeptuneRpcPhysicalInvoker> physicalInvokers =
                invokers.stream()
                        .map(NeptuneRpcPhysicalInvoker::new)
                        .filter(physicalInvokerSet::add)
                        .collect(Collectors.toList());
        // 2. 在哈希环中添加结点
        for (NeptuneRpcPhysicalInvoker physicalInvoker : physicalInvokers) {
            addInvoker(physicalInvoker, virtualInvokerCount);
        }
        // 3. 选择哈希环中的结点
        long hash = hashFunction.hash(SocketUtil.getLocalHost());
        // 3.1 返回所有大于等于哈希值的键值对
        SortedMap<Long, NeptuneRpcVirtualInvoker> tailMap = ring.tailMap(hash);
        // TODO 3.2 判断哈希表是否为空
        hash = !tailMap.isEmpty() ? tailMap.firstKey() : ring.firstKey();
        return ring.get(hash).getPhysicalInvoker().getInvoker();
    }

    /**
     * <h3>如果有新的服务器进入注册中心, 就需要添加到环中</h3>
     */
    public void addInvoker(NeptuneRpcPhysicalInvoker invoker, int virtualInvokerCount){
        if (virtualInvokerCount <= 0)
            throw new IllegalArgumentException("[Neptune RPC Router]: 添加虚拟结点数量不可以为 0");
        // 1. 获取虚拟结点数量
        int replicas = getExistingReplicas(invoker);
        // 2. 添加虚拟结点
        for (int index = 0; index < virtualInvokerCount; index++) {
            NeptuneRpcVirtualInvoker virtualInvoker =
                    new NeptuneRpcVirtualInvoker(invoker, replicas + index);
            ring.put(hashFunction.hash(virtualInvoker.getKey()), virtualInvoker);
        }
    }

    /**
     * <h3>如果有服务下线, 那么需要将环中对应的虚拟结点全部移除</h3>
     */
    public void removeInvoker(NeptuneRpcPhysicalInvoker invoker){
        ring.keySet().removeIf(index -> ring.get(index).isVirtualInvoker(invoker));
    }

    /**
     * <h3>获取真实结点现在拥有的副本结点</h3>
     */
    private int getExistingReplicas(NeptuneRpcPhysicalInvoker invoker){
        int replicas = 0;
        // 1. 遍历环中的结点
        for (NeptuneRpcVirtualInvoker virtualInvoker : ring.values()) {
            // 2. 判断哪些虚拟结点映射到这个真实结点
            if (virtualInvoker.isVirtualInvoker(invoker)){
                replicas++;
            }
        }
        return replicas;
    }
}
