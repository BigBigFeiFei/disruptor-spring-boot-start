//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.zlf.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * 后续研究一个自定义类加载器加载指定路径下的jar包
 */
@Slf4j
public class LoaderUtil {

    /**
     * 获取ClassLoader
     *
     * @return
     */
    public static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl != null) {
            return cl;
        } else {
            ClassLoader ccl = LoaderUtil.class.getClassLoader();
            return ccl == null ? ClassLoader.getSystemClassLoader() : ccl;
        }
    }

    /**
     * @param className
     * @param loader
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> initializeClass(final String className, final ClassLoader loader)
            throws ClassNotFoundException {
        return Class.forName(className, true, loader);
    }

    /**
     * @param className
     * @param loader
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadClass(final String className, final ClassLoader loader)
            throws ClassNotFoundException {
        return loader != null ? loader.loadClass(className) : null;
    }

    /**
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class<?> loadSystemClass(final String className) throws ClassNotFoundException {
        try {
            return Class.forName(className, true, ClassLoader.getSystemClassLoader());
        } catch (final Throwable t) {
            log.error("LoaderUtil Couldn't use SystemClassLoader. Trying Class.forName({}).", className, t);
            return Class.forName(className);
        }
    }

    /**
     * @param clazz
     * @param <T>
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static <T> T newInstanceOf(Class<T> clazz) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        try {
            return clazz.getConstructor().newInstance();
        } catch (NoSuchMethodException var2) {
            return clazz.newInstance();
        }
    }

}
