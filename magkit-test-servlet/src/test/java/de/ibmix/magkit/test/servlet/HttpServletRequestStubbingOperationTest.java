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

import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubAttribute;
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
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testing HttpServletRequestStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 18.03.11
 */
public class HttpServletRequestStubbingOperationTest {

    private HttpServletRequest _request;

    @Before
    public void setUp() {
        _request = mockHttpServletRequest();
    }

    @Test
    public void testStubContextPath() {
        assertThat(_request.getContextPath(), nullValue());
        assertThat(_request.getSession(), notNullValue());
        assertThat(_request.getSession().getId(), is("test"));
        assertThat(_request.getSession().getServletContext(), notNullValue());

        stubContextPath("path").of(_request);
        assertThat(_request.getContextPath(), is("path"));
        assertThat(_request.getSession().getId(), is("test"));
        assertThat(_request.getSession().getServletContext().getContextPath(), is("path"));

        stubContextPath(null).of(_request);
        assertThat(_request.getContextPath(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubContextPathForNull() {
        stubContextPath("path").of(null);
    }

    @Test
    public void testStubMethod() {
        stubMethod("some method").of(_request);
        assertThat(_request.getMethod(), is("some method"));

        stubMethod(null).of(_request);
        assertThat(_request.getMethod(), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubMethodForNull() {
        stubMethod("some method").of(null);
    }

    @Test
    public void testStubHeader() {
        stubHeader("name", "value").of(_request);
        assertThat(_request.getHeader("name"), is("value"));

        stubHeader("name", null).of(_request);
        assertThat(_request.getHeader("name"), nullValue());
    }

    @Test(expected = AssertionError.class)
    public void testStubHeaderForNullName() {
        stubHeader(null, "value").of(_request);
    }

    @Test(expected = AssertionError.class)
    public void testStubHeaderForNull() {
        stubHeader("name", "value").of(null);
    }

    @Test
    public void testStubQueryString() {
        stubQueryString("value").of(_request);
        assertThat(_request.getQueryString(), is("value"));
    }

    @Test(expected = AssertionError.class)
    public void testStubQueryStringForNull() {
        stubQueryString("value").of(null);
    }

    @Test
    public void testStubHttpSession() {
        HttpSession session = mock(HttpSession.class);
        stubHttpSession(session).of(_request);
        assertThat(_request.getSession(), is(session));
        assertThat(_request.getSession(true), is(session));
        assertThat(_request.getSession(false), is(session));
    }

    @Test(expected = AssertionError.class)
    public void testStubHttpSessionForNull() {
        stubHttpSession(mock(HttpSession.class)).of(null);
    }

    @Test
    public void testStubHttpSessionWithContextPath() {
        assertThat(_request.getSession(), notNullValue());
        stubContextPath("original").of(_request);
        HttpSession session = _request.getSession();
        assertThat(_request.getSession(), is(session));
        assertThat(_request.getContextPath(), is("original"));
        assertThat(_request.getSession().getServletContext().getContextPath(), is("original"));

        session = mockHttpSession(null, stubServletContext());
        stubHttpSession(session).of(_request);
        assertThat(_request.getSession(), is(session));
        assertThat(_request.getContextPath(), nullValue());
    }

    @Test
    public void testStubHttpSessionWithImplicitMocking() {
        assertThat(_request.getSession(), notNullValue());
        stubContextPath("original").of(_request);
        assertThat(_request.getContextPath(), is("original"));
        stubHttpSession(null, stubServletContext(ServletContextStubbingOperation.stubContextPath("sessionPath"))).of(
            _request);
        assertThat(_request.getSession(), notNullValue());
        assertThat(_request.getSession().getServletContext(), notNullValue());
        assertThat(_request.getSession().getServletContext().getContextPath(), is("sessionPath"));
        assertThat(_request.getContextPath(), is("sessionPath"));
    }

    @Test
    public void testStubParameter() {
        // verify stubbing of first parameter
        stubParameter("name", "value1", "value2").of(_request);
        assertThat(_request.getParameter("name"), is("value1"));

        assertThat(_request.getParameterValues("name"), notNullValue());
        assertThat(_request.getParameterValues("name").length, is(2));
        assertThat(_request.getParameterValues("name")[0], is("value1"));
        assertThat(_request.getParameterValues("name")[1], is("value2"));

        assertThat(_request.getParameterMap(), notNullValue());
        assertThat(_request.getParameterMap().size(), is(1));
        assertThat(_request.getParameterMap().get("name"), notNullValue());
        assertThat(_request.getParameterMap().get("name")[0], is("value1"));
        assertThat(_request.getParameterMap().get("name")[1], is("value2"));

        assertThat(_request.getParameterNames(), notNullValue());
        assertThat(_request.getParameterNames().hasMoreElements(), is(true));
        assertThat(_request.getParameterNames().nextElement(), is("name"));

        // verify that existing parameters will be kept on successive stubbing.
        stubParameter("name2", "value").of(_request);
        assertThat(_request.getParameterMap(), notNullValue());
        assertThat(_request.getParameterMap().size(), is(2));
        assertThat(_request.getParameterMap().get("name2"), notNullValue());
        assertThat(_request.getParameterMap().get("name2")[0], is("value"));

        Enumeration<String> parameterNames = _request.getParameterNames();
        assertThat(parameterNames, notNullValue());
        assertThat(parameterNames.hasMoreElements(), is(true));
        assertThat(parameterNames.nextElement(), is("name"));
        assertThat(parameterNames.nextElement(), is("name2"));

        // verify that parameters will be removed if stubbed with null value
        String[] values = null;
        stubParameter("name2", values).of(_request);
        assertThat(_request.getParameterMap().size(), is(1));
        assertThat(_request.getParameter("name2"), nullValue());

        parameterNames = _request.getParameterNames();
        assertThat(parameterNames, notNullValue());
        assertThat(parameterNames.hasMoreElements(), is(true));
        assertThat(parameterNames.nextElement(), is("name"));
        assertThat(parameterNames.hasMoreElements(), is(false));
    }

    @Test
    public void testStubParameterMap() {
        assertThat(_request.getParameter("name1"), nullValue());
        assertThat(_request.getParameter("name2"), nullValue());

        Map<String, String[]> parameters = new HashMap<>(2);
        parameters.put("name1", new String[]{"value1"});
        parameters.put("name2", new String[]{"value2a", "value2b"});
        stubParameterMap(parameters).of(_request);

        assertThat(_request.getParameter("name1"), is("value1"));
        assertThat(_request.getParameter("name2"), is("value2a"));
        assertThat(_request.getParameterValues("name2")[0], is("value2a"));
        assertThat(_request.getParameterValues("name2")[1], is("value2b"));
    }

    @Test
    public void testStubAttribute() {
        assertThat(_request.getAttribute("test"), nullValue());
        assertThat(_request.getAttributeNames().hasMoreElements(), is(false));

        stubAttribute("test", "test string").of(_request);
        assertThat(_request.getAttribute("test"), is("test string"));
        assertThat(_request.getAttributeNames().hasMoreElements(), is(true));
        assertThat(_request.getAttributeNames().nextElement(), is("test"));

        Calendar now = Calendar.getInstance();
        stubAttribute("now", now).of(_request);
        assertThat(_request.getAttribute("test"), is("test string"));
        assertThat(_request.getAttribute("now"), is(now));
        Enumeration<String> names = _request.getAttributeNames();
        assertThat(names.hasMoreElements(), is(true));
        assertThat(names.nextElement(), is("test"));
        assertThat(names.nextElement(), is("now"));

        stubAttribute("test", null).of(_request);
        assertThat(_request.getAttribute("test"), nullValue());
        assertThat(_request.getAttribute("now"), is(now));
        names = _request.getAttributeNames();
        assertThat(names.hasMoreElements(), is(true));
        assertThat(names.nextElement(), is("now"));
        assertThat(names.hasMoreElements(), is(false));
    }

    @Test
    public void testStubServerName() {
        stubServerName("www.test.de").of(_request);
        assertThat(_request.getServerName(), is("www.test.de"));

        stubServerName(null).of(_request);
        assertThat(_request.getServerName(), nullValue());
    }

    @Test
    public void testStubServerPort() {
        stubServerPort(8080).of(_request);
        assertThat(_request.getServerPort(), is(8080));
    }

    @Test(expected = AssertionError.class)
    public void testStubServerNameForNull() {
        stubServerName("www.test.de").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubServerPortForNull() {
        stubServerPort(8080).of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubProtocolForNull() {
        stubProtocol("http").of(null);
    }

    @Test
    public void testStubProtocol() {
        stubProtocol("http").of(_request);
        assertThat(_request.getProtocol(), is("http"));

        stubProtocol(null).of(_request);
        assertThat(_request.getProtocol(), nullValue());
    }

    @Test
    public void getLocaleNameTest() {
        assertThat(_request.getLocalName(), nullValue());
        stubLocalName("name").of(_request);
        assertThat(_request.getLocalName(), is("name"));
    }

    @Test
    public void getLocalePortTest() {
        assertThat(_request.getLocalPort(), is(0));
        stubLocalPort(8765).of(_request);
        assertThat(_request.getLocalPort(), is(8765));
    }

    @Test
    public void testStubCookie() {
        assertThat(_request.getCookies(), nullValue());
        stubCookie("keks 1", "value 1").of(_request);
        assertThat(_request.getCookies(), notNullValue());
        assertThat(_request.getCookies().length, is(1));
        assertThat(_request.getCookies()[0].getName(), is("keks 1"));
        assertThat(_request.getCookies()[0].getValue(), is("value 1"));

        stubCookie("keks 2", "value 2").of(_request);
        assertThat(_request.getCookies(), notNullValue());
        assertThat(_request.getCookies().length, is(2));
        assertThat(_request.getCookies()[0].getName(), is("keks 1"));
        assertThat(_request.getCookies()[1].getName(), is("keks 2"));

        stubCookies(null).of(_request);
        assertThat(_request.getCookies(), nullValue());
    }

    @Test
    public void stubRequestUriTest() {
        assertThat(_request.getRequestURI(), nullValue());
        stubRequestUri("test/uri").of(_request);
        assertThat(_request.getRequestURI(), is("test/uri"));
    }

    @Test
    public void stubRequestUrlTest() {
        assertThat(_request.getRequestURL(), nullValue());
        stubRequestUrl("test/uri").of(_request);
        assertThat(_request.getRequestURL().toString(), is("test/uri"));
    }

    @Test
    public void stubIsSecureTest() {
        assertThat(_request.isSecure(), is(false));
        stubIsSecure(true).of(_request);
        assertThat(_request.isSecure(), is(true));
    }
}
