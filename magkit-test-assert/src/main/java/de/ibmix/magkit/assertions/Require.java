package de.ibmix.magkit.assertions;

/*-
 * #%L
 * Archetype - magkit-test-assert
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

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Assertion helper providing argument and state validation.
 * Uses shared internal helper methods parameterized by an exception factory.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 1.0.9
 */
public abstract class Require {

    public static final Predicate<Object> IS_NULL = Objects::isNull;
    public static final Predicate<CharSequence> IS_EMPTY_STRING = s -> s.length() == 0;
    public static final Predicate<CharSequence> IS_BLANK_STRING = StringUtils::isBlank;
    public static final Predicate<Object[]> IS_EMPTY_ARRAY = a -> a.length == 0;
    public static final Predicate<Iterable<?>> IS_EMPTY_ITERABLE = i -> !i.iterator().hasNext();

    private Require() {
    }

    public static void reject(Predicate condition, Object toTest, String message, final ExceptionFactory ex) {
        if (condition == null) {
            throw ex.create("condition must not be null");
        }
        if (condition.test(toTest)) {
            throw ex.create(message);
        }
    }

    public static void requireNotNull(final Object value, final String message, final ExceptionFactory ex) {
        reject(IS_NULL, value, message, ex);
    }

    public static void requireNotEmpty(final CharSequence value, final String message, final ExceptionFactory ex) {
        requireNotNull(value, message, ex);
        reject(IS_EMPTY_STRING, value, message, ex);
    }

    public static void requireNotBlank(final CharSequence value, final String message, final ExceptionFactory ex) {
        requireNotEmpty(value, message, ex);
        reject(IS_BLANK_STRING, value, message, ex);
    }

    public static void requireNotEmpty(final Object[] value, final String message, final ExceptionFactory ex) {
        requireNotNull(value, message, ex);
        reject(IS_EMPTY_ARRAY, value, message, ex);
    }

    public static void requireNotEmpty(final Iterable<?> value, final String message, final ExceptionFactory ex) {
        requireNotNull(value, message, ex);
        reject(IS_EMPTY_ITERABLE, value, message, ex);
    }

    public static void requireInstanceOf(final Object value, final Class<?> clazz, final String message, final ExceptionFactory ex) {
        requireNotNull(value, message, ex);
        requireNotNull(clazz, "clazz must not be null", ex);
        reject(Predicate.not(clazz::isInstance), value, message, ex);
    }

    /**
     * State assertions throwing IllegalStateException.
     */
    public static final class State {
        private static final ExceptionFactory FACTORY = IllegalStateException::new;

        private State() {
        }

        public static void notNull(final Object obj, final String message) {
            requireNotNull(obj, message, FACTORY);
        }

        public static void notEmpty(final CharSequence str, final String message) {
            requireNotEmpty(str, message, FACTORY);
        }

        public static void notBlank(final CharSequence str, final String message) {
            requireNotBlank(str, message, FACTORY);
        }

        public static void notEmpty(final Iterable<?> iterable, final String message) {
            requireNotEmpty(iterable, message, FACTORY);
        }

        public static void notEmpty(final Object[] array, final String message) {
            requireNotEmpty(array, message, FACTORY);
        }

        public static void isInstanceof(final Object obj, final Class<?> clazz, final String message) {
            requireInstanceOf(obj, clazz, message, FACTORY);
        }

        public static void reject(Predicate<Object> condition, Object toTest, String message) {
            Require.reject(condition, toTest, message, FACTORY);
        }
    }

    /**
     * Argument assertions throwing IllegalArgumentException.
     */
    public static final class Argument {
        private static final ExceptionFactory FACTORY = IllegalArgumentException::new;

        private Argument() {
        }

        public static void notNull(final Object obj, final String message) {
            requireNotNull(obj, message, FACTORY);
        }

        public static void notEmpty(final CharSequence str, final String message) {
            requireNotEmpty(str, message, FACTORY);
        }

        public static void notBlank(final CharSequence str, final String message) {
            requireNotBlank(str, message, FACTORY);
        }

        public static void notEmpty(final Iterable<?> iterable, final String message) {
            requireNotEmpty(iterable, message, FACTORY);
        }

        public static void notEmpty(final Object[] array, final String message) {
            requireNotEmpty(array, message, FACTORY);
        }

        public static void isInstanceof(final Object obj, final Class<?> clazz, final String message) {
            requireInstanceOf(obj, clazz, message, FACTORY);
        }

        public static void reject(Predicate<Object> condition, Object toTest, String message) {
            Require.reject(condition, toTest, message, FACTORY);
        }
    }

    @FunctionalInterface
    public interface ExceptionFactory {
        RuntimeException create(String message);
    }
}
