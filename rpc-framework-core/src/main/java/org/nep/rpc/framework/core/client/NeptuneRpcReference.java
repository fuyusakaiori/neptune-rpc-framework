package org.nep.rpc.framework.core.client;


import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * <h3>代理目标对象的引用类</h3>
 */
@Data
public class NeptuneRpcReference {

    private static final String TOKEN = "token";

    private static final String GROUP = "group";

    private static final String URL = "url";

    private static final String TIMEOUT = "timeout";

    private static final String RETRY_TIME = "retry";

    /**
     * <h3>代理的目标 class 对象</h3>
     */
    private Class<?> target;

    /**
     * <h3>携带的附加参数</h3>
     */
    private final Map<String, Object> attachments = new HashMap<>();

    public void setToken(String token){
        this.attachments.put(TOKEN, token);
    }

    public void setUrl(String url){
        this.attachments.put(URL, url);
    }

    public void setGroup(String group){
        this.attachments.put(GROUP, group);
    }

    public void setTimeout(int timeout){
        this.attachments.put(TIMEOUT, timeout);
    }

    public void setRetryTime(int retryTime){
        this.attachments.put(RETRY_TIME, retryTime);
    }

}
