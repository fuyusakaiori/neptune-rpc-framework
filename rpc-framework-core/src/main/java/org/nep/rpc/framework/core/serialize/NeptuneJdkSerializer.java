package org.nep.rpc.framework.core.serialize;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * <h3>JDK 序列化</h3>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class NeptuneJdkSerializer implements INeptuneSerializer
{

    @Override
    public byte[] serialize(Object source) {
        log.info("[neptune rpc serializer]: jdk serialize start");
        byte[] target = null;
        // 1. 初始化流对象
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)){
            // 2. 序列化
            oos.writeObject(source);
            oos.flush();
            // 3. 获取二进制数据
            target = baos.toByteArray();
            log.info("[neptune rpc serializer]: jdk serialize end");
            return target;
        } catch (IOException e) {
            log.error("[neptune rpc serializer]: jdk serialize occurred error", e);
            return target;
        }
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        log.info("[neptune rpc serializer]: jdk deserialize start");
        Object target = null;
        // 1. 初始化流对象
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(source))){
            // 2. 反序列化对象
            target = ois.readObject();
            log.info("[neptune rpc serializer]: jdk deserialize end");
            return (T) target;
        } catch (IOException | ClassNotFoundException e) {
            log.error("[neptune rpc serializer]: jdk deserialize occurred error", e);
            return (T) target;
        }
    }
}
