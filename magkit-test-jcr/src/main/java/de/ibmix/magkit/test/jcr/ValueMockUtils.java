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
 * Utility class for mocking javax.jcr.Value.
 *
 * @author wolf.bubenik
 * @since 04.08.2012
 */
public final class ValueMockUtils {
    public static final Answer<InputStream> STREAM_ANSWER = invocation -> {
        Value value = (Value) invocation.getMock();
        Binary binary = value.getBinary();
        return binary == null ? null : binary.getStream();
    };

    private ValueMockUtils() {
    }

    public static Value mockValue(String value) throws RepositoryException {
        return mockValue(value, PropertyType.STRING);
    }

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
            Binary binary = mockBinary(value);
            when(result.getBinary()).thenReturn(binary);
            doAnswer(STREAM_ANSWER).when(result).getStream();
        }
        return result;
    }

    public static Value mockValue(boolean value) throws RepositoryException {
        return mockValue(Boolean.toString(value), PropertyType.BOOLEAN);
    }

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
            Binary binary = mockBinary(stringValue);
            when(result.getBinary()).thenReturn(binary);
            doAnswer(STREAM_ANSWER).when(result).getStream();
        }
        return result;
    }

    public static Value mockValue(double value) throws RepositoryException {
        return mockValue(Double.toString(value), PropertyType.DOUBLE);
    }

    public static Value mockValue(long value) throws RepositoryException {
        return mockValue(Long.toString(value), PropertyType.LONG);
    }

    public static Value mockValue(Binary value) throws RepositoryException {
        Value result = mock(Value.class);
        String stringValue = value.toString();
        when(result.getBinary()).thenReturn(value);
        when(result.getString()).thenReturn(stringValue);
        doAnswer(STREAM_ANSWER).when(result).getStream();
        when(result.getType()).thenReturn(PropertyType.BINARY);
        return result;
    }

    public static Value mockValue(Node value) throws RepositoryException {
        return mockValue(value.getIdentifier(), PropertyType.REFERENCE);
    }

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
}
