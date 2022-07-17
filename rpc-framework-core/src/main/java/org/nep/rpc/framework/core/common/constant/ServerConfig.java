package org.nep.rpc.framework.core.common.constant;

/**
 * <h3>服务器配置</h3>
 */
public class ServerConfig
{
    // boss 线程池线程数量
    public static final int BOSS_THREAD_COUNT = 4;
    // worker 线程池线程数量
    public static final int WORKER_THREAD_COUNT = 10;
    // 发送方缓冲区大小; 不是滑动窗口大小
    public static final int SEND_BUFFER_SIZE = 16 * 1024;
    // 接收方缓冲区大小; 不是滑动窗口大小
    public static final int RECEIVE_BUFFER_SIZE = 16 * 1024;
    // 阻塞队列大小
    public static final int BACK_LOG_SIZE = 1024;

}
