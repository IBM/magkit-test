package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
 * %%
 * Copyright (C) 2023 IBM iX
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

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.mockito.stubbing.Answer;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility factory for creating Mockito based {@link Value} and {@link Binary} test doubles for JCR related unit tests.
 * <p>
 * The helpers aim to simulate the conversion behaviour defined by the JCR specification for the provided source value.
 * Depending on the input different {@code getXxx()} accessors are stubbed to either return a converted representation
 * or to throw a {@link ValueFormatException} if the conversion would be illegal. This allows concise set up of
 * repository related tests without verbose Mockito boilerplate.
 * <p>
 * Behaviour rules (summary):
 * <ul>
 *   <li>String values: If parsable as ISO8601 date a calendar is provided. If numeric (see {@link NumberUtils#isCreatable(String)}) numeric accessors return appropriate conversions; otherwise they throw {@link ValueFormatException}.</li>
 *   <li>Boolean: Created from its string representation and only the boolean accessor returns a value.</li>
 *   <li>Date: Provides long (millis), double, decimal and string (ISO8601) representations; incompatible conversions throw {@link ValueFormatException}.</li>
 *   <li>Binary: Only {@code getBinary()}, {@code getStream()} (via {@link #STREAM_ANSWER}) and {@code getString()} (its {@code toString()}) are defined, all other conversions throw {@link ValueFormatException}.</li>
 *   <li>Reference (Node): Uses the node identifier string.</li>
 *   <li>{@code toString()} of mocked values delegates to {@code getString()} and returns "NULL" if that is {@code null}.</li>
 * </ul>
 * The factory methods never return {@code null}. Passing {@code null} as source value usually yields a mock whose
 * optional conversions are either absent (returning {@code null}) or will throw a {@link ValueFormatException} in line
 * with JCR behaviour.
 * <p>
 * Thread safety: Returned mocks are stateless after creation (all behaviour is preconfigured) and can be reused
 * across threads in tests if required.
 * <p>
 * Note: All methods declare {@link RepositoryException} to mirror JCR API signatures; the current implementation
 * does not throw it.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-04
 */
public final class ValueMockUtils {

    /**
     * Create a mock {@link Value} of type {@link PropertyType#STRING} with conversion behaviour inferred from the text.
     * Equivalent to calling {@link #mockValue(String, int)} with {@link PropertyType#STRING}.
     *
     * @param value raw string; may be an ISO8601 date or numeric representation to enable additional accessors
     * @return configured mock (never {@code null})
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(String value) throws RepositoryException {
        return mockValue(value, PropertyType.STRING);
    }

    /**
     * Create a mock {@link Value} with an explicit JCR property {@code type} while still inferring additional
     * conversion accessors from the textual representation.
     * <p>
     * Provided behaviour:
     * <ul>
     *   <li>{@code getString()} returns the provided value (may be {@code null}).</li>
     *   <li>If the string is ISO8601 parseable, {@code getDate()} returns that calendar.</li>
     *   <li>If the string is numeric: {@code getLong()}, {@code getDouble()}, {@code getDecimal()} return conversions and
     *       {@code getDate()} returns a calendar with time set to the rounded long value (overrides ISO date).</li>
     *   <li>Otherwise the respective numeric/date accessors throw {@link ValueFormatException} (except {@code getDate()} when ISO parseable).</li>
     *   <li>{@code getBoolean()} returns {@link Boolean#valueOf(String)}.</li>
     *   <li>{@code toString()} delegates to {@code getString()} with the fallback described in {@link #TO_STRING_ANSWER}.</li>
     * </ul>
     *
     * @param value textual source value (may be {@code null})
     * @param type  JCR property type constant to return from {@link Value#getType()}
     * @return configured mock (never {@code null})
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(String value, int type) throws RepositoryException {
        Value result = mock(Value.class);
        when(result.getType()).thenReturn(type);
        if (value != null) {
            when(result.getString()).thenReturn(value);
            Calendar date = ISO8601.parse(value);
            if (date != null) {
                when(result.getDate()).thenReturn(date);
            }
            if (NumberUtils.isCreatable(value)) {
                double doubleNumber = Double.parseDouble(value);
                long longValue = Math.round(doubleNumber);
                when(result.getLong()).thenReturn(longValue);
                when(result.getDouble()).thenReturn(doubleNumber);
                when(result.getDecimal()).thenReturn(new BigDecimal(value));
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(longValue);
                when(result.getDate()).thenReturn(cal);
            } else {
                when(result.getLong()).thenThrow(new ValueFormatException());
                when(result.getDouble()).thenThrow(new ValueFormatException());
                when(result.getDecimal()).thenThrow(new ValueFormatException());
                if (date == null) {
                    when(result.getDate()).thenThrow(new ValueFormatException());
                }
            }
            when(result.getBoolean()).thenReturn(Boolean.valueOf(value));
        }
        doAnswer(TO_STRING_ANSWER).when(result).toString();
        return result;
    }

    /**
     * Create a mock {@link Value} of type {@link PropertyType#BOOLEAN}.
     * Underlying implementation delegates to {@link #mockValue(String, int)} and therefore still provides numeric / date
     * conversions if the boolean literal also matches these (e.g. "1").
     *
     * @param value boolean to mock
     * @return configured boolean value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(boolean value) throws RepositoryException {
        return mockValue(Boolean.toString(value), PropertyType.BOOLEAN);
    }

    /**
     * Create a mock {@link Value} of type {@link PropertyType#DATE} for a calendar instance.
     * Provides millisecond, double, decimal and ISO8601 string representations; incompatible conversions
     * ({@code getBoolean()}, {@code getBinary()}) throw {@link ValueFormatException}.
     *
     * @param value calendar to mock; if {@code null} only the type is defined and all conversions return {@code null} or throw
     * @return configured date value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(Calendar value) throws RepositoryException {
        Value result = mock(Value.class);
        when(result.getType()).thenReturn(PropertyType.DATE);
        if (value != null) {
            String stringValue = ISO8601.format(value);
            when(result.getDate()).thenReturn(value);
            when(result.getLong()).thenReturn(value.getTimeInMillis());
            when(result.getString()).thenReturn(stringValue);
            when(result.getDouble()).thenReturn((double) value.getTimeInMillis());
            when(result.getDecimal()).thenReturn(new BigDecimal(value.getTimeInMillis()));
            when(result.getBoolean()).thenThrow(new ValueFormatException());
            when(result.getBinary()).thenThrow(new ValueFormatException());
        }
        doAnswer(TO_STRING_ANSWER).when(result).toString();
        return result;
    }

    /**
     * Create a mock {@link Value} of type {@link PropertyType#DOUBLE}.
     * Conversion behaviour is delegated to {@link #mockValue(String, int)} and therefore includes numeric/date logic.
     *
     * @param value double value to mock
     * @return configured double value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(double value) throws RepositoryException {
        return mockValue(Double.toString(value), PropertyType.DOUBLE);
    }

    /**
     * Create a mock {@link Value} of type {@link PropertyType#LONG}.
     * Conversion behaviour is delegated to {@link #mockValue(String, int)} and therefore includes numeric/date logic.
     *
     * @param value long value to mock
     * @return configured long value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(long value) throws RepositoryException {
        return mockValue(Long.toString(value), PropertyType.LONG);
    }

    /**
     * Create a mock binary {@link Value} of type {@link PropertyType#BINARY}. Only binary and stream access are supported;
     * all numeric, date and boolean conversions throw {@link ValueFormatException}.
     *
     * @param value binary instance to expose; may be {@code null}
     * @return configured binary value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(Binary value) throws RepositoryException {
        Value result = mock(Value.class);
        String stringValue = value != null ? value.toString() : null;
        when(result.getBinary()).thenReturn(value);
        when(result.getString()).thenReturn(stringValue);
        doAnswer(STREAM_ANSWER).when(result).getStream();
        when(result.getType()).thenReturn(PropertyType.BINARY);
        when(result.getLong()).thenThrow(new ValueFormatException());
        when(result.getDouble()).thenThrow(new ValueFormatException());
        when(result.getDecimal()).thenThrow(new ValueFormatException());
        when(result.getBoolean()).thenThrow(new ValueFormatException());
        when(result.getDate()).thenThrow(new ValueFormatException());
        doAnswer(TO_STRING_ANSWER).when(result).toString();
        return result;
    }

    /**
     * Create a mock {@link Value} of type {@link PropertyType#REFERENCE} using the identifier of the provided node.
     * Delegates to {@link #mockValue(String, int)}.
     *
     * @param value JCR node whose identifier should be used; may be {@code null}
     * @return configured reference value mock
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Value mockValue(Node value) throws RepositoryException {
        return mockValue(value != null ? value.getIdentifier() : null, PropertyType.REFERENCE);
    }

    /**
     * Create a Mockito {@link Binary} test double backed by the UTF-8 bytes of the provided string.
     * The mock returns the byte array length as size and a fresh {@link java.io.ByteArrayInputStream} for every
     * {@link Binary#getStream()} invocation.
     *
     * @param value textual representation; may be {@code null}
     * @return configured binary mock (never {@code null})
     * @throws RepositoryException declared for API symmetry; not thrown
     */
    public static Binary mockBinary(String value) throws RepositoryException {
        Binary result = mock(Binary.class);
        if (value != null) {
            when(result.toString()).thenReturn(value);
            byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
            when(result.getStream()).thenReturn(new ByteArrayInputStream(bytes));
            when(result.getSize()).thenReturn((long) bytes.length);
        }
        return result;
    }

    /**
     * Answer that implements {@link Value#getStream()} by delegating to the currently configured {@link Binary} of the mock.
     * Returns {@code null} if no binary is set.
     */
    public static final Answer<InputStream> STREAM_ANSWER = invocation -> {
        Value value = (Value) invocation.getMock();
        Binary binary = value.getBinary();
        return binary == null ? null : binary.getStream();
    };
    /**
     * Answer used to implement {@link Object#toString()} on the value mock. Delegates to {@link Value#getString()} and
     * returns the literal {@code "NULL"} if the string value is {@code null} to aid debugging of test failures.
     */
    public static final Answer<String> TO_STRING_ANSWER = invocation -> {
        Value value = (Value) invocation.getMock();
        String result = value.getString();
        return result != null ? result : "NULL";
    };

    private ValueMockUtils() {
    }
}
