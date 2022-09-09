package org.nep.rpc.framework.core.router.hash;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.util.NeptuneUtil;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.router.AbstractNeptuneRpcLoadBalance;
import org.nep.rpc.framework.core.router.invoker.NeptuneRpcPhysicalInvoker;
import org.nep.rpc.framework.core.router.invoker.NeptuneRpcVirtualInvoker;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <h3>一致性哈希</h3>
 */
@Slf4j
public class NeptuneConsistentHashLoadBalance extends AbstractNeptuneRpcLoadBalance {


    /**
     * <h3>每个服务都有对应的哈希环进行处理</h3>
     */
    private final Map<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    public NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation) {
        String key = invokers.get(0).getUrl().getServiceName();
        // 1. 获取调用的接口对应的哈希环
        ConsistentHashSelector selector = selectors.getOrDefault(key, new ConsistentHashSelector());
        // 2. 更新环中的结点
        selector.updateInvoker(invokers);
        // 3. 更新哈希环表
        selectors.put(key, selector);
        // 4. 返回负载均衡选择地服务端
        return selector.select(invocation);
    }


    /**
     * <h3>实现一致性哈希的类</h3>
     */
    private static final class ConsistentHashSelector{

        private static final int virtualInvokerCount = 10;
        private final NeptuneRpcMd5Function hashFunction = new NeptuneRpcMd5Function();
        private final SortedMap<Long, NeptuneRpcVirtualInvoker> hashRing = new TreeMap<>();

        private NeptuneRpcInvoker select(NeptuneRpcInvocation invocation){
            // 1. 根据方法参数和方法名计算哈希值: 如果只根据 IP 地址计算哈希值, 那么无论调用接口中哪个方法都只会打中同一个服务
            long hash = hashFunction.hash(invocation.getMethodName() + Arrays.toString(invocation.getArgs()));
            // 2 返回所有大于等于哈希值的键值对
            SortedMap<Long, NeptuneRpcVirtualInvoker> tailMap = hashRing.tailMap(hash);
            // 3 如果大于当前哈希值的所有哈希表集合为空, 那么就获取哈希环中
            hash = !tailMap.isEmpty() ? tailMap.firstKey() : hashRing.firstKey();
            // 4. 获取真实结点
            return hashRing.get(hash).getPhysicalInvoker().getInvoker();
        }

        /**
         * <h3>调用新增和删除更新环中的结点</h3>
         */
        private void updateInvoker(List<NeptuneRpcInvoker> invokers){
            // 1. NeptuneRpcInvoker -> NeptuneRpcPhysicalInvoker
            Set<NeptuneRpcPhysicalInvoker> newInvokers =
                    invokers.stream()
                            .map(NeptuneRpcPhysicalInvoker::new)
                            .collect(Collectors.toSet());
            // 2. 获取所有旧的服务端
            Set<NeptuneRpcPhysicalInvoker> oldInvokers = getExistingPhysicalInvokers();
            // 3. 获取两个集合的交集
            Collection<NeptuneRpcPhysicalInvoker> intersection =
                    CollectionUtil.intersection(newInvokers, oldInvokers);
            // 4. 新集合中不在交集中的服务端就是要新增的
            for (NeptuneRpcPhysicalInvoker physicalInvoker : newInvokers) {
                if (!intersection.contains(physicalInvoker))
                    addInvoker(physicalInvoker);
            }
            // 5. 旧集合中不在交集中的服务就是要移除的
            for (NeptuneRpcPhysicalInvoker physicalInvoker : oldInvokers){
                if (!intersection.contains(physicalInvoker))
                    removeInvoker(physicalInvoker);
            }
        }

        /**
         * <h3>如果有新的服务器进入注册中心, 就需要添加到环中</h3>
         */
        private void addInvoker(NeptuneRpcPhysicalInvoker invoker){
            // 1. 获取虚拟结点数量
            int replicas = getExistingReplicas(invoker);
            // 2. 添加虚拟结点
            for (int index = 0; index < virtualInvokerCount; index++) {
                NeptuneRpcVirtualInvoker virtualInvoker =
                        new NeptuneRpcVirtualInvoker(invoker, replicas + index);
                hashRing.put(hashFunction.hash(virtualInvoker.getKey()), virtualInvoker);
            }
        }

        /**
         * <h3>如果有服务下线, 那么需要将环中对应的虚拟结点全部移除</h3>
         */
        private void removeInvoker(NeptuneRpcPhysicalInvoker invoker){
            hashRing.keySet().removeIf(index -> hashRing.get(index).isVirtualInvoker(invoker));
        }

        /**
         * <h3>获取所有真实的服务端</h3>
         */
        private Set<NeptuneRpcPhysicalInvoker> getExistingPhysicalInvokers(){
            return hashRing.values().stream()
                    .map(NeptuneRpcVirtualInvoker::getPhysicalInvoker)
                    .collect(Collectors.toSet());
        }

        /**
         * <h3>获取真实结点现在拥有的副本结点</h3>
         */
        private int getExistingReplicas(NeptuneRpcPhysicalInvoker invoker){
            int replicas = 0;
            // 1. 遍历环中的结点
            for (NeptuneRpcVirtualInvoker virtualInvoker : hashRing.values()) {
                // 2. 判断哪些虚拟结点映射到这个真实结点
                if (virtualInvoker.isVirtualInvoker(invoker)){
                    replicas++;
                }
            }
            return replicas;
        }
    }

    /**
     * <h3>MD5 哈希函数</h3>
     */
    private static class NeptuneRpcMd5Function{

        private static final String functionName = "MD5";
        private final MessageDigest instance;

        public NeptuneRpcMd5Function() {
            try {
                this.instance = MessageDigest.getInstance(functionName);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("[Neptune RPC HashFunction]: 初始化 MD5 哈希函数异常", e);
            }
        }

        public long hash(String key) {
            this.instance.reset();
            this.instance.update(key.getBytes());
            byte[] digest = instance.digest();
            long hash = 0;
            for (int index = 0; index < 4; index++) {
                hash <<= 8;
                hash |= ((int) digest[index]) & 0xFF;
            }
            return hash;
        }
    }


}
