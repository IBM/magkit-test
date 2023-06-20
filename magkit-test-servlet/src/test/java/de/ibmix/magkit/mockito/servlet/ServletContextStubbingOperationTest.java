package de.ibmix.magkit.mockito.servlet;

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

import javax.servlet.ServletContext;

import static de.ibmix.magkit.mockito.servlet.ServletContextStubbingOperation.stubAttribute;
import static de.ibmix.magkit.mockito.servlet.ServletContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.mockito.servlet.ServletContextStubbingOperation.stubInitParameter;
import static de.ibmix.magkit.mockito.servlet.ServletMockUtils.mockServletContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing ServletContextStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 18.03.2011
 */
public class ServletContextStubbingOperationTest {

    private ServletContext _context;

    @Before
    public void setUp() {
        _context = mockServletContext();
    }

    @Test
    public void testStubContextPath() {
        stubContextPath("path").of(_context);
        assertThat(_context.getContextPath(), is("path"));
    }

    @Test(expected = AssertionError.class)
    public void testStubContextPathForNull() {
        stubContextPath("path").of(null);
    }

    @Test
    public void stubAttributeTest() {
        Object value1 = new Object();
        Object value2 = new Object();
        assertThat(_context.getAttribute("name_1"), nullValue());
        assertThat(_context.getAttribute("name_2"), nullValue());
        assertThat(_context.getAttributeNames().hasMoreElements(), is(false));

        stubAttribute("name_1", value1).of(_context);
        assertThat(_context.getAttribute("name_1"), is(value1));
        assertThat(_context.getAttributeNames(), notNullValue());
        assertThat(_context.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_context.getAttributeNames().nextElement(), is("name_1"));
        assertThat(_context.getAttributeNames().hasMoreElements(), is(false));

        stubAttribute("name_2", value2).of(_context);
        assertThat(_context.getAttribute("name_2"), is(value2));
        assertThat(_context.getAttributeNames(), notNullValue());
        assertThat(_context.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_context.getAttributeNames().nextElement(), is("name_1"));
        assertThat(_context.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_context.getAttributeNames().nextElement(), is("name_2"));
        assertThat(_context.getAttributeNames().hasMoreElements(), is(false));
    }

    @Test
    public void stubInitParameterTest() {
        String value1 = "value_1";
        String value2 = "value_2";
        assertThat(_context.getInitParameter("name_1"), nullValue());
        assertThat(_context.getInitParameter("name_2"), nullValue());
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(false));

        stubInitParameter("name_1", value1).of(_context);
        assertThat(_context.getInitParameter("name_1"), is(value1));
        assertThat(_context.getInitParameterNames(), notNullValue());
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(true));
        assertThat(_context.getInitParameterNames().nextElement(), is("name_1"));
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(false));

        stubInitParameter("name_2", value2).of(_context);
        assertThat(_context.getInitParameter("name_2"), is(value2));
        assertThat(_context.getInitParameterNames(), notNullValue());
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(true));
        assertThat(_context.getInitParameterNames().nextElement(), is("name_1"));
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(true));
        assertThat(_context.getInitParameterNames().nextElement(), is("name_2"));
        assertThat(_context.getInitParameterNames().hasMoreElements(), is(false));
    }
}
