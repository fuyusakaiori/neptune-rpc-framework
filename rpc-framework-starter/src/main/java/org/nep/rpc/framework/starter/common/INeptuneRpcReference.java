package org.nep.rpc.framework.starter.common;

import java.lang.annotation.*;

/**
 * <h3>客户端使用</h3>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface INeptuneRpcReference {

    String url() default "";

    String group() default "default";

    String token() default "";

    // TODO 以下内容暂时不支持
    int timeOut() default 3000;

    int retry() default 1;

    boolean async() default false;

}
