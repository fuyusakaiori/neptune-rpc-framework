package org.nep.rpc.framework.interfaces;

import java.util.List;

/**
 * <h3>测试使用</h3>
 */
public interface INeptuneService
{

    /**
     * <h3>发送消息</h3>
     */
    String send(String request);

    String send(int request);

    /**
     * <h3>接收消息</h3>
     */
    List<String> receive();
}
