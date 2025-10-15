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

import org.apache.jackrabbit.util.ISO8601;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.math.BigDecimal;
import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Testing ValueMockUtils.
 *
 * @author wolf.bubenik@ibmix.de, completed and refactored by GitHub Copilot
 * @since 2012-08-03
 */
public class ValueMockUtilsTest {

    private static void assertAllNumericConversionsThrow(Value v) {
        expectValueFormatException("getLong()", v::getLong);
        expectValueFormatException("getDouble()", v::getDouble);
        expectValueFormatException("getDecimal()", v::getDecimal);
        expectValueFormatException("getDate()", v::getDate);
        expectValueFormatException("getBoolean() when unsupported", v::getBoolean);
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithString() throws RepositoryException {
        Value v = ValueMockUtils.mockValue("test");
        assertThat(v.getString(), is("test"));
        assertThat(v.getType(), is(PropertyType.STRING));
        assertThat(v.toString(), is("test"));

        Value numeric = ValueMockUtils.mockValue("5.9");
        assertThat(numeric.getString(), is("5.9"));
        assertThat(numeric.getLong(), is(6L));
        assertThat(numeric.getDouble(), is(5.9D));
        assertThat(numeric.getDecimal(), is(new BigDecimal("5.9")));
        assertThat(numeric.getType(), is(PropertyType.STRING));

        Value nullString = ValueMockUtils.mockValue((String) null);
        assertThat(nullString.getString(), nullValue());
        assertThat(nullString.getLong(), is(0L));
        assertThat(nullString.getDouble(), is(0.0D));
        assertThat(nullString.getBoolean(), is(false));

        Value nonNumeric = ValueMockUtils.mockValue("abc");
        assertThat(nonNumeric.getString(), is("abc"));
        expectValueFormatException("non-numeric#getLong()", nonNumeric::getLong);
        expectValueFormatException("non-numeric#getDouble()", nonNumeric::getDouble);
        expectValueFormatException("non-numeric#getDecimal()", nonNumeric::getDecimal);
        expectValueFormatException("non-numeric#getDate()", nonNumeric::getDate);
        assertThat(nonNumeric.getBoolean(), is(false));

        for (String b : new String[]{"true", "false"}) {
            assertThat(ValueMockUtils.mockValue(b).getBoolean(), is(Boolean.valueOf(b)));
        }
        // Non boolean numeric string -> Boolean.valueOf("1") = false
        assertThat(ValueMockUtils.mockValue("1").getBoolean(), is(false));
    }

    /**
     * Numeric string rounding & date conversion variants.
     */
    @Test
    public void testNumericStringRoundingAndDate() throws RepositoryException {
        String[] samples = {"5.4", "5.5", "5", "-5.2"};
        long[] expectedLong = {5L, 6L, 5L, -5L};
        for (int i = 0; i < samples.length; i++) {
            Value v = ValueMockUtils.mockValue(samples[i]);
            assertThat(v.getLong(), is(expectedLong[i]));
            assertThat(v.getDate().getTimeInMillis(), is(expectedLong[i]));
        }
        assertThat(ValueMockUtils.mockValue("-5.2").getDecimal(), is(new BigDecimal("-5.2")));
    }

    @Test
    public void testMockValueWithIsoDateString() throws RepositoryException {
        String iso = "2025-10-09T12:34:56.000Z";
        Value v = ValueMockUtils.mockValue(iso);
        assertThat(v.getDate(), notNullValue());
        expectValueFormatException("iso#getLong()", v::getLong);
        expectValueFormatException("iso#getDouble()", v::getDouble);
        expectValueFormatException("iso#getDecimal()", v::getDecimal);
        assertThat(v.getBoolean(), is(false));
        assertThat(v.toString(), is(iso));
    }

    @Test
    public void testMockValueWithCalendarUnsupportedConversions() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        Value v = ValueMockUtils.mockValue(cal);
        expectValueFormatException("calendar#getBoolean()", v::getBoolean);
        expectValueFormatException("calendar#getBinary()", v::getBinary);
        assertThat(v.getDecimal(), is(new BigDecimal(cal.getTimeInMillis())));
    }

    @Test
    public void testMockValueWithDoubleDecimalAccessor() throws RepositoryException {
        assertThat(ValueMockUtils.mockValue(1D).getDecimal(), is(new BigDecimal("1.0")));
    }

    @Test
    public void testMockValueWithLongDecimalAccessor() throws RepositoryException {
        assertThat(ValueMockUtils.mockValue(1L).getDecimal(), is(new BigDecimal("1")));
    }

