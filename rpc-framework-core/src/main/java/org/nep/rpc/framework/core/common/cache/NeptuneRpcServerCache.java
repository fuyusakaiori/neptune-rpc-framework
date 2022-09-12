package org.nep.rpc.framework.core.common.cache;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.server.NeptuneServiceWrapper;
import org.nep.rpc.framework.registry.url.NeptuneURL;

import java.util.*;

/**
 * <h3>容器</h3>
 */
@Slf4j
public class NeptuneRpcServerCache {

    /**
     * <h3>服务端缓存每个对外提供的服务</h3>
     */
    public static class Service{
        private static final Map<String, NeptuneServiceWrapper> services = new HashMap<>();

        /**
         * <h3>根据服务名获取服务</h3>
         */
        public static NeptuneServiceWrapper getService(String serviceName) {
            if (StrUtil.isEmpty(serviceName)){
                log.error("[neptune rpc server cache]: service name is null");
                return null;
            }
            return services.get(serviceName);
        }

        /**
         * <h3>将注册的服务添加哈希表中</h3>
         */
        public static void registerService(String serviceName, NeptuneServiceWrapper wrapper) {
            if (StrUtil.isEmpty(serviceName) || Objects.isNull(wrapper)){
                log.error("[neptune rpc server cache]: service name or wrapper is null");
                return;
            }
            services.put(serviceName, wrapper);
        }

    }

    /**
     * <h3>服务端异步注册服务缓存的 URL</h3>
     */
    public static Set<NeptuneURL> URLS = new HashSet<>();


}
