package de.ibmix.magkit.assertions;

/*-
 * #%L
 * magkit-test-assert
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Require} utility class ensuring correct exception types, messages and predicate behavior.
 *
 * @author AI (GitHub Copilot), guided by wolf.bubenik@ibmix.de
 * @since 1.0.9
 */
public class RequireTest {

    private static final Require.ExceptionFactory ILLEGAL_ARGUMENT_FACTORY = IllegalArgumentException::new;

    /**
     * Verifies that providing a null predicate to reject throws immediately with given factory.
     */
    @Test
    public void testRejectNullCondition() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Require.reject(null, "value", "message", ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals("condition must not be null", ex.getMessage());
    }

    /**
     * Verifies reject throws when predicate evaluates to true.
     */
    @Test
    public void testRejectConditionTrue() {
        String msg = "failed";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Require.reject(o -> true, "value", msg, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(msg, ex.getMessage());
    }

    /**
     * Verifies reject does not throw when predicate evaluates to false.
     */
    @Test
    public void testRejectConditionFalse() {
        assertDoesNotThrow(() -> Require.reject(o -> false, "value", "msg", ILLEGAL_ARGUMENT_FACTORY));
    }

    /**
     * Verifies requireNotNull passes non-null and fails for null with provided message.
     */
    @Test
    public void testRequireNotNull() {
        assertDoesNotThrow(() -> Require.requireNotNull("ok", "x", ILLEGAL_ARGUMENT_FACTORY));
        String message = "must not be null";
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotNull(null, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, ex.getMessage());
    }

    /**
     * Verifies requireNotEmpty for CharSequence constraints.
     */
    @Test
    public void testRequireNotEmptyCharSequence() {
        assertDoesNotThrow(() -> Require.requireNotEmpty("a", "x", ILLEGAL_ARGUMENT_FACTORY));
        String message = "empty";
        IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty("", message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exEmpty.getMessage());
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty((CharSequence) null, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exNull.getMessage());
    }

    /**
     * Verifies requireNotEmpty for Object array.
     */
    @Test
    public void testRequireNotEmptyArray() {
        assertDoesNotThrow(() -> Require.requireNotEmpty(new Object[]{"x"}, "x", ILLEGAL_ARGUMENT_FACTORY));
        String message = "array empty";
        IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty(new Object[]{}, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exEmpty.getMessage());
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty((Object[]) null, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exNull.getMessage());
    }

    /**
     * Verifies requireNotEmpty for Iterable.
     */
    @Test
    public void testRequireNotEmptyIterable() {
        List<String> list = new ArrayList<>();
        list.add("a");
        assertDoesNotThrow(() -> Require.requireNotEmpty(list, "x", ILLEGAL_ARGUMENT_FACTORY));
        String message = "iterable empty";
        IllegalArgumentException exEmpty = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty(Collections.emptyList(), message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exEmpty.getMessage());
        IllegalArgumentException exNull = assertThrows(IllegalArgumentException.class, () ->
            Require.requireNotEmpty((Iterable<?>) null, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exNull.getMessage());
    }

    /**
     * Verifies requireInstanceOf for success and failure scenarios including nulls.
     */
    @Test
    public void testRequireInstanceOf() {
        assertDoesNotThrow(() -> Require.requireInstanceOf("abc", String.class, "x", ILLEGAL_ARGUMENT_FACTORY));
        String message = "wrong type";
        IllegalArgumentException exWrong = assertThrows(IllegalArgumentException.class, () ->
            Require.requireInstanceOf("abc", Integer.class, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exWrong.getMessage());
        IllegalArgumentException exValueNull = assertThrows(IllegalArgumentException.class, () ->
            Require.requireInstanceOf(null, String.class, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals(message, exValueNull.getMessage());
        IllegalArgumentException exClazzNull = assertThrows(IllegalArgumentException.class, () ->
            Require.requireInstanceOf("abc", null, message, ILLEGAL_ARGUMENT_FACTORY)
        );
        assertEquals("clazz must not be null", exClazzNull.getMessage());
    }

    /**
     * Verifies Argument nested class throws IllegalArgumentException.
     */
    @Test
    public void testArgumentNestedClass() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Require.Argument.notNull(null, "arg")
        );
        assertEquals("arg", ex.getMessage());
    }

    /**
     * Verifies Argument.notEmpty wrapper method for CharSequence, Iterable and array.
     */
    @Test
    public void testArgumentNotEmptyWrappers() {
        IllegalArgumentException exCharSeq = assertThrows(IllegalArgumentException.class, () ->
            Require.Argument.notEmpty("", "charseq")
        );
        assertEquals("charseq", exCharSeq.getMessage());
        IllegalArgumentException exIterable = assertThrows(IllegalArgumentException.class, () ->
            Require.Argument.notEmpty(Collections.emptyList(), "iterable")
        );
        assertEquals("iterable", exIterable.getMessage());
        IllegalArgumentException exArray = assertThrows(IllegalArgumentException.class, () ->
            Require.Argument.notEmpty(new Object[]{}, "array")
        );
        assertEquals("array", exArray.getMessage());
    }

    /**
     * Verifies Argument.isInstanceof wrapper method.
     */
    @Test
    public void testArgumentIsInstanceofWrapper() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Require.Argument.isInstanceof("x", Integer.class, "incompatible")
        );
        assertEquals("incompatible", ex.getMessage());
    }

    /**
     * Verifies State nested class throws IllegalStateException.
     */
    @Test
    public void testStateNestedClass() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Require.State.notNull(null, "state")
        );
        assertEquals("state", ex.getMessage());
    }

    /**
     * Verifies State.isInstanceof wrapper method.
     */
    @Test
    public void testStateIsInstanceofWrapper() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Require.State.isInstanceof("x", Integer.class, "state type")
        );
        assertEquals("state type", ex.getMessage());
    }

    /**
     * Verifies predicate constants basic behavior.
     */
    @Test
    public void testPredicateConstants() {
        assertTrue(Require.IS_NULL.test(null));
        assertFalse(Require.IS_NULL.test("a"));
        assertTrue(Require.IS_EMPTY_STRING.test(""));
        assertFalse(Require.IS_EMPTY_STRING.test("x"));
        assertTrue(Require.IS_EMPTY_ARRAY.test(new Object[]{}));
        assertFalse(Require.IS_EMPTY_ARRAY.test(new Object[]{"x"}));
        assertTrue(Require.IS_EMPTY_ITERABLE.test(Collections.emptyList()));
        assertFalse(Require.IS_EMPTY_ITERABLE.test(Collections.singletonList("x")));
    }
}
