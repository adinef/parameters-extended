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

    private final Map<String, Object> instances = new HashMap<>();

    /**
     * Tries to put new parameter into bucket.
     * When any argument was already registered by this name, fails the tests with appropriate message.
     *
     * @param name name for the parameter
     * @param object object to register under given name
     * @return self
     */
    @Override
    public Parameters addNamed(String name, Object object) {
        if (instances.containsKey(name)) {
            throw new ParameterResolutionException("Argument for " + name + " was already registered");
        }
        instances.put(name, object);
        return this;
    }

    /**
     * Gets the parameter registered under 'name' from the bucket.
     * If none is present, a null will be returned, although this should not happen through ParameterResolver.
     *
     * @param name name of the parameter
     * @return parameter under given name from bucket
     */
    @Override
    public Object get(String name) {
        return instances.get(name);
    }

    /**
     * Checks if parameter is present, either in 'named' parameters, or in class type parameters bucket.
     *
     * @param context ParameterContext
     * @return \c true, if parameter is present, \c false otherwise
     */
    final boolean checkPresence(ParameterContext context) {
        return context.findAnnotation(Named.class)
            .map(named -> {
                /* Check, if parameters is saved in the 'named' bucket */
                final Object argument = instances.get(named.value());
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
                /* Fallback to class registered arguments in the bucket */
                instances.containsKey(context.getParameter().getType().getCanonicalName()));
    }

    /**
     * Creates a new instance of Parameters, that is unmodifiable.
     *
     * @return new unmodifiable implementation of Parameters interface
     */
    final Parameters readOnly() {
        return new Parameters() {

            @Override
            public Parameters addNamed(String name, Object object) {
                throw new UnsupportedOperationException("Cannot modify parameters.");
            }

            @Override
            public Object get(String name) {
                return ParametersImpl.this.get(name);
            }
        };
    }
}
