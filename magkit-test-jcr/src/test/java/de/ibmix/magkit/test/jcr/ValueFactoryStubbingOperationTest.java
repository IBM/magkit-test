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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testing ValueFactoryStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class ValueFactoryStubbingOperationTest {

    private ValueFactory _factory;

    @Before
    public void setUp() {
        _factory = mock(ValueFactory.class);
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueString() throws RepositoryException {
        String value1 = "test";
        String value2 = "other";
        assertThat(_factory.createValue(value1), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(value1).of(_factory);
        assertThat(_factory.createValue(value1), notNullValue());
        assertThat(_factory.createValue(value2), nullValue());
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueBoolean() throws RepositoryException {
        assertThat(_factory.createValue(true), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(true).of(_factory);
        assertThat(_factory.createValue(true), notNullValue());
        assertThat(_factory.createValue(false), nullValue());
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueCalendar() throws RepositoryException {
        Calendar cal1 = Calendar.getInstance();
        assertThat(_factory.createValue(cal1), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(cal1).of(_factory);
        assertThat(_factory.createValue(cal1), notNullValue());
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueDouble() throws RepositoryException {
        double value1 = 1D;
        double value2 = 2D;
        assertThat(_factory.createValue(value1), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(value1).of(_factory);
        assertThat(_factory.createValue(value1), notNullValue());
        assertThat(_factory.createValue(value2), nullValue());
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueLong() throws RepositoryException {
        long value1 = 1L;
        long value2 = 2L;
        assertThat(_factory.createValue(value1), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(value1).of(_factory);
        assertThat(_factory.createValue(value1), notNullValue());
        assertThat(_factory.createValue(value2), nullValue());
    }

    /**
     * Test of stubCreateValue method, of class ValueFactoryStubbingOperation.
     */
    @Test
    public void testStubCreateValueBinary() throws RepositoryException {
        Binary value1 = Mockito.mock(Binary.class);
        Binary value2 = Mockito.mock(Binary.class);
        assertThat(_factory.createValue(value1), nullValue());

        ValueFactoryStubbingOperation.stubCreateValue(value1).of(_factory);
        assertThat(_factory.createValue(value1), notNullValue());
        assertThat(_factory.createValue(value2), nullValue());
    }
}
