package org.nep.rpc.framework.core.router.invoker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.nep.rpc.framework.core.common.constant.Separator;

/**
 * <h3>哈希环虚拟结点</h3>
 */
@AllArgsConstructor
@Data
@ToString
public class NeptuneRpcVirtualInvoker implements NeptuneRpcHashInvoker {
    // 1. 虚拟结点对应的真实结点
    private final NeptuneRpcPhysicalInvoker physicalInvoker;
    // 2. 该结点在虚拟结点中的位置
    private final int virtualIndex;


    /**
     * <h3>获取虚拟结点的名字</h3>
     */
    @Override
    public String getKey() {
        return physicalInvoker.getKey() + Separator.LINKER + virtualIndex;
    }

    public boolean isVirtualInvoker(NeptuneRpcPhysicalInvoker invoker){
        return physicalInvoker.getKey().equals(invoker.getKey());
    }
}
