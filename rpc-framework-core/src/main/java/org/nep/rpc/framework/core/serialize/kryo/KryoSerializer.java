package org.nep.rpc.framework.core.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.esotericsoftware.kryo.serializers.DefaultSerializers;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.K;
import org.nep.rpc.framework.core.protocol.NeptuneRpcInvocation;
import org.nep.rpc.framework.core.serialize.INeptuneSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.nep.rpc.framework.core.common.util.StreamUtil.close;

/**
 * <h3>Kryo 序列化</h3>
 */
@Slf4j
@SuppressWarnings("unchecked")
public class KryoSerializer implements INeptuneSerializer {
    /**
     * <h3>1. Kryo 是非线程安全的 </h3>
     * <h3>2. 采用 ThreadLocal 控制并发序列化</h3>
     * <h3>3. 局部变量确实可以控制, 但是每次序列化都需要创建对象, 浪费性能</h3>
     */
    private static final ThreadLocal<Kryo> KRYO_LOCAL = ThreadLocal.withInitial(()->{
        // 1. 准备 Kryo 对象
        Kryo kryo = new Kryo();
        // 2. 添加注册器
        kryo.register(NeptuneRpcInvocation.class,
                new FieldSerializer<NeptuneRpcInvocation>(kryo, NeptuneRpcInvocation.class));
        kryo.register(Object[].class,
                new DefaultArraySerializers.ObjectArraySerializer(kryo, Object[].class));
        kryo.register(Class.class, new DefaultSerializers.ClassSerializer());
        kryo.register(Class[].class, new DefaultArraySerializers.ObjectArraySerializer(kryo, Class[].class));
        // 3. 设置对象引用
        kryo.setReferences(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object source) {
        byte[] target = null;
        // 1. 准备输出结果
        try (Output output = new Output(new ByteArrayOutputStream())){
            // 2. 序列化
            KRYO_LOCAL.get()
                    .writeClassAndObject(output, source);
            // 3. 从输出对象获取序列化结果
            target = output.toBytes();
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Kryo 序列化出现异常", e);
        }
        return target;
    }

    @Override
    public <T> T deserialize(byte[] source, Class<T> clazz) {
        Object target = null;
        // 1. 获取输入结果
        try (Input input = new Input(new ByteArrayInputStream(source))){
            // 2. 反序列化并且获取结果
            target = KRYO_LOCAL.get()
                             .readClassAndObject(input);
        } catch (Exception e) {
            log.error("[Neptune RPC Serialize]: Kryo 反序列化出现异常", e);
        }
        return (T) target;
    }
}
