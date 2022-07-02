package org.nep.rpc.framework.core.serialize.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.nep.rpc.framework.core.common.util.StreamUtil.close;

/**
 * <h3>Hessian 序列化</h3>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class HessianSerializer implements INeptuneSerializer
{
    @Override
    public <T> byte[] serialize(T source) {
        byte[] target = null;
        ByteArrayOutputStream output = null;
        Hessian2Output hessian = null;
        try {
            // 1. 初始化流对象
            output = new ByteArrayOutputStream();
            hessian = new Hessian2Output(output);
            // 2. 序列化对象
            hessian.writeObject(source);
            hessian.getBytesOutputStream().flush();
            hessian.completeMessage();
            hessian.close();
            // 3. 获取序列化后的字节数组
            target = output.toByteArray();
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Hessian 序列化异常", e);
        } finally {
            close(output);
        }
        return target;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        Object target = null;
        Hessian2Input hession = null;
        try {
            hession = new Hessian2Input(
                    new ByteArrayInputStream(source));
            target = hession.readObject();
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Hessian 反序列化异常", e);
        } finally {
            try {
                if (hession != null)
                    hession.close();
            } catch (IOException e) {
                log.error("[Neptune RPC Serialize]: Hessian 关闭流对象异常", e);
            }
        }

        return (T) target;
    }
}
