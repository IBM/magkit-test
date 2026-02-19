package de.ibmix.magkit.test.servlet;

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

import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.servlet.ServletContextStubbingOperation.stubAttribute;
import static de.ibmix.magkit.test.servlet.ServletContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.servlet.ServletContextStubbingOperation.stubInitParameter;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockServletContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing ServletContextStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 18.03.2011
 */
public class ServletContextStubbingOperationTest {

    private ServletContext _context;

    @BeforeEach
    public void setUp() {
        _context = mockServletContext();
    }

    @Test
    public void testStubContextPath() {
        stubContextPath("path").of(_context);
        assertEquals("path", _context.getContextPath());
    }

    @Test
    public void testStubContextPathForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubContextPath("path").of(null));
    }

    @Test
    public void stubAttributeTest() {
        Object value1 = new Object();
        Object value2 = new Object();
        assertNull(_context.getAttribute("name_1"));
        assertNull(_context.getAttribute("name_2"));
        assertFalse(_context.getAttributeNames().hasMoreElements());

        stubAttribute("name_1", value1).of(_context);
        assertEquals(value1, _context.getAttribute("name_1"));
        assertNotNull(_context.getAttributeNames());
        assertTrue(_context.getAttributeNames().hasMoreElements());
        assertEquals("name_1", _context.getAttributeNames().nextElement());
        assertFalse(_context.getAttributeNames().hasMoreElements());

        stubAttribute("name_2", value2).of(_context);
        assertEquals(value2, _context.getAttribute("name_2"));
        assertNotNull(_context.getAttributeNames());
        assertTrue(_context.getAttributeNames().hasMoreElements());
        assertEquals("name_1", _context.getAttributeNames().nextElement());
        assertTrue(_context.getAttributeNames().hasMoreElements());
        assertEquals("name_2", _context.getAttributeNames().nextElement());
        assertFalse(_context.getAttributeNames().hasMoreElements());
    }

    @Test
    public void stubInitParameterTest() {
        String value1 = "value_1";
        String value2 = "value_2";
        assertNull(_context.getInitParameter("name_1"));
        assertNull(_context.getInitParameter("name_2"));
        assertFalse(_context.getInitParameterNames().hasMoreElements());

        stubInitParameter("name_1", value1).of(_context);
        assertEquals(value1, _context.getInitParameter("name_1"));
        assertNotNull(_context.getInitParameterNames());
        assertTrue(_context.getInitParameterNames().hasMoreElements());
        assertEquals("name_1", _context.getInitParameterNames().nextElement());
        assertFalse(_context.getInitParameterNames().hasMoreElements());

        stubInitParameter("name_2", value2).of(_context);
        assertEquals(value2, _context.getInitParameter("name_2"));
        assertNotNull(_context.getInitParameterNames());
        assertTrue(_context.getInitParameterNames().hasMoreElements());
        assertEquals("name_1", _context.getInitParameterNames().nextElement());
        assertTrue(_context.getInitParameterNames().hasMoreElements());
        assertEquals("name_2", _context.getInitParameterNames().nextElement());
        assertFalse(_context.getInitParameterNames().hasMoreElements());
    }
}
