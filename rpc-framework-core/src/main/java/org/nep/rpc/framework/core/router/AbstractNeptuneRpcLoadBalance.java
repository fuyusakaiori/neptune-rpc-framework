package org.nep.rpc.framework.core.router;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;

import java.util.List;

@Slf4j
public abstract class AbstractNeptuneRpcLoadBalance implements INeptuneRpcLoadBalance {


    @Override
    public NeptuneRpcInvoker select(List<NeptuneRpcInvoker> invokers) {
        // 1. 如果服务的提供者集合为空, 那么就不需要轮询了
        if (CollectionUtil.isEmpty(invokers)){
            log.debug("[Neptune RPC Router]: 服务的提供者为空");
            return null;
        }
        // 2. 如果服务的提供者集合仅有一个元素, 那么直接返回就可以了
        if (invokers.size() == 1){
            log.debug("[Neptune RPC Router]: 仅有唯一的提供者");
            return invokers.get(0);
        }
        // 3. 实现负载均衡
        return doSelect(invokers);
    }

    public abstract NeptuneRpcInvoker doSelect(List<NeptuneRpcInvoker> invokers);


}
