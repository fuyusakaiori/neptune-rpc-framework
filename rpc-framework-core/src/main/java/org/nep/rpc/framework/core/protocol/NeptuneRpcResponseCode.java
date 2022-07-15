package org.nep.rpc.framework.core.protocol;

public enum NeptuneRpcResponseCode {
    SUCCESS(200, "请求处理成功"), FAIL(500, "请求处理失败");

    private int code;

    private String message;

    NeptuneRpcResponseCode(int code, String message)
    {
        this.code = code;
        this.message = message;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
