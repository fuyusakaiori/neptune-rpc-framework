package org.nep.rpc.framework.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.nep.rpc.framework.core.common.util.StreamUtil.close;

/**
 * <h3>Kryo 序列化</h3>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class KryoSerializer implements INeptuneSerializer
{

    // TODO 注: 可能是为了避免多线程序列化?
    private static final ThreadLocal<Kryo> kryoLocal;

    static {
        kryoLocal = new ThreadLocal<>();
        kryoLocal.set(new Kryo());
    }

    @Override
    public <T> byte[] serialize(T source) {
        byte[] target = null;
        Output output = null;
        try {
            output = new Output(new ByteArrayOutputStream());
            kryoLocal.get().writeClassAndObject(output, source);
            target = output.toBytes();
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Kryo 序列化出现异常", e);
        } finally {
            close(output);
        }
        return target;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        Object target = null;
        Input input = null;
        try {
            input = new Input(new ByteArrayInputStream(source));
            target = kryoLocal.get().readClassAndObject(input);
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Kryo 序列化出现异常", e);
        } finally {
            close(input);
        }
        return (T) target;
    }
}
