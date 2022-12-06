package net.script;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interface representing Parameters used and registered throughout tests.
 *
 * @author Adrian Fijałkowski
 */
public interface Parameters {

    /**
     * Method that sets up the parameters should be annotated with @Setup annotation.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Setup {
    }

    default Parameters add(Object object) {
        return addNamed(object.getClass().getCanonicalName(), object);
    }

    Parameters addNamed(String name, Object object);

    @SuppressWarnings("unchecked")
    default <T> T get(Class<T> clazz) {
        return (T) get(clazz.getCanonicalName());
    }

    Object get(String name);
}
