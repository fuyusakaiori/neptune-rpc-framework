package org.nep.rpc.framework.core.filter.server;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.cache.NeptuneRpcServerCache;
import org.nep.rpc.framework.core.filter.chain.NeptuneServerFilter;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.server.NeptuneServiceWrapper;

import java.util.List;
import java.util.Objects;

@Slf4j
public class NeptuneTokenFilter extends NeptuneServerFilter {

    private static final String TOKEN = "token";

    @Override
    protected void filter(NeptuneRpcInvocation invocation) {
        // 1. 获取 token
        String token = String.valueOf(invocation.getAttachments().get(TOKEN));
        // 2.根据客户端调用的接口获取对应的 token
        NeptuneServiceWrapper wrapper = NeptuneRpcServerCache.Service.getService(invocation.getServiceName());
        // 3. 检验获取到的接口是否存在
        if (Objects.isNull(wrapper)){
            log.error("[neptune rpc server filter chain]: client invoke server is not exist");
            throw new RuntimeException("[neptune rpc server filter chain]: client invoke server is not exist");
        }
        // 4. 检验接口是否设置服务鉴权: 如果没有设置就默认所有客户端都可以访问
        if (StrUtil.isEmpty(wrapper.getToken())){
            log.warn("[neptune rpc server filter chain]: server provide service doesn't have token, all clients can call");
            return;
        }
        // 5. 如果服务端设置过服务鉴权, 那么只有携带特定 token 的客户端才可以访问
        if (!wrapper.getToken().equals(token)){
            log.error("[neptune rpc server filter chain]: client token doesn't match server token, client doesn't hava privilege to call");
            throw new RuntimeException("[neptune rpc server filter chain]: client token doesn't match server token, client doesn't hava privilege to call");
        }

    }
}
