package provider.neptune;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.interfaces.INeptuneService;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class NeptuneRpcService implements INeptuneService
{

    @Override
    public String send(String request) {
        log.info("[Neptune RPC Server]: {}", request);
        return "success";
    }

    @Override
    public String send(int request) {
        log.info("[Neptune RPC Server]: {}", request);
        return "success";
    }

    @Override
    public List<String> receive() {
        return Arrays.asList("first", "second", "third");
    }
}
