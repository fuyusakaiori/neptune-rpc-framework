package org.nep.rpc.framework.registry.url;

/**
 * <h3>存储在注册中心结点的格式</h3>
 */
public interface NeptuneURL
{

    String getApplicationName();

    String getServiceName();

    String getAddress();

    int getPort();
    int getWeight();

    void setApplicationName(String applicationName);

    void setServiceName(String serviceName);

    void setAddress(String address);

    void setPort(int port);

    void setWeight(int weight);

    String toProviderString();

    String toConsumerString();


}
