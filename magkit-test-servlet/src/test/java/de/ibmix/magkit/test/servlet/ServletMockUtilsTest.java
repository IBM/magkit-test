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

import org.junit.jupiter.api.Test;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockCookie;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpSession;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockPageContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockServletContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test ServletMockUtilsTest.
 *
 * @author wolf.bubenik
 * @since 17.03.2011
 */
public class ServletMockUtilsTest {

    @Test
    public void testMockHttpServletRequest() {
        HttpServletRequestStubbingOperation op1 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequestStubbingOperation op2 = mock(HttpServletRequestStubbingOperation.class);
        HttpServletRequest result = mockHttpServletRequest(op1, op2);
        assertNotNull(result);

        // We do not run into NullPointerException when accessing the attributes and parameters
        assertFalse(result.getAttributeNames().hasMoreElements());
        assertTrue(result.getParameterMap().isEmpty());
        assertFalse(result.getParameterNames().hasMoreElements());
        assertNull(result.getParameterValues("any"));

        // Each request mock has a session mock with the id "test"...
        assertEquals("test", result.getSession().getId());

        // ane each session mock has a servlet context mock:
        assertNotNull(result.getSession().getServletContext());

        // All passed HttpServletRequestStubbingOperation have been executed:
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test
    public void testMockHttpServletRequestForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockHttpServletRequest((HttpServletRequestStubbingOperation[]) null));
    }

    @Test
    public void testMockHttpServletResponse() throws Exception {
        HttpServletResponseStubbingOperation op1 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponseStubbingOperation op2 = mock(HttpServletResponseStubbingOperation.class);
        HttpServletResponse result = mockHttpServletResponse(op1, op2);
        assertNotNull(result);
        assertNotNull(result.getWriter());
        assertNotNull(result.getOutputStream());
        assertEquals("some.test/url", result.encodeRedirectURL("some.test/url"));
        assertEquals("some.test/url", result.encodeURL("some.test/url"));
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test
    public void testMockHttpServletResponseForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockHttpServletResponse((HttpServletResponseStubbingOperation[]) null));
    }

    @Test
    public void testMockServletContext() {
        ServletContextStubbingOperation op1 = mock(ServletContextStubbingOperation.class);
        ServletContextStubbingOperation op2 = mock(ServletContextStubbingOperation.class);
        ServletContext result = mockServletContext(op1, op2);
        assertNotNull(result);
        assertNotNull(result.getAttributeNames());
        assertFalse(result.getAttributeNames().hasMoreElements());
        assertNotNull(result.getInitParameterNames());
        assertFalse(result.getInitParameterNames().hasMoreElements());
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test
    public void testMockServletContextForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockServletContext((ServletContextStubbingOperation[]) null));
    }

    @Test
    public void testMockPageContext() {
        PageContextStubbingOperation op1 = mock(PageContextStubbingOperation.class);
        PageContextStubbingOperation op2 = mock(PageContextStubbingOperation.class);
        PageContext result = mockPageContext(op1, op2);
        assertNotNull(result);
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test
    public void testMockPageContextForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockPageContext((PageContextStubbingOperation[]) null));
    }

    @Test
    public void testMockHttpSession() {
        HttpSessionStubbingOperation op1 = mock(HttpSessionStubbingOperation.class);
        HttpSessionStubbingOperation op2 = mock(HttpSessionStubbingOperation.class);
        HttpSession result = mockHttpSession("id", op1, op2);
        assertNotNull(result);
        assertEquals("id", result.getId());
        assertNotNull(result.getAttributeNames());
        assertFalse(result.getAttributeNames().hasMoreElements());
        verify(op1, times(1)).of(result);
        verify(op2, times(1)).of(result);
    }

    @Test
    public void testMockHttpSessionForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockHttpSession(null, (HttpSessionStubbingOperation[]) null));
    }

    @Test
    public void testMockCookie() {
        CookieStubbingOperation op1 = mock(CookieStubbingOperation.class);
        CookieStubbingOperation op2 = mock(CookieStubbingOperation.class);

        Cookie cookie = mockCookie("cookieName", "cookieValue", op1, op2);
        assertNotNull(cookie);
        assertEquals("cookieName", cookie.getName());
        assertEquals("cookieValue", cookie.getValue());
        verify(op1, times(1)).of(cookie);
        verify(op2, times(1)).of(cookie);
    }
}
