package org.nep.rpc.framework.core.router;

import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;

import java.util.List;

/**
 * <h3>负载均衡接口</h3>
 * <h3>1. 常见四类负载均衡策略: </h3>
 * <h3>1.1 随机策略: </h3>
 * <h3>1.1.1 完全随机: 直接调用随机函数选择服务端</h3>
 * <h3>1.1.2 加权随机: 根据权重值增加或者减少随机到的概率</h3>
 * <h3>1.2 轮询策略: </h3>
 * <h3>1.2.1 完全轮询: 按照顺序选择服务端</h3>
 * <h3>1.2.2 加权轮询: 根据权重进行轮询</h3>
 * <h3>1.2.3 平滑加权轮询: 非常神奇的算法, 但是不难</h3>
 * <h3>1.3 一致性哈希: 非常经典的算法, 好好了解下</h3>
 * <h3>2. 主流框架中的负载均衡策略</h3>
 * <h3>2.1 dubbo: 加权随机、加权轮询、一致性哈希、最小活跃调用数</h3>
 * <h3>2.2 nginx: 完全轮询、加权轮询、粘性 IP、响应时间、URL</h3>
 * <h3>2.3 nacos / ribbon</h3>
 * <h3>3. 自定义的负载均衡策略</h3>
 */
public interface INeptuneRpcLoadBalance {

    String consistentHash = "consistent.hash";

    String randomSimple = "random.simple";

    String randomWeight = "random.weight";

    String robinSimple = "robin.simple";

    String robinWeight = "robin.weight";

    String robinSmooth = "robin.smooth";

    /**
     * <h3>负载均衡</h3>
     * @param invokers 提供服务的所有服务端
     */
    NeptuneRpcInvoker select(List<NeptuneRpcInvoker> invokers, NeptuneRpcInvocation invocation);

}
