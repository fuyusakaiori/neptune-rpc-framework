package org.nep.rpc.framework.core.server;


import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@ToString
public class NeptuneServiceWrapper {

    /**
     * <h3>暴露的服务</h3>
     */
    private Object service;

    /**
     * <h3>服务所属分组</h3>
     */
    private String group = "default";

    /**
     * <h3>服务鉴权</h3>
     */
    private String token = "";

    /**
     * <h3>服务限流</h3>
     */
    private int limit = 10;

}
