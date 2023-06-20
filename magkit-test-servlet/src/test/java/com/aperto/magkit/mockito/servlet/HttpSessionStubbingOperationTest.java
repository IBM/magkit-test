package com.aperto.magkit.mockito.servlet;

/*-
 * #%L
 * magkit-test-servlet Magnolia Module
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

import javax.servlet.http.HttpSession;

import static com.aperto.magkit.mockito.servlet.HttpSessionStubbingOperation.stubAttribute;
import static com.aperto.magkit.mockito.servlet.HttpSessionStubbingOperation.stubServletContext;
import static com.aperto.magkit.mockito.servlet.ServletMockUtils.mockHttpSession;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing HttpSessionStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 27.06.2012
 */
public class HttpSessionStubbingOperationTest {

    private HttpSession _session;

    @Before
    public void setUp() {
        _session = mockHttpSession("id");
    }

    @Test
    public void testStubServletContext() {
        assertThat(_session.getServletContext(), notNullValue());

        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        stubServletContext(op1, op2).of(_session);

        assertThat(_session.getServletContext(), notNullValue());
        verify(op1, times(1)).of(_session.getServletContext());
        verify(op2, times(1)).of(_session.getServletContext());
    }

    @Test
    public void testStubAttribute() {
        assertThat(_session.getAttribute("name_1"), nullValue());
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().hasMoreElements(), is(false));

        Object value1 = "value_1";
        Object value2 = "value_2";
        stubAttribute("name_1", value1).of(_session);
        assertThat(_session.getAttribute("not_existing"), nullValue());
        assertThat(_session.getAttribute("name_1"), is(value1));
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_session.getAttributeNames().nextElement(), is("name_1"));
        assertThat(_session.getAttributeNames().hasMoreElements(), is(false));

        stubAttribute("name_2", value2).of(_session);
        assertThat(_session.getAttribute("name_1"), is(value1));
        assertThat(_session.getAttribute("name_2"), is(value2));
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_session.getAttributeNames().nextElement(), is("name_1"));
        assertThat(_session.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_session.getAttributeNames().nextElement(), is("name_2"));
        assertThat(_session.getAttributeNames().hasMoreElements(), is(false));

        stubAttribute("name_2", null).of(_session);
        assertThat(_session.getAttribute("name_1"), is(value1));
        assertThat(_session.getAttribute("name_2"), nullValue());
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_session.getAttributeNames().nextElement(), is("name_1"));
        assertThat(_session.getAttributeNames().hasMoreElements(), is(false));
    }

    @Test
    public void stubLastAccessedTimeTest() {
        assertThat(_session.getLastAccessedTime(), is(0L));

        long time = System.currentTimeMillis();
        HttpSessionStubbingOperation.stubLastAccessedTime(time).of(_session);

        assertThat(_session.getLastAccessedTime(), is(time));
    }

    @Test
    public void stubCreationTimeTest() {
        assertThat(_session.getCreationTime(), is(0L));

        long time = System.currentTimeMillis();
        HttpSessionStubbingOperation.stubCreationTime(time).of(_session);

        assertThat(_session.getCreationTime(), is(time));
    }

    @Test
    public void stubIsNewTest() {
        assertThat(_session.isNew(), is(false));

        HttpSessionStubbingOperation.stubIsNew(true).of(_session);

        assertThat(_session.isNew(), is(true));
    }
}
