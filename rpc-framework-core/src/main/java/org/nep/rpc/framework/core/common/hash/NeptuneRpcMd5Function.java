package org.nep.rpc.framework.core.common.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <h3>MD5 哈希函数</h3>
 */
public class NeptuneRpcMd5Function implements NeptuneRpcHashFunction {

    private static final String functionName = "MD5";
    private final MessageDigest instance;

    public NeptuneRpcMd5Function() {
        try {
            this.instance = MessageDigest.getInstance(functionName);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("[Neptune RPC HashFunction]: 初始化 MD5 哈希函数异常", e);
        }
    }

    @Override
    public long hash(String key) {
        this.instance.reset();
        this.instance.update(key.getBytes());
        byte[] digest = instance.digest();
        long hash = 0;
        for (int index = 0; index < 4; index++) {
            hash <<= 8;
            hash |= ((int) digest[index]) & 0xFF;
        }
        return hash;
    }
}
