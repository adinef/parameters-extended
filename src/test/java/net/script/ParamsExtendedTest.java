package net.script;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(ParamsExtended.class)
class ParamsExtendedTest {

    private static final String FIRST_NAME = "Hello";
    private static final String SECOND_NAME = "World";

    private static final String NAMED_FIRST_NAME = "Welcome";
    private static final String NAMED_SECOND_NAME = "Again";

    @Parameters.Setup
    static void setUp(Parameters parameters) {
        parameters
            .add(new First(FIRST_NAME))
            .add(new Second(SECOND_NAME))
            .addNamed(NAMED_FIRST_NAME, new First(NAMED_FIRST_NAME))
            .addNamed(NAMED_SECOND_NAME, new Second(NAMED_SECOND_NAME));
    }

    @Test
    void testTestMethodGetsNamedArguments(@Named(NAMED_FIRST_NAME) First first, @Named(NAMED_SECOND_NAME) Second second) {
        assertEquals(NAMED_FIRST_NAME, first.getName());
        assertEquals(NAMED_SECOND_NAME, second.getName());
    }

    @Test
    void testTestMethodReceivesArgumentsAlongsideFirstAndSecond(First first, Second second, Parameters parameters) {
        testTestMethodCanExtractArguments(parameters);
        testTestMethodReceivesFirstAndSecond(first, second);
    }

    @Test
    void testTestMethodCanExtractArguments(Parameters parameters) {
        testTestMethodReceivesFirstAndSecond(parameters.get(First.class), parameters.get(Second.class));
    }

    @Test
    void testTestMethodReceivesFirstAndSecond(First first, Second second) {
        testTestMethodCanReceiveFirst(first);
        testTestMethodCanReceiveSecond(second);
    }

    @Test
    void testTestMethodCanReceiveFirst(First first) {
        assertFirstNameCorrect(first);
    }

    @Test
    void testTestMethodCanReceiveSecond(Second second) {
        assertSecondNameCorrect(second);
    }

    @Test
    void testTestMethodCantModifyArgs(Parameters parameters) {
        assertThrows(UnsupportedOperationException.class, () -> parameters.add(new First("any")));
    }

    private void assertFirstNameCorrect(First first) {
        assertEquals(FIRST_NAME, first.getName());
    }

    private void assertSecondNameCorrect(Second second) {
        assertEquals(SECOND_NAME, second.getName());
    }

    static class First {

        private final String name;

        First(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    static class Second {

        private final String name;

        Second(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
