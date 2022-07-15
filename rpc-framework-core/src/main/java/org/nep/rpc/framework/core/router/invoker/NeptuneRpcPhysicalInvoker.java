package org.nep.rpc.framework.core.router.invoker;

import lombok.Getter;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.common.constant.Separator;

/**
 * <h3>真实结点</h3>
 */
@Getter
public class NeptuneRpcPhysicalInvoker implements NeptuneRpcHashInvoker {
    private final NeptuneRpcInvoker invoker;

    public NeptuneRpcPhysicalInvoker(NeptuneRpcInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public String getKey() {
        return invoker.getAddress() + Separator.COLON + invoker.getPort();
    }
}
