package org.nep.rpc.framework.core.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * <h3>LTC 解码器: 防止半包粘包问题</h3>
 */
public class NeptuneRpcFrameDecoder extends LengthFieldBasedFrameDecoder {


    /**
     * <h3>数据包最大长度</h3>
     */
    private static final int MAX_FRAME_LENGTH = 2048;

    /**
     * <h3>从首个字段开始到长度字段偏移量</h3>
     */
    private static final int LENGTH_FIELD_OFFSET = 9;

    /**
     *  <h3>长度字段的大小</h3>
     */
    private static final int LENGTH_FIELD_LENGTH = 4;

    /**
     * <h3>从长度字段开始还有多少个字节到达内容</h3>
     */
    private static final int LENGTH_ADJUSTMENT = 0;

    /**
     * <h3>从头开始去除多少个字节</h3>
     */
    private static final int INITIAL_BYTES_TO_STRIP = 0;

    public NeptuneRpcFrameDecoder(){
        this(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP);
    }

    public NeptuneRpcFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

}