    @Test
    public void testMockValueWithBoolean() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(true);
        assertThat(v.getType(), is(PropertyType.BOOLEAN));
        assertThat(v.getBoolean(), is(true));
        assertThat(v.getString(), is("true"));
        assertThat(v.toString(), is("true"));
    }

    @Test
    public void testMockValueWithCalendar() throws RepositoryException {
        Calendar value = Calendar.getInstance();
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v.getDate(), is(value));
        assertThat(v.getLong(), is(value.getTimeInMillis()));
        assertThat(v.getDouble(), is((double) value.getTimeInMillis()));
        assertThat(v.getString(), is(ISO8601.format(value)));
        assertThat(v.getType(), is(PropertyType.DATE));
        assertThat(v.toString(), is(ISO8601.format(value)));

        v = ValueMockUtils.mockValue((Calendar) null);
        assertThat(v.getString(), nullValue());
        assertThat(v.getLong(), is(0L));
        assertThat(v.getDouble(), is(0.0D));
        assertThat(v.getDate(), nullValue());
        assertThat(v.getType(), is(PropertyType.DATE));
        assertThat(v.toString(), is("NULL"));
    }

    @Test
    public void testMockValueWithDouble() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(1D);
        assertThat(v.getDouble(), is(1D));
        assertThat(v.getString(), is("1.0"));
        assertThat(v.getLong(), is(1L));
        assertThat(v.getDate().getTimeInMillis(), is(1L));
        assertThat(v.getType(), is(PropertyType.DOUBLE));
        assertThat(v.toString(), is("1.0"));
    }

    @Test
    public void testMockValueWithLong() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(1L);
        assertThat(v.getLong(), is(1L));
        assertThat(v.getDouble(), is(1D));
        assertThat(v.getString(), is("1"));
        assertThat(v.getDate().getTimeInMillis(), is(1L));
        assertThat(v.getType(), is(PropertyType.LONG));
        assertThat(v.toString(), is("1"));
    }

    @Test
    public void testMockValueWithInputStream() throws RepositoryException {
        Value empty = ValueMockUtils.mockValue(ValueMockUtils.mockBinary(""));
        assertThat(empty.getBinary().getSize(), is(0L));
        assertThat(empty.getString(), is(""));

        Value filled = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("Hallo Wolf"));
        assertThat(filled.getBinary().getSize(), is(10L));
        assertThat(filled.getString(), is("Hallo Wolf"));
        assertThat(filled.toString(), is("Hallo Wolf"));

        Value v2 = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("Testdata"));
        assertThat(v2.getBinary().getSize(), is(8L));
        assertThat(v2.getString(), is("Testdata"));

        assertAllNumericConversionsThrow(filled);
    }

    @Test
    public void testMockValueWithNullBinary() throws RepositoryException {
        Value v = ValueMockUtils.mockValue((Binary) null);
        assertThat(v.getBinary(), nullValue());
        assertThat(v.getStream(), nullValue());
        assertThat(v.getString(), nullValue());
        assertThat(v.toString(), is("NULL"));
        assertAllNumericConversionsThrow(v);
    }

    @Test
    public void testMockValueReferenceNullNode() throws RepositoryException {
        Value v = ValueMockUtils.mockValue((Node) null);
        assertThat(v.getType(), is(PropertyType.REFERENCE));
        assertThat(v.getString(), nullValue());
        assertThat(v.getLong(), is(0L));
        assertThat(v.getDouble(), is(0.0D));
        assertThat(v.getDate(), nullValue());
        assertThat(v.getBoolean(), is(false));
        assertThat(v.toString(), is("NULL"));
    }

    @Test
    public void testMockValueNode() throws RepositoryException {
        Node node = Mockito.mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-1").of(node);
        Value v = ValueMockUtils.mockValue(node);
        assertThat(v.getString(), is("uuid-1"));
        assertThat(v.getType(), is(PropertyType.REFERENCE));
        assertThat(v.toString(), is("uuid-1"));
    }

    @Test
    public void testMockBinaryNull() throws RepositoryException {
        Binary b = ValueMockUtils.mockBinary(null);
        assertThat(b, notNullValue());
        assertThat(b.toString(), notNullValue());
        assertThat(b.getStream(), nullValue());
        assertThat(b.getSize(), is(0L));
    }

    @Test
    public void testValueGetStreamAnswerWithBinary() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("stream-test"));
        assertThat(v.getStream(), notNullValue());
    }

    @Test
    public void testMockValueBooleanUnsupportedNumericConversions() throws RepositoryException {
        Value vTrue = ValueMockUtils.mockValue(true);
        expectValueFormatException("boolean#getLong()", vTrue::getLong);
        expectValueFormatException("boolean#getDouble()", vTrue::getDouble);
        expectValueFormatException("boolean#getDecimal()", vTrue::getDecimal);
        expectValueFormatException("boolean#getDate()", vTrue::getDate);
        Value vFalse = ValueMockUtils.mockValue(false);
        expectValueFormatException("boolean#getLong()", vFalse::getLong);
    }

    @Test
    public void testMockValueReferenceNumericIdentifier() throws RepositoryException {
        Node node = Mockito.mock(Node.class);
        NodeStubbingOperation.stubIdentifier("42").of(node);
        Value v = ValueMockUtils.mockValue(node);
        assertThat(v.getString(), is("42"));
        assertThat(v.getLong(), is(42L));
        assertThat(v.getDouble(), is(42D));
        assertThat(v.getDecimal(), is(new BigDecimal("42")));
        assertThat(v.getDate().getTimeInMillis(), is(42L));
    }

    @Test
    public void testMockValueReferenceNonNumericUnsupportedNumericConversions() throws RepositoryException {
        Node node = Mockito.mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-xyz").of(node);
        Value v = ValueMockUtils.mockValue(node);
        expectValueFormatException("ref#getLong()", v::getLong);
        expectValueFormatException("ref#getDouble()", v::getDouble);
        expectValueFormatException("ref#getDecimal()", v::getDecimal);
        expectValueFormatException("ref#getDate()", v::getDate);
    }

    // Helper methods ---------------------------------------------------------
    private static void expectValueFormatException(String label, ThrowingAction invocation) {
        try {
            invocation.run();
            fail("Expected ValueFormatException for " + label);
        } catch (ValueFormatException expected) {
            // expected path
        } catch (RepositoryException re) {
            fail("Unexpected RepositoryException for " + label + ": " + re.getMessage());
        }
    }

    @FunctionalInterface
    private interface ThrowingAction {
        void run() throws RepositoryException;
    }
}
