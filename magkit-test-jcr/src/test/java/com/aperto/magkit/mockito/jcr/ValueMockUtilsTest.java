package com.aperto.magkit.mockito.jcr;

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
import java.util.Calendar;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing ValueMockUtils.
 *
 * @author wolf
 */
public class ValueMockUtilsTest {

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithString() throws IllegalStateException, RepositoryException {
        String value = "test";
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getString(), is(value));
        assertThat(v.getType(), is(PropertyType.STRING));

        v = ValueMockUtils.mockValue("5.9");
        assertThat(v, notNullValue());
        assertThat(v.getString(), is("5.9"));
        assertThat(v.getLong(), is(6L));
        assertThat(v.getDouble(), is(5.9D));
        assertThat(v.getType(), is(PropertyType.STRING));

        value = null;
        v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getString(), nullValue());
        assertThat(v.getLong(), is(0L));
        assertThat(v.getDouble(), is(0.0D));
        assertThat(v.getType(), is(PropertyType.STRING));
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithBoolean() throws IllegalStateException, RepositoryException {
        boolean value = true;
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getType(), is(PropertyType.BOOLEAN));
        assertThat(v.getBoolean(), is(value));
        assertThat(v.getString(), is(Boolean.toString(value)));
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithCalendar() throws IllegalStateException, RepositoryException {
        Calendar value = Calendar.getInstance();
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getDate(), is(value));
        assertThat(v.getLong(), is(value.getTimeInMillis()));
        assertThat(v.getDouble(), is((double) value.getTimeInMillis()));
        assertThat(v.getString(), is(ISO8601.format(value)));
        assertThat(v.getType(), is(PropertyType.DATE));

        value = null;
        v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getString(), nullValue());
        assertThat(v.getLong(), is(0L));
        assertThat(v.getDouble(), is(0.0D));
        assertThat(v.getDate(), nullValue());
        assertThat(v.getType(), is(PropertyType.DATE));
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithDouble() throws IllegalStateException, RepositoryException {
        double value = 1D;
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getDouble(), is(value));
        assertThat(v.getString(), is(Double.toString(value)));
        assertThat(v.getLong(), is(1L));
        assertThat(v.getDate().getTimeInMillis(), is(1L));
        assertThat(v.getType(), is(PropertyType.DOUBLE));
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithLong() throws IllegalStateException, RepositoryException {
        long value = 1L;
        Value v = ValueMockUtils.mockValue(value);
        assertThat(v, notNullValue());
        assertThat(v.getLong(), is(value));
        assertThat(v.getDouble(), is(1D));
        assertThat(v.getString(), is(Long.toString(value)));
        assertThat(v.getDate().getTimeInMillis(), is(1L));
        assertThat(v.getType(), is(PropertyType.LONG));
    }

    /**
     * Test of mockValue method, of class ValueMockUtils.
     */
    @Test
    public void testMockValueWithInputStream() throws IllegalStateException, RepositoryException {
        Binary bin = ValueMockUtils.mockBinary("");
        Value v = ValueMockUtils.mockValue(bin);
        assertThat(v, notNullValue());
        assertThat(v.getBinary(), is(bin));
        assertThat(v.getType(), is(PropertyType.BINARY));
        assertThat(v.getBinary().getStream(), notNullValue());
        assertThat(v.getBinary().getSize(), is(0L));
        assertThat(v.getString(), is(""));

        bin = ValueMockUtils.mockBinary("Hallo Wolf");
        v = ValueMockUtils.mockValue(bin);
        assertThat(v, notNullValue());
        assertThat(v.getType(), is(PropertyType.BINARY));
        assertThat(v.getBinary(), notNullValue());
        assertThat(v.getBinary().getStream(), notNullValue());
        assertThat(v.getBinary().getSize(), is(10L));
        assertThat(v.getString(), is("Hallo Wolf"));
    }

    /**
     * Tests mockValue with node.
     *
     * @throws RepositoryException
     */
    @Test
    public void testMockValueNode() throws RepositoryException {
        Node node = Mockito.mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-1").of(node);
        Value v = ValueMockUtils.mockValue(node);
        assertThat(v, notNullValue());
        assertThat(v.getString(), is("uuid-1"));
        assertThat(v.getType(), is(PropertyType.REFERENCE));
    }
}
