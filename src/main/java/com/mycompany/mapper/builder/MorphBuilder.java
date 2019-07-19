package fr.milleis.morphit.builder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 *
 * @author ajosse
 */
public class MorphBuilder {

    private final static HashMap<Class, Object> INSTANCES = new HashMap<>();

    public static <T> T get(Class<T> clazz) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            add(clazz, classLoader);
            return (T) INSTANCES.get(clazz);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            System.err.println("Exception in MorphBuilder, " + clazz.getCanonicalName() + "Impl cannot be constructed");
        }
        return null;
    }

    private static <T> void add(Class<T> clazz, ClassLoader classLoader) throws IllegalAccessException, InstantiationException, InvocationTargetException, IllegalArgumentException, SecurityException, ClassNotFoundException {
        if (!INSTANCES.containsKey(clazz)) {
            Class<?> loadClass = classLoader.loadClass(clazz.getCanonicalName() + "Impl");
            Constructor<T> construct = (Constructor<T>) loadClass.getConstructors()[0];
            INSTANCES.put(clazz, (T) construct.newInstance());
        }
    }

}
