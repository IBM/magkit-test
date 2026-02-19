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

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation.stubAttribute;
import static de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation.stubServletContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @BeforeEach
    public void setUp() {
        _session = mockHttpSession("id");
    }

    @Test
    public void testStubServletContext() {
        assertNotNull(_session.getServletContext());

        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        stubServletContext(op1, op2).of(_session);

        assertNotNull(_session.getServletContext());
        verify(op1, times(1)).of(_session.getServletContext());
        verify(op2, times(1)).of(_session.getServletContext());
    }

    @Test
    public void testStubAttribute() {
        assertNull(_session.getAttribute("name_1"));
        assertNotNull(_session.getAttributeNames());
        assertFalse(_session.getAttributeNames().hasMoreElements());

        Object value1 = "value_1";
        Object value2 = "value_2";
        stubAttribute("name_1", value1).of(_session);
        assertNull(_session.getAttribute("not_existing"));
        assertEquals(value1, _session.getAttribute("name_1"));
        assertNotNull(_session.getAttributeNames());
        assertTrue(_session.getAttributeNames().hasMoreElements());
        assertEquals("name_1", _session.getAttributeNames().nextElement());
        assertFalse(_session.getAttributeNames().hasMoreElements());

        stubAttribute("name_2", value2).of(_session);
        assertEquals(value1, _session.getAttribute("name_1"));
        assertEquals(value2, _session.getAttribute("name_2"));
        assertNotNull(_session.getAttributeNames());
        assertTrue(_session.getAttributeNames().hasMoreElements());
        assertEquals("name_1", _session.getAttributeNames().nextElement());
        assertTrue(_session.getAttributeNames().hasMoreElements());
        assertEquals("name_2", _session.getAttributeNames().nextElement());
        assertFalse(_session.getAttributeNames().hasMoreElements());

        stubAttribute("name_2", null).of(_session);
        assertEquals(value1, _session.getAttribute("name_1"));
        assertNull(_session.getAttribute("name_2"));
        assertNotNull(_session.getAttributeNames());
        assertTrue(_session.getAttributeNames().hasMoreElements());
        assertEquals("name_1", _session.getAttributeNames().nextElement());
        assertFalse(_session.getAttributeNames().hasMoreElements());
    }

    @Test
    public void stubLastAccessedTimeTest() {
        assertEquals(0L, _session.getLastAccessedTime());

        long time = System.currentTimeMillis();
        HttpSessionStubbingOperation.stubLastAccessedTime(time).of(_session);

        assertEquals(time, _session.getLastAccessedTime());
    }

    @Test
    public void stubCreationTimeTest() {
        assertEquals(0L, _session.getCreationTime());

        long time = System.currentTimeMillis();
        HttpSessionStubbingOperation.stubCreationTime(time).of(_session);

        assertEquals(time, _session.getCreationTime());
    }

    @Test
    public void stubIsNewTest() {
        assertFalse(_session.isNew());

        HttpSessionStubbingOperation.stubIsNew(true).of(_session);

        assertTrue(_session.isNew());
    }
}
