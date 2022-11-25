package net.script;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * This extension allows setting up multiple parameters for tests without actually using static fields in class,
 * that may be subjected to change throughout the tests.
 *
 * Additionally, extends the default parameterized tests in JUnit5 with a possibility of named arguments.
 *
 * Example of usage:
 * <pre>
 * @ExtendWith(ParamsExtended.class)
 * class TestClass {
 *
 *      @BeforeEach
 *      static void setUp(Arguments arguments) {
 *          arguments.addNamed("default", "Hello World");
 *      }
 *
 *      @Test
 *      void test(@Named("default") String testedString) {
 *          assertEquals("Hello World", testedString);
 *      }
 * }
 * </pre>
 *
 * @author Adrian Fijałkowski
 */
public final class ParamsExtended implements ParameterResolver {

    private final ParametersImpl parameters = new ParametersImpl();

    /**
     * Checks if parameter is supported.
     *
     * @param parameterContext ParameterContext
     * @param extensionContext ExtensionContext
     * @return \c true, if parameter is registered either in 'named' bucket, or in class type bucket.
     *         Also, returns \c true, when method request Parameters type argument
     */
    @Override
    public final boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        final Class<?> paramType = getType(parameterContext);
        return parameters.checkPresence(parameterContext) || paramType.isInstance(parameters);
    }

    /**
     * For methods annotated with @BeforeClass, injects Parameters type (if requested) that is modifiable.
     * If a regular test method request Parameters type argument, an unmodifiable (wrapping) Parameters instance is returned.
     *
     * A @Named annotation takes precedence in a check, so if a parameter with given name was registered,
     * it is always returned.
     *
     * @param parameterContext ParameterContext
     * @param extensionContext ExtensionContext
     * @return parameter stored under 'name' or a class type
     */
    @Override
    public final Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        /* First, check if method request Parameters */
        final Class<?> paramType = parameterContext.getParameter().getType();
        if (paramType.isInstance(parameters)) {
            /* When a method annotated with @BeforeClass, return regular instance */
            if (isSetup(parameterContext)) {
                return parameters;
            }
            /* For other methods, returns an unmodifiable instance */
            return parameters.readOnly();
        }
        return parameterContext.findAnnotation(Name.class)
            .map(Name::value)
            .map(parameters::get)
            .orElseGet(() -> parameters.get(paramType));
    }

    private Class<?> getType(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType();
    }

    private boolean isSetup(ParameterContext parameterContext) {
        return parameterContext.getDeclaringExecutable().getDeclaredAnnotation(BeforeAll.class) != null;
    }
}
