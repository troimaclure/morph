package com.mycompany.mapper.morph;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author ajosse
 */
public class MorphBuilder {

    public static <T> T get(Class<T> clazz) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> loadClass = classLoader.loadClass(clazz.getCanonicalName() + "Impl");
            Constructor<T> construct = (Constructor<T>) loadClass.getConstructors()[0];
            return (T) construct.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {

        }
        return null;
    }

}
