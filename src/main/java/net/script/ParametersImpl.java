package net.script;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameters implementation used by ParamsExtended.
 *
 * @see ParamsExtended for more information on use case
 * @author Adrian Fijałkowski
 */
final class ParametersImpl implements Parameters {

    private final Map<Class<?>, Object> instances = new HashMap<>();
    private final Map<String, Object> namedInstances = new HashMap<>();

    /**
     * Tries to put new parameter into 'named' bucket.
     * When any argument was already registered by this name, fails the tests with appropriate message.
     *
     * @param name name for the parameter
     * @param object object to register under given name
     * @return self
     */
    @Override
    public Parameters addNamed(String name, Object object) {
        final boolean added = namedInstances.put(name, object) == null;
        if (!added) {
            throw new ParameterResolutionException("Argument for " + name + " was already registered");
        }
        return this;
    }

    /**
     * Tries to put new parameter into class type bucket.
     * When any argument was already registered by the class, fails the tests with appropriate message.
     *
     * @param object argument to add to class type bucket
     * @return self
     * @throws ParameterResolutionException when any argument with given type was already present in the bucket
     */
    @Override
    public final Parameters add(Object object) {
        final boolean added = instances.put(object.getClass(), object) == null;
        if (!added) {
            throw new ParameterResolutionException("Argument for " + object.getClass() + " was already registered");
        }
        return this;
    }

    /**
     * Gets the parameter registered in class type bucket.
     * If none is present, a null will be returned, although this should not happen through ParameterResolver.
     *
     * @param clazz class of parameter
     * @param <T> type of class
     * @return parameter under given name from class type bucket
     */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> T get(Class<T> clazz) {
        return (T) instances.get(clazz);
    }

    /**
     * Gets the parameter registered under 'name' from the 'named' bucket.
     * If none is present, a null will be returned, although this should not happen through ParameterResolver.
     *
     * @param name name of the parameter
     * @return parameter under given name from 'named' bucket
     */
    @Override
    public Object get(String name) {
        return namedInstances.get(name);
    }

    /**
     * Checks if parameter is present, either in 'named' parameters, or in class type parameters bucket.
     *
     * @param context ParameterContext
     * @return \c true, if parameter is present, \c false otherwise
     */
    final boolean checkPresence(ParameterContext context) {
        return context.findAnnotation(Name.class)
            .map(named -> {
                /* Check, if parameters is saved in the 'named' bucket */
                final Object argument = namedInstances.get(named.value());
                if (argument == null) {
                    throw new ParameterResolutionException("No named argument registered for: " + named.value());
                }
                return argument;
            })
            .map(argument -> {
                /* Check, if fetched argument has a proper type */
                if (!context.getParameter().getType().isInstance(argument)) {
                    throw new ParameterResolutionException("Parameter registered is of type " + argument.getClass());
                }
                return true;
            })
            .orElseGet(() ->
                /* Fallback to class registered arguments bucket */
                instances.containsKey(context.getParameter().getType()));
    }

    /**
     * Creates a new instance of Arguments, that is unmodifiable.
     *
     * @return new unmodifiable implementation of Arguments interface
     */
    final Parameters readOnly() {
        return new Parameters() {
            @Override
            public Parameters addNamed(String name, Object object) {
                return ParametersImpl.this.addNamed(name, object);
            }

            @Override
            public Parameters add(Object object) {
                throw new UnsupportedOperationException("Cannot modify arguments in the tests.");
            }

            @Override
            public <T> T get(Class<T> clazz) {
                return ParametersImpl.this.get(clazz);
            }

            @Override
            public Object get(String name) {
                return ParametersImpl.this.get(name);
            }
        };
    }
}
