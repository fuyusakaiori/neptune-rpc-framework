package org.nep.rpc.framework.starter.config;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcClient;
import org.nep.rpc.framework.core.client.NeptuneRpcReference;
import org.nep.rpc.framework.core.proxy.ProxyFactory;
import org.nep.rpc.framework.starter.common.INeptuneRpcReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * <h3>客户端自动装配类</h3>
 */
@Slf4j
public class INeptuneClientAutoConfiguration implements
        BeanPostProcessor, ApplicationListener<ApplicationReadyEvent> {

    private static ProxyFactory proxyFactory;
    /**
     * <h3>客户端</h3>
     */
    private static NeptuneRpcClient client;

    /**
     * <h3>是否需要初始化客户端</h3>
     */
    private volatile boolean needInitClient = false;
    /**
     * <h3>是否已经初始化过客户端的配置</h3>
     */
    private volatile boolean hasInitClientConfig = false;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 1. 获取实例的所有字段
        Field[] fields = bean.getClass().getDeclaredFields();
        // 2. 遍历字段找到有对应注解的字段
        for (Field field : fields) {
            if (field.isAnnotationPresent(INeptuneRpcReference.class)){
                if (!hasInitClientConfig){
                    client = new NeptuneRpcClient();
                    proxyFactory = client.getReference();
                    hasInitClientConfig = true;
                }
                needInitClient = true;
            }
            INeptuneRpcReference rpcReference = field.getAnnotation(INeptuneRpcReference.class);
            field.setAccessible(true);
            try {
                field.set(bean, proxyFactory.getProxy(newRpcReference(field, rpcReference)));
                client.subscribeService(field.getType());
            } catch (IllegalAccessException e) {
                log.error("[Neptune RPC AutoConfig]: 客户端在获取动态代理时出现异常", e);
            }
        }
        return bean;
    }

    private NeptuneRpcReference newRpcReference(Field field, INeptuneRpcReference rpcReference){
        NeptuneRpcReference reference = new NeptuneRpcReference();
        reference.setTarget(field.getType());
        reference.setToken(rpcReference.token());
        reference.setGroup(rpcReference.group());
        reference.setUrl(rpcReference.url());
        reference.setTimeout(rpcReference.timeOut());
        reference.setRetryTime(rpcReference.retry());
        return reference;
    }


    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (needInitClient && Objects.nonNull(client)){
            log.info(" ================== [{}] Neptune RPC Client 启动成功 ================== ",
                    client.getClientConfig().getApplicationName());
            client.startNeptune();
        }
    }
}
