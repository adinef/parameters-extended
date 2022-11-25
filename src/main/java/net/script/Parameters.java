package net.script;

/**
 * Interface representing Parameters used and registered throughout tests.
 *
 * @author Adrian Fijałkowski
 */
public interface Parameters {

    Parameters addNamed(String name, Object object);

    Parameters add(Object object);

    <T> T get(Class<T> clazz);

    Object get(String name);
}
