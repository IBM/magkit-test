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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubAttribute;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubCharacterEncoding;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubCookie;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubCookies;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubHeader;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubHttpSession;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubIsSecure;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubLocalName;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubLocalPort;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubMethod;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubParameter;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubParameterMap;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubProtocol;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubQueryString;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubRequestUri;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubRequestUrl;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubServerName;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubServerPort;
import static de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation.stubServletContext;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpSession;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Testing HttpServletRequestStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 18.03.11
 */
public class HttpServletRequestStubbingOperationTest {

    private HttpServletRequest _request;

    @BeforeEach
    public void setUp() {
        _request = mockHttpServletRequest();
    }

    @Test
    public void testStubContextPath() {
        assertNull(_request.getContextPath());
        assertNotNull(_request.getSession());
        assertEquals("test", _request.getSession().getId());
        assertNotNull(_request.getSession().getServletContext());

        stubContextPath("path").of(_request);
        assertEquals("path", _request.getContextPath());
        assertEquals("test", _request.getSession().getId());
        assertEquals("path", _request.getSession().getServletContext().getContextPath());

        stubContextPath(null).of(_request);
        assertNull(_request.getContextPath());
    }

    @Test
    public void testStubContextPathForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubContextPath("path").of(null));
    }

    @Test
    public void testStubMethod() {
        stubMethod("some method").of(_request);
        assertEquals("some method", _request.getMethod());

        stubMethod(null).of(_request);
        assertNull(_request.getMethod());
    }

    @Test
    public void testStubMethodForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubMethod("some method").of(null));
    }

    @Test
    public void testStubHeader() {
        stubHeader("name", "value").of(_request);
        assertEquals("value", _request.getHeader("name"));

        stubHeader("name", null).of(_request);
        assertNull(_request.getHeader("name"));
    }

    @Test
    public void testStubHeaderForNullName() {
        assertThrows(IllegalArgumentException.class, () -> stubHeader(null, "value").of(_request));
    }

    @Test
    public void testStubHeaderForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubHeader("name", "value").of(null));
    }

    @Test
    public void testStubQueryString() {
        stubQueryString("value").of(_request);
        assertEquals("value", _request.getQueryString());
    }

    @Test
    public void testStubQueryStringForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubQueryString("value").of(null));
    }

    @Test
    public void testStubHttpSession() {
        HttpSession session = mock(HttpSession.class);
        stubHttpSession(session).of(_request);
        assertSame(session, _request.getSession());
        assertSame(session, _request.getSession(true));
        assertSame(session, _request.getSession(false));
    }

    @Test
    public void testStubHttpSessionForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubHttpSession(mock(HttpSession.class)).of(null));
    }

    @Test
    public void testStubHttpSessionWithContextPath() {
        assertNotNull(_request.getSession());
        stubContextPath("original").of(_request);
        HttpSession session = _request.getSession();
        assertSame(session, _request.getSession());
        assertEquals("original", _request.getContextPath());
        assertEquals("original", _request.getSession().getServletContext().getContextPath());

        session = mockHttpSession(null, stubServletContext());
        stubHttpSession(session).of(_request);
        assertSame(session, _request.getSession());
        assertNull(_request.getContextPath());
    }

    @Test
    public void testStubHttpSessionWithImplicitMocking() {
        assertNotNull(_request.getSession());
        stubContextPath("original").of(_request);
        assertEquals("original", _request.getContextPath());
        stubHttpSession(null, stubServletContext(ServletContextStubbingOperation.stubContextPath("sessionPath"))).of(_request);
        assertNotNull(_request.getSession());
        assertNotNull(_request.getSession().getServletContext());
        assertEquals("sessionPath", _request.getSession().getServletContext().getContextPath());
        assertEquals("sessionPath", _request.getContextPath());
    }

    @Test
    public void testStubParameter() {
        stubParameter("name", "value1", "value2").of(_request);
        assertEquals("value1", _request.getParameter("name"));

        assertNotNull(_request.getParameterValues("name"));
        assertEquals(2, _request.getParameterValues("name").length);
        assertEquals("value1", _request.getParameterValues("name")[0]);
        assertEquals("value2", _request.getParameterValues("name")[1]);

        assertNotNull(_request.getParameterMap());
        assertEquals(1, _request.getParameterMap().size());
        assertNotNull(_request.getParameterMap().get("name"));
        assertEquals("value1", _request.getParameterMap().get("name")[0]);
        assertEquals("value2", _request.getParameterMap().get("name")[1]);

        assertNotNull(_request.getParameterNames());
        assertTrue(_request.getParameterNames().hasMoreElements());
        assertEquals("name", _request.getParameterNames().nextElement());

        stubParameter("name2", "value").of(_request);
        assertNotNull(_request.getParameterMap());
        assertEquals(2, _request.getParameterMap().size());
        assertNotNull(_request.getParameterMap().get("name2"));
        assertEquals("value", _request.getParameterMap().get("name2")[0]);

        Enumeration<String> parameterNames = _request.getParameterNames();
        assertNotNull(parameterNames);
        assertTrue(parameterNames.hasMoreElements());
        assertEquals("name", parameterNames.nextElement());
        assertEquals("name2", parameterNames.nextElement());

        String[] values = null;
        stubParameter("name2", values).of(_request);
        assertEquals(1, _request.getParameterMap().size());
        assertNull(_request.getParameter("name2"));

        parameterNames = _request.getParameterNames();
        assertNotNull(parameterNames);
        assertTrue(parameterNames.hasMoreElements());
        assertEquals("name", parameterNames.nextElement());
        assertFalse(parameterNames.hasMoreElements());
    }

    @Test
    public void testStubParameterMap() {
        assertNull(_request.getParameter("name1"));
        assertNull(_request.getParameter("name2"));

        Map<String, String[]> parameters = new HashMap<>(2);
        parameters.put("name1", new String[]{"value1"});
        parameters.put("name2", new String[]{"value2a", "value2b"});
        stubParameterMap(parameters).of(_request);

        assertEquals("value1", _request.getParameter("name1"));
        assertEquals("value2a", _request.getParameter("name2"));
        assertEquals("value2a", _request.getParameterValues("name2")[0]);
        assertEquals("value2b", _request.getParameterValues("name2")[1]);
    }

    @Test
    public void testStubAttribute() {
        assertNull(_request.getAttribute("test"));
        assertFalse(_request.getAttributeNames().hasMoreElements());

        stubAttribute("test", "test string").of(_request);
        assertEquals("test string", _request.getAttribute("test"));
        assertTrue(_request.getAttributeNames().hasMoreElements());
        assertEquals("test", _request.getAttributeNames().nextElement());

        Calendar now = Calendar.getInstance();
        stubAttribute("now", now).of(_request);
        assertEquals("test string", _request.getAttribute("test"));
        assertEquals(now, _request.getAttribute("now"));
        Enumeration<String> names = _request.getAttributeNames();
        assertTrue(names.hasMoreElements());
        assertEquals("test", names.nextElement());
        assertEquals("now", names.nextElement());

        stubAttribute("test", null).of(_request);
        assertNull(_request.getAttribute("test"));
        assertEquals(now, _request.getAttribute("now"));
        names = _request.getAttributeNames();
        assertTrue(names.hasMoreElements());
        assertEquals("now", names.nextElement());
        assertFalse(names.hasMoreElements());
    }

    @Test
    public void testStubServerName() {
        stubServerName("www.test.de").of(_request);
        assertEquals("www.test.de", _request.getServerName());

        stubServerName(null).of(_request);
        assertNull(_request.getServerName());
    }

    @Test
    public void testStubServerPort() {
        stubServerPort(8080).of(_request);
        assertEquals(8080, _request.getServerPort());
    }

    @Test
    public void testStubServerNameForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubServerName("www.test.de").of(null));
    }

    @Test
    public void testStubServerPortForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubServerPort(8080).of(null));
    }

    @Test
    public void testStubProtocolForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubProtocol("http").of(null));
    }

    @Test
    public void testStubProtocol() {
        stubProtocol("http").of(_request);
        assertEquals("http", _request.getProtocol());

        stubProtocol(null).of(_request);
        assertNull(_request.getProtocol());
    }

    @Test
    public void getLocaleNameTest() {
        assertNull(_request.getLocalName());
        stubLocalName("name").of(_request);
        assertEquals("name", _request.getLocalName());
    }

    @Test
    public void getLocalePortTest() {
        assertEquals(0, _request.getLocalPort());
        stubLocalPort(8765).of(_request);
        assertEquals(8765, _request.getLocalPort());
    }

    @Test
    public void testStubCookie() {
        assertNull(_request.getCookies());
        stubCookie("keks 1", "value 1").of(_request);
        assertNotNull(_request.getCookies());
        assertEquals(1, _request.getCookies().length);
        assertEquals("keks 1", _request.getCookies()[0].getName());
        assertEquals("value 1", _request.getCookies()[0].getValue());

        stubCookie("keks 2", "value 2").of(_request);
        assertNotNull(_request.getCookies());
        assertEquals(2, _request.getCookies().length);
        assertEquals("keks 1", _request.getCookies()[0].getName());
        assertEquals("keks 2", _request.getCookies()[1].getName());

        stubCookies(null).of(_request);
        assertNull(_request.getCookies());
    }

    @Test
    public void stubRequestUriTest() {
        assertNull(_request.getRequestURI());
        stubRequestUri("test/uri").of(_request);
        assertEquals("test/uri", _request.getRequestURI());
    }

    @Test
    public void stubRequestUrlTest() {
        assertNull(_request.getRequestURL());
        stubRequestUrl("test/uri").of(_request);
        assertEquals("test/uri", _request.getRequestURL().toString());
    }

    @Test
    public void stubIsSecureTest() {
        assertFalse(_request.isSecure());
        stubIsSecure(true).of(_request);
        assertTrue(_request.isSecure());
    }

    @Test
    public void stubCharacterEncodingTest() {
        assertNull(_request.getCharacterEncoding());
        stubCharacterEncoding("test-encoding").of(_request);
        assertEquals("test-encoding", _request.getCharacterEncoding());
    }
}
