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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.math.BigDecimal;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
        assertEquals("test", v.getString());
        assertEquals(PropertyType.STRING, v.getType());
        assertEquals("test", v.toString());

        Value numeric = ValueMockUtils.mockValue("5.9");
        assertEquals("5.9", numeric.getString());
        assertEquals(6L, numeric.getLong());
        assertEquals(5.9D, numeric.getDouble());
        assertEquals(new BigDecimal("5.9"), numeric.getDecimal());
        assertEquals(PropertyType.STRING, numeric.getType());

        Value nullString = ValueMockUtils.mockValue((String) null);
        assertNull(nullString.getString());
        assertEquals(0L, nullString.getLong());
        assertEquals(0.0D, nullString.getDouble());
        assertFalse(nullString.getBoolean());

        Value nonNumeric = ValueMockUtils.mockValue("abc");
        assertEquals("abc", nonNumeric.getString());
        expectValueFormatException("non-numeric#getLong()", nonNumeric::getLong);
        expectValueFormatException("non-numeric#getDouble()", nonNumeric::getDouble);
        expectValueFormatException("non-numeric#getDecimal()", nonNumeric::getDecimal);
        expectValueFormatException("non-numeric#getDate()", nonNumeric::getDate);
        assertFalse(nonNumeric.getBoolean());

        for (String b : new String[]{"true", "false"}) {
            assertEquals(Boolean.valueOf(b), ValueMockUtils.mockValue(b).getBoolean());
        }
        // Non boolean numeric string -> Boolean.valueOf("1") = false
        assertFalse(ValueMockUtils.mockValue("1").getBoolean());
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
            assertEquals(expectedLong[i], v.getLong());
            assertEquals(expectedLong[i], v.getDate().getTimeInMillis());
        }
        assertEquals(new BigDecimal("-5.2"), ValueMockUtils.mockValue("-5.2").getDecimal());
    }

    @Test
    public void testMockValueWithIsoDateString() throws RepositoryException {
        String iso = "2025-10-09T12:34:56.000Z";
        Value v = ValueMockUtils.mockValue(iso);
        assertNotNull(v.getDate());
        expectValueFormatException("iso#getLong()", v::getLong);
        expectValueFormatException("iso#getDouble()", v::getDouble);
        expectValueFormatException("iso#getDecimal()", v::getDecimal);
        assertFalse(v.getBoolean());
        assertEquals(iso, v.toString());
    }

    @Test
    public void testMockValueWithCalendarUnsupportedConversions() throws RepositoryException {
        Calendar cal = Calendar.getInstance();
        Value v = ValueMockUtils.mockValue(cal);
        expectValueFormatException("calendar#getBoolean()", v::getBoolean);
        expectValueFormatException("calendar#getBinary()", v::getBinary);
        assertEquals(new BigDecimal(cal.getTimeInMillis()), v.getDecimal());
    }

    @Test
    public void testMockValueWithDoubleDecimalAccessor() throws RepositoryException {
        assertEquals(new BigDecimal("1.0"), ValueMockUtils.mockValue(1D).getDecimal());
    }

    @Test
    public void testMockValueWithLongDecimalAccessor() throws RepositoryException {
        assertEquals(new BigDecimal("1"), ValueMockUtils.mockValue(1L).getDecimal());
    }

    @Test
    public void testMockValueWithBoolean() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(true);
        assertEquals(PropertyType.BOOLEAN, v.getType());
        assertTrue(v.getBoolean());
        assertEquals("true", v.getString());
        assertEquals("true", v.toString());
    }

    @Test
    public void testMockValueWithCalendar() throws RepositoryException {
        Calendar value = Calendar.getInstance();
        Value v = ValueMockUtils.mockValue(value);
        assertEquals(value, v.getDate());
        assertEquals(value.getTimeInMillis(), v.getLong());
        assertEquals((double) value.getTimeInMillis(), v.getDouble());
        assertEquals(ISO8601.format(value), v.getString());
        assertEquals(PropertyType.DATE, v.getType());
        assertEquals(ISO8601.format(value), v.toString());

        v = ValueMockUtils.mockValue((Calendar) null);
        assertNull(v.getString());
        assertEquals(0L, v.getLong());
        assertEquals(0.0D, v.getDouble());
        assertNull(v.getDate());
        assertEquals(PropertyType.DATE, v.getType());
        assertEquals("NULL", v.toString());
    }

    @Test
    public void testMockValueWithDouble() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(1D);
        assertEquals(1D, v.getDouble());
        assertEquals("1.0", v.getString());
        assertEquals(1L, v.getLong());
        assertEquals(1L, v.getDate().getTimeInMillis());
        assertEquals(PropertyType.DOUBLE, v.getType());
        assertEquals("1.0", v.toString());
    }

    @Test
    public void testMockValueWithLong() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(1L);
        assertEquals(1L, v.getLong());
        assertEquals(1D, v.getDouble());
        assertEquals("1", v.getString());
        assertEquals(1L, v.getDate().getTimeInMillis());
        assertEquals(PropertyType.LONG, v.getType());
        assertEquals("1", v.toString());
    }

    @Test
    public void testMockValueWithInputStream() throws RepositoryException {
        Value empty = ValueMockUtils.mockValue(ValueMockUtils.mockBinary(""));
        assertEquals(0L, empty.getBinary().getSize());
        assertEquals("", empty.getString());

        Value filled = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("Hallo Wolf"));
        assertEquals(10L, filled.getBinary().getSize());
        assertEquals("Hallo Wolf", filled.getString());
        assertEquals("Hallo Wolf", filled.toString());

        Value v2 = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("Testdata"));
        assertEquals(8L, v2.getBinary().getSize());
        assertEquals("Testdata", v2.getString());

        assertAllNumericConversionsThrow(filled);
    }

    @Test
    public void testMockValueWithNullBinary() throws RepositoryException {
        Value v = ValueMockUtils.mockValue((Binary) null);
        assertNull(v.getBinary());
        assertNull(v.getStream());
        assertNull(v.getString());
        assertEquals("NULL", v.toString());
        assertAllNumericConversionsThrow(v);
    }

    @Test
    public void testMockValueReferenceNullNode() throws RepositoryException {
        Value v = ValueMockUtils.mockValue((Node) null);
        assertEquals(PropertyType.REFERENCE, v.getType());
        assertNull(v.getString());
        assertEquals(0L, v.getLong());
        assertEquals(0.0D, v.getDouble());
        assertNull(v.getDate());
        assertFalse(v.getBoolean());
        assertEquals("NULL", v.toString());
    }

    @Test
    public void testMockValueNode() throws RepositoryException {
        Node node = Mockito.mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-1").of(node);
        Value v = ValueMockUtils.mockValue(node);
        assertEquals("uuid-1", v.getString());
        assertEquals(PropertyType.REFERENCE, v.getType());
        assertEquals("uuid-1", v.toString());
    }

    @Test
    public void testMockBinaryNull() throws RepositoryException {
        Binary b = ValueMockUtils.mockBinary(null);
        assertNotNull(b);
        assertNotNull(b.toString());
        assertNull(b.getStream());
        assertEquals(0L, b.getSize());
    }

    @Test
    public void testValueGetStreamAnswerWithBinary() throws RepositoryException {
        Value v = ValueMockUtils.mockValue(ValueMockUtils.mockBinary("stream-test"));
        assertNotNull(v.getStream());
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
        assertEquals("42", v.getString());
        assertEquals(42L, v.getLong());
        assertEquals(42D, v.getDouble());
        assertEquals(new BigDecimal("42"), v.getDecimal());
        assertEquals(42L, v.getDate().getTimeInMillis());
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
