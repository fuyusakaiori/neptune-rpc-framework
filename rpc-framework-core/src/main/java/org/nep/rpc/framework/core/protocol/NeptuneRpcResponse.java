package org.nep.rpc.framework.core.protocol;


import lombok.*;

import java.io.Serializable;

/**
 * <h3>Neptune RPC Response</h3>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@ToString
public class NeptuneRpcResponse<T> implements Serializable {

    private static final long serialVersionUID = 715745410605631233L;

    private String uuid;

    private int code;

    private String message;

    private T body;


    public static <T> NeptuneRpcResponse<T> success(T body, String uuid){
        NeptuneRpcResponse<T> response = new NeptuneRpcResponse<>();
        response.setUuid(uuid);
        response.setMessage( NeptuneRpcResponseCode.SUCCESS.getMessage());
        response.setCode(NeptuneRpcResponseCode.SUCCESS.getCode());
        response.setBody(body);
        return response;
    }


    public static <T> NeptuneRpcResponse<T> fail(NeptuneRpcResponseCode code){
        NeptuneRpcResponse<T> response = new NeptuneRpcResponse<>();
        response.setMessage(code.getMessage());
        response.setCode(code.getCode());
        return response;
    }

}
