package org.nep.rpc.framework.core.common.cache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.nep.rpc.framework.core.client.NeptuneRpcInvoker;
import org.nep.rpc.framework.core.protocal.NeptuneRpcInvocation;
import org.nep.rpc.framework.registry.url.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <h3>缓冲区</h3>
 */
@Slf4j
public class NeptuneRpcClientCache {

    /**
     * <h3>1. 动态代理类会封装好消息然后放入阻塞队列中</h3>
     * <h3>2. 异步线程就会从阻塞队列中获取消息然后发送给服务端</h3>
     */
    public static class MessageQueue{
        private static final int SEND_MESSAGE_QUEUE_SIZE = 100;

        private static final BlockingQueue<NeptuneRpcInvocation> MESSAGE_QUEUE =
                new ArrayBlockingQueue<>(SEND_MESSAGE_QUEUE_SIZE);

        public static NeptuneRpcInvocation receive(){
            NeptuneRpcInvocation message = null;
            try {
                message = MESSAGE_QUEUE.take();
            } catch (InterruptedException e) {
                log.error("[Neptune RPC Client]: 客户端异步线程获取消息出现错误", e);
            }
            return message;
        }

        public static NeptuneRpcInvocation receive(long timeout, TimeUnit unit){
            // 1. 判断传入的时间单位是否存在问题
            if (timeout <= 0 || unit == null){
                log.error("[Neptune RPC Client]: 客户端异步线程获取消息的时间单位错误");
                return null;
            }
            // 2. 获取阻塞队列中的消息
            NeptuneRpcInvocation message = null;
            try {
                message = MESSAGE_QUEUE.poll(timeout, unit);
                log.debug("message: {}", message);
            } catch (InterruptedException e) {
                log.error("[Neptune RPC Client]: 客户端异步线程获取消息出现错误", e);
            }
            return message;
        }

        public static void send(NeptuneRpcInvocation message){
            // 1. 判断消息是否为空
            if (message == null){
                log.error("[Neptune RPC Client]: 客户端的代理类将要发送的消息为空");
                return;
            }
            // 2. 发送消息
            try {
                log.debug("message: {}", message);
                MESSAGE_QUEUE.put(message);
            } catch (InterruptedException e) {
                log.error("[Neptune RPC Client]: 客户端的代理类发送消息出翔异常");
            }
        }
    }

    /**
     * <h3>1. 发送消息前会在本地保留消息的序列号</h3>
     * <h3>2. 接收消息的时候就将消息携带的序列号和本地比较, 如果匹配, 那么收到的就是正确的</h3>
     */
    public static class Windows{
        private static final Map<String, Object> RESPONSE = new ConcurrentHashMap<>();

        public static boolean match(String sequence){
            if (sequence == null || !RESPONSE.containsKey(sequence)){
                log.error("[Neptune RPC Client]: 服务端响应错误");
                return false;
            }
            return true;
        }

        public static void put(String key, Object value){
            if (key == null || value == null){
                log.error("[Neptune RPC Client]: 存放的键值对不可以为空");
                return;
            }
            RESPONSE.put(key, value);
        }

        public static Object get(String key){
            if (key == null){
                log.error("[Neptune RPC Client]: 存放的键值对不可以为空");
                return null;
            }
            return RESPONSE.get(key);
        }

    }

    /**
     * <h3>客户端存储订阅的服务（接口）的地址</h3>
     */
    public static class Services {
        private static final List<URL> services = new ArrayList<>();

        public static void subscribe(URL url){
            if (url == null){
                log.error("[Neptune RPC Client]: 客户端订阅的服务不存在");
                return;
            }
            services.add(url);
        }

        public static void cancel(URL url){
            if (url == null){
                log.error("[Neptune RPC Client]: 客户端取消订阅的服务不存在");
                return;
            }
            services.remove(url);
        }

        /**
         * <h3>如果服务提供者的权重之类的属性变了, 那么本地存储的内容就需要变化</h3>
         */
        public static void update(URL url){
            for (int index = 0; index < services.size(); index++) {
                if (services.get(index).equals(url)){
                    services.set(index, url);
                    break;
                }
            }
        }

        public static List<URL> getServices(){
            return services;
        }

    }

    /**
     * <h3>主要负责管理每个服务对应的连接集合</h3>
     */
    public static class Connection{
        // 注: 每个服务都会有多个应用提供, 这里的集合就是客户端和提供服务的应用建立的所有连接
        private static final Map<String, List<NeptuneRpcInvoker>> CONNECTION
                = new ConcurrentHashMap<>();
        public static void connect(String service, NeptuneRpcInvoker wrapper){
            // 1. 查询该服务是否已经有连接集合, 如果没有创建一个
            List<NeptuneRpcInvoker> connections
                    = CONNECTION.getOrDefault(service, new ArrayList<>());
            // 2. 添加新的连接
            connections.add(wrapper);
            // 3. 更新连接集合
            CONNECTION.put(service, connections);
        }

        public static NeptuneRpcInvoker disconnect(String service, String path){
            List<NeptuneRpcInvoker> connections = CONNECTION.get(service);
            if (CollectionUtil.isNotEmpty(connections)){
                for (int index = 0; index < connections.size(); index++) {
                    NeptuneRpcInvoker wrapper = connections.get(index);
                    if (path.equals(wrapper.getAddress() + ":" + wrapper.getPort())){
                        return connections.remove(index);
                    }
                }
            }
            return null;
        }

        public static List<NeptuneRpcInvoker> providers(String service){
            return CONNECTION.getOrDefault(service, new ArrayList<>());
        }
    }
}
