package org.nep.rpc.framework.core.proxy.javassist;

import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <h3>代理类生成器</h3>
 */
@Slf4j
public class NeptuneProxy {

    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * <h3>如果不同的接口中提供相同的服务, 那么就会出现重复动态代理的情况, 所以提供缓存</h3>
     */
    private static final Map<Class<?>, Object> proxyInstanceCache = new ConcurrentHashMap<>();

    /**
     * <h3>创建代理类</h3>
     * @param classLoader 类加载器
     * @param target 代理的目标对象的 Class 对象
     * @param invocationHandler 增强逻辑
     * @return 代理类
     */
    public static Object newProxyInstance(ClassLoader classLoader, Class<?> target, InvocationHandler invocationHandler) throws Exception {
        // 1. 检查代理的目标对象是否已经被动态代理过
        if (proxyInstanceCache.containsKey(target)){
            log.info("[neptune rpc dynamic proxy]: target class already have dynamic proxy class, return proxy class from cache");
            return proxyInstanceCache.get(target);
        }
        // 2.创建 Class 对象容器用于创建 Class 对象
        ClassPool pool = ClassPool.getDefault();
        // 3. 生成动态代理类的全限定名
        String clazzName = newProxyClassName(target);
        // 4. 创建 Class 对象
        CtClass proxy = pool.makeClass(clazzName);
        // 5. 生成 Method 对象数组
        CtField methods = CtField.make("public static java.lang.reflect.Method[] methods;", proxy);
        proxy.addField(methods);
        // 6. 生成 InvocationHandler 对象: 增强逻辑对象
        CtField handler = CtField.make("public " + InvocationHandler.class.getName() + " handler;", proxy);
        proxy.addField(handler);
        // 7. 生成 Constructor 构造函数
        CtConstructor constructor = new CtConstructor(new CtClass[]{pool.get(InvocationHandler.class.getName())}, proxy);
        // 7.1 给 Proxy 对象中的 InvocationHandler 赋值, $0 表示 this, $1、$2、$3 表示构造器的入参
        constructor.setBody("$0.handler = $1");
        // 7.2 给 Proxy 对象中的 InvocationHandler 设置访问修饰符
        constructor.setModifiers(Modifier.PUBLIC);
        proxy.addConstructor(constructor);
        // 8. 生成默认的空参构造器
        proxy.addConstructor(CtNewConstructor.defaultConstructor(proxy));
        // 9. 代理对象实现接口
        List<Method> newMethods = new ArrayList<>();
        // 9.1 获取代理对象的 Class 对象: 约定代理的对象已经实现接口, 所以这里获取到的也是接口的全限定名
        CtClass targetInterface = pool.get(target.getName());
        // 9.2 让代理对象实现接口
        proxy.addInterface(targetInterface);
        // 9.3 获取接口中所有方法
        Method[] oldMethods = target.getDeclaredMethods();
        // 10. 遍历接口中的方法给每个方法都添加增强逻辑
        for (Method oldMethod : oldMethods) {
            // 10.1 获取方法的返回值类型
            Class<?> returnType = oldMethod.getReturnType();
            // 10.2 获取方法的所有参数类型
            Class<?>[] parameterTypes = oldMethod.getParameterTypes();
            // 10.3 生成方法体
            StringBuilder methodBody = newMethodBody(newMethods, returnType, parameterTypes);
            // 10.7 生成方法签名
            StringBuilder methodSignature = newMethodSignature(oldMethod, returnType, parameterTypes);
            // 10.10 结合方法体和方法签名
            CtMethod method = CtMethod.make(methodSignature.append("{").append(methodBody).append("}").toString(), proxy);
            proxy.addMethod(method);
            newMethods.add(oldMethod);
        }
        // 11. 设置 Proxy 访问权限
        proxy.setModifiers(Modifier.PUBLIC);
        // 12. 设置 Proxy 的类加载器
        Class<?> clazz = proxy.toClass(classLoader, null);
        // 13. 获取此前设置 methods 字段然后设置方法
        clazz.getField("methods").set(null, newMethods.toArray(new Method[0]));
        // 14. 创建代理类对象
        Object instance = clazz.getConstructor(InvocationHandler.class).newInstance(invocationHandler);
        // 15. 填充到哈希表缓冲中
        proxyInstanceCache.put(target, instance);
        return instance;
    }

    /**
     * <h3>生成动态代理类的名字</h3>
     * <h3>class.getName() 获取的就是类的全限定名称</h3>
     */
    private static String newProxyClassName(Class<?> target){
        return String.format("%s$Proxy%d", target.getName(), counter.getAndIncrement());
    }

