package org.nep.rpc.framework.starter.config;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.server.NeptuneRpcServer;
import org.nep.rpc.framework.core.server.NeptuneServiceWrapper;
import org.nep.rpc.framework.starter.common.INeptuneRpcService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Map;

/**
 * <h3>服务端自动装配类</h3>
 */
@Slf4j
public class INeptuneServerAutoConfiguration implements InitializingBean, ApplicationContextAware {

    /**
     * <h3>Spring 容器</h3>
     */
    private ApplicationContext applicationContext;


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 1. 获取所有被注解标注的类, 然后返回对象名称 + 对象实例组成的哈希表
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(INeptuneRpcService.class);
        // 2. 如果获取到的哈希表是空的, 那么就证明没有对外暴露的服务
        if (MapUtil.isEmpty(beanMap)){
            log.info("[Neptune RPC Start]: 没有对外暴露的服务接口");
            return;
        }
        // 3. 输出标志
        displayBanner();
        // 4. 启动服务器
        long start = System.currentTimeMillis();
        NeptuneRpcServer server = new NeptuneRpcServer();
        // 5. 遍历暴露的服务接口
        for (String beanName : beanMap.keySet()) {
            // 5.1 获取接口实例
            Object bean = beanMap.get(beanName);
            // 5.2 根据接口实例获取注解实例
            INeptuneRpcService rpcService = bean.getClass().getAnnotation(INeptuneRpcService.class);
            // 5.3 注册服务
            server.registerService(new NeptuneServiceWrapper()
                                         .setService(bean)
                                         .setGroup(rpcService.group())
                                         .setToken(rpcService.serviceToken())
                                         .setLimit(rpcService.limit()));
            log.info(">>>>>>>>>>>>>>> [Neptune RPC] 成功提供服务接口 {}  >>>>>>>>>>>>>>> ", beanName);
        }
        long end = System.currentTimeMillis();
        server.startNeptune();
        log.info(" ================== [{}] 所有服务接口启动完成 {}s ================== ",
                server.getServerConfig().getApplication(), ((double) end-(double) start)/1000);
    }

    private void displayBanner(){
        log.info("=================================================================================");
        log.info("||------------------- Neptune-RPC-Framework 1.0 Starting Now ------------------||");
        log.info("=================================================================================");
    }
}
