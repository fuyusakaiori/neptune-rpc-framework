package org.nep.rpc.framework.core.serialize.jdk;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.io.*;

import static org.nep.rpc.framework.core.common.util.StreamUtil.close;

/**
 * <h3>JDK 序列化</h3>
 */
@Slf4j
public class JdkSerializer implements INeptuneSerializer
{

    @Override
    public byte[] serialize(Object source) {
        byte[] target = null;
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            // 1. 初始化流对象
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            // 2. 序列化
            oos.writeObject(source);
            oos.flush();
            // 3. 获取二进制数据
            target = baos.toByteArray();
        } catch (IOException e) {
            log.error("[Neptune RPC Serialize]: JDK 序列化出现异常", e);
        }finally {
            close(baos);
            close(oos);
        }
        return target;
    }

    @Override
    public Object deserialize(byte[] source) {
        Object target = null;
        ObjectInputStream ois = null;
        try {
            // 1. 初始化流对象
            ois = new ObjectInputStream(
                    new ByteArrayInputStream(source));
            // 2. 反序列化对象
            target = ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error("[Neptune RPC Serialize]: JDK 反序列化出现异常", e);
        } finally {
            close(ois);
        }
        return target;
    }
}
