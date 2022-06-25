package org.nep.rpc.framework.core.neptune;

import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.interfaces.IDataService;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class DataService implements IDataService {

    @Override
    public String send(String request) {
        log.info("[Neptune RPC Server]: {}", request);
        return "success";
    }

    @Override
    public List<String> receive() {
        return Arrays.asList("first", "second", "third");
    }
}
