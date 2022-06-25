package org.nep.rpc.framework.core.common.cache;

import org.nep.rpc.framework.core.common.constant.ClientConfigConstant;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <h3>缓冲区</h3>
 */
public class NeptuneRpcMessageCache {

    public static final BlockingQueue<NeptuneRpcInvocation> SEND_MESSAGE_QUEUE
            = new ArrayBlockingQueue<>(ClientConfigConstant.SEND_MESSAGE_QUEUE_SIZE);

    public static final Map<String, Object> RESPONSE_CACHE = new ConcurrentHashMap<>();
}