    /**
     * <h3>生成方法访问修饰符</h3>
     */
    private static String newMethodModifier(Method method){
        int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers)){
            return "public";
        }else if (Modifier.isPrivate(modifiers)){
            return "private";
        }else if (Modifier.isProtected(modifiers)){
            return "protected";
        }else{
            return "";
        }
    }

    /**
     * <h3>生成方法签名</h3>
     */
    private static StringBuilder newMethodSignature(Method oldMethod, Class<?> returnType, Class<?>[] parameterTypes) {
        StringBuilder methodSignature = new StringBuilder();
        methodSignature.append(newMethodModifier(oldMethod))
                .append(" ").append(newVariableType(returnType))
                .append(" ").append(oldMethod.getName())
                .append("(");
        // 10.8 生成方法参数
        for (int idx = 0; idx < parameterTypes.length; idx++) {
            if (idx > 0)
                methodSignature.append(", ");
            methodSignature.append(newVariableType(parameterTypes[idx])).append(" param").append(idx);
        }
        methodSignature.append(")");
        // 10.9 生成方法异常
        Class<?>[] exceptionTypes = oldMethod.getExceptionTypes();
        for (int idx = 0; idx < exceptionTypes.length; idx++) {
            if (idx == 0)
                methodSignature.append(" throws ");
            if (idx > 0)
                methodSignature.append(", ");
            methodSignature.append(newVariableType(exceptionTypes[idx]));
        }
        return methodSignature;
    }

    /**
     * <h3>生成方法体</h3>
     */
    private static StringBuilder newMethodBody(List<Method> newMethods, Class<?> returnType, Class<?>[] parameterTypes) {
        StringBuilder methodBody = new StringBuilder().append("Object[] args = new Object[")
                                           .append(parameterTypes.length)
                                           .append("];");
        // 10.4 给数组中的元素赋值
        for (int idx = 0; idx < parameterTypes.length; idx++) {
            methodBody.append(" args[").append(idx).append("] = ($w)$").append(idx + 1).append(";");
        }
        // 10.5 执行方法并设置返回值
        final String variableName = "ret";
        methodBody.append(" Object ").append(variableName).append(" = handler.invoke(this, methods[").append(newMethods.size()).append("], args);");
        // 10.6 检查方法的返回值是否为 Void
        if (!Void.TYPE.equals(returnType)){
            // 注: 如果返回类型不是 Void, 那么就需要设置返回值
            methodBody.append(" return ").append(asArgument(returnType, variableName)).append(";");
        }
        return methodBody;
    }

    /**
     * <h3>获取返回类型的字符串</h3>
     */
    private static String newVariableType(Class<?> returnType) {
        // 2. 如果是自定义类或者包装类, 那么直接强转返回
        if (!returnType.isArray()){
            return returnType.getName();
        }
        // 3. 如果是数组, 那么拼接返回类型后强转返回
        StringBuilder type = new StringBuilder(returnType.getName());
        while (returnType.isArray()){
            // 注: 可能是多维数组, 所以需要循环判断
            type.append("[]");
            returnType = returnType.getComponentType();
        }
        return type.toString();
    }


    /**
     * <h3>根据返回值类型和变量名称生成对应的代码</h3>
     */
    private static String asArgument(Class<?> returnType, String variableName){
        // 1. 如果是基本数据类型, 那么需要单独处理
        if (returnType.isPrimitive()){
            if (Boolean.TYPE == returnType){
                // 如果返回值为空, 那么默认返回 false; 如果返回值不为空, 那么就拆箱后再返回
                return variableName + " == null ? false : ((Boolean) " + variableName + ").booleanValue()";
            }else if (Integer.TYPE == returnType){
                return variableName + " == null ? (int)0 : ((Integer) " + variableName + ").intValue()";
            }else if (Double.TYPE == returnType){
                return variableName + " == null ? (double)0 : ((Double) " + variableName + ").doubleValue()";
            }else if (Float.TYPE == returnType){
                return variableName + " == null ? (float)0 : ((Float) " + variableName + ").floatValue()";
            }else if (Long.TYPE == returnType){
                return variableName + " == null ? (long)0 : ((Long) " + variableName + ").longValue()";
            }else if (Short.TYPE == returnType){
                return variableName + " == null ? (short)0 : ((Short) " + variableName + ").shortValue()";
            }else if (Character.TYPE == returnType){
                return variableName + " == null ? (char)0 : ((Character) " + variableName + ").charValue()";
            }else if (Byte.TYPE == returnType){
                return variableName + " == null ? (byte)0 : ((Byte) " + variableName + ").byteValue()";
            }
            log.error("[neptune rpc javassist proxy]: can't find return type");
            throw new RuntimeException("[neptune rpc javassist proxy]: can't find return type");
        }
        return "(" + newVariableType(returnType) + ")" + variableName;
    }


}
