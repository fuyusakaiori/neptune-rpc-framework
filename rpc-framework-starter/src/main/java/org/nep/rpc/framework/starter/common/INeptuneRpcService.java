package org.nep.rpc.framework.starter.common;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <h3>服务端使用</h3>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface INeptuneRpcService {

    int limit() default 0;

    String group() default "default";

    String serviceToken() default "";

}
