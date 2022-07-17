package org.nep.rpc.framework.core.protocol;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * <h3>Neptune RPC Request</h3>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NeptuneRpcInvocation implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    // 0. 请求序列号
    private String uuid;
    // 1. 调用的目标方法
    private String method;
    // 2. 调用的目标类
    private String service;
    // 3. 方法携带的参数
    private Object[] args;
    // 4. 参数类型
    private Class<?>[] types;
}
