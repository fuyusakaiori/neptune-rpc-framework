package org.nep.rpc.framework.core.protocol;


import lombok.*;

import java.io.Serializable;

/**
 * <h3>Neptune RPC Response</h3>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class NeptuneRpcResponse implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;
    private String uuid;
    private int code;
    private String message;
    private Object body;

}
