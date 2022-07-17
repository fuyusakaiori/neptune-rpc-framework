package org.nep.rpc.framework.core.common.hash;

/**
 * <h3>哈希函数</h3>
 */
public interface NeptuneRpcHashFunction {
    /**
     * <h3>根据结点名称确定索引</h3>
     */
    long hash(String key);
}
