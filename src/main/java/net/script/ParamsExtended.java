package net.script;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

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
 *      @Parameters.Setup
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
public final class ParamsExtended implements ParameterResolver, BeforeAllCallback {

    private final ParametersImpl parameters = new ParametersImpl();

    /**
     * Tries to lookup a method annotated with @Parameters.Setup, that is static and has one parameter of type
     * Parameters.
     *
     * When it is impossible to find such method, an exception with appropriate message is thrown.
     *
     * @param extensionContext ExtensionContext
     * @throws Exception when impossible to invoke setup method, or find a method with proper signature
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        final Class<?> testClass = extensionContext.getRequiredTestClass();
        final Method setupMethod = Arrays.stream(testClass.getDeclaredMethods())
            .filter(method -> method.getAnnotation(Parameters.Setup.class) != null)
            .findFirst()
            .orElseThrow(
                () -> new ParameterResolutionException(
                    "Test class using ParamsExtended extension needs to provide " +
                        "a static method annotated with @Parameters.Setup."));
        if (!Modifier.isStatic(setupMethod.getModifiers())) {
            throw new ParameterResolutionException(
                "Method annotated with @Parameters.Setup needs to be static and take one parameter of type Parameters.");
        }
        if (setupMethod.getParameterCount() != 1 || !setupMethod.getParameterTypes()[0].equals(Parameters.class)) {
            throw new ParameterResolutionException(
                "Method annotated with @Parameters.Setup needs to be static and take one parameter of type Parameters.");
        }
        setupMethod.setAccessible(true);
        setupMethod.invoke(testClass, parameters);
    }

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
        return parameters.checkPresence(parameterContext) || getType(parameterContext).isInstance(parameters);
    }

    /**
     * Injects parameters into test method.
     * If a test method requests Parameters type argument, an unmodifiable (wrapping) Parameters instance is returned.
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
        final Class<?> parameterType = getType(parameterContext);
        if (parameterType.isInstance(parameters)) {
            /* For test methods return an unmodifiable instance */
            return parameters.readOnly();
        }
        return parameterContext.findAnnotation(Named.class)
            .map(Named::value)
            .map(parameters::get)
            .orElseGet(() -> parameters.get(parameterType));
    }

    private Class<?> getType(ParameterContext parameterContext) {
        return parameterContext.getParameter().getType();
    }
}
