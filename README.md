# JUnit 5 - parameters extended

By default, in JUnit 5, it is possible to inject parameters into a test method in different ways.
This project extends the possibility in a very simple manner to introduce a new behavior.

## Purpose
In JUnit 5, when a method annotated with `@BeforeAll` is used for creating objects used throughout the tests,
the created instances need to be stored in static fields.

To encapsulate and remove possibility of side-effect, where multiple tests may (for some reason)
modify such variables, this project has been introduced.

Additionally, it adds functionality of named parameters.

## Usage

To use extension, annotate your test class with:
```java
@ExtendWith(ParamsExtended.class)
void TestClass { ... }
```

In set up method annotated with `@BeforeAll`, parameters can be set following way:
```java
import org.junit.jupiter.api.BeforeAll;

@ExtendWith(ParamsExtended.class)
void TestClass {

    @BeforeAll
    static void setUp(Parameters parameters) {
        parameters
                .add(new Integer(5))
                .addNamed("another", "Hello World!");
    }

    /* [...] */
}
```

Then, a test method can intercept the registered parameters:

```java
import org.junit.jupiter.api.Test;

    /* [...] */
    @Test
    void test(Integer unnamed, @Named("another") String named) {/*[...]*/}
    /* [...] */
```
Test method can also directly get `Parameters` instance, 
however modification is only possible in a method annotated with `@BeforeAll`.

```java
import org.junit.jupiter.api.Test;

    /* [...] */
    @Test
    void test2(Parameters parameters) {
        /*
        parameters.add(...) and parameters.addNamed(...) are not allowed,
        as instance injected into the test methods is read only.
        */

        final Integer unnamed = parameters.get(Integer.class);
        final String named = parameters.get("another");
    }
    /* [...] */
```