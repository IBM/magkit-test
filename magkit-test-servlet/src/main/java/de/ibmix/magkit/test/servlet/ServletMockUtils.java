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

import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.mockito.stubbing.Answer;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.TreeMap;

import static java.util.Collections.enumeration;
import static org.apache.commons.collections4.IteratorUtils.emptyIterator;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility / factory class providing convenient, consistently pre-configured Mockito based mocks
 * for core Servlet API artifacts: {@link HttpServletRequest}, {@link HttpServletResponse},
 * {@link ServletContext}, {@link PageContext}, {@link HttpSession} and {@link Cookie}.
 * <p>
 * Each factory method returns a new mock instance with sensible defaults applied so that tests
 * can focus only on the behavior under test and override ("stubbings") the defaults where
 * required. Additional behavior can be supplied through dedicated *StubbingOperation strategies
 * passed as varargs. Passing no stubbing operations yields the documented defaults. Passing
 * {@code null} triggers an {@link AssertionError}. If you want only defaults simply call the
 * method without arguments.
 * </p>
 * <p>
 * Thread safety: The produced mocks are <em>not</em> thread-safe; treat each returned instance as
 * test-method local. The class itself is stateless and therefore thread-safe.
 * </p>
 * <p>
 * Typical usage:
 * <pre>{@code
 * HttpServletRequest request = ServletMockUtils.mockHttpServletRequest(
 *     HttpServletRequestStubbingOperation.stubParameter("foo", "bar")
 * );
 * HttpServletResponse response = ServletMockUtils.mockHttpServletResponse();
 * }
 * </pre>
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-04
 */
public final class ServletMockUtils {

    private static final Answer<String> REPLY_PARAMETER_ANSWER = invocation -> (String) invocation.getArguments()[0];

    private static final Answer<String> REQUEST_CONTEXT_PATH_ANSWER = invocationOnMock -> {
        HttpServletRequest request = (HttpServletRequest) invocationOnMock.getMock();
        ServletContext context = request != null ? request.getServletContext() : null;
        return context != null ? context.getContextPath() : null;
    };

    private static final Answer<ServletContext> REQUEST_SERVLET_CONTEXT_ANSWER = invocationOnMock -> {
        HttpServletRequest request = (HttpServletRequest) invocationOnMock.getMock();
        HttpSession session = request != null ? request.getSession() : null;
        return session != null ? session.getServletContext() : null;
    };

    private static final Answer<Enumeration<String>> REQUEST_PARAMETER_NAMES_ANSWER = invocation -> {
        HttpServletRequest request = (HttpServletRequest) invocation.getMock();
        return enumeration(request.getParameterMap().keySet());
    };

    private static final Answer<String[]> REQUEST_PARAMETER_VALUES_ANSWER = invocation -> {
        HttpServletRequest request = (HttpServletRequest) invocation.getMock();
        String key = (String) invocation.getArguments()[0];
        return request.getParameterMap().get(key);
    };

    private static final Answer<String> REQUEST_PARAMETER_ANSWER = invocation -> {
        HttpServletRequest request = (HttpServletRequest) invocation.getMock();
        String key = (String) invocation.getArguments()[0];
        String[] values = request.getParameterMap().get(key);
        return values != null && values.length > 0 ? values[0] : null;
    };

    /**
     * Create a new {@link HttpServletRequest} mock with the following defaults:
     * <ul>
     *     <li>Empty attribute names enumeration.</li>
     *     <li>Empty and mutable parameter map (backed by a {@link TreeMap}).</li>
     *     <li>Working implementations for {@link HttpServletRequest#getParameterNames()},
     *     {@link HttpServletRequest#getParameterValues(String)} and {@link HttpServletRequest#getParameter(String)} that
     *     reflect the (possibly later modified) parameter map.</li>
     *     <li>A default {@link HttpSession} with id "test".</li>
     *     <li>{@link HttpServletRequest#getServletContext()} resolves to the session's servlet context.</li>
     *     <li>{@link HttpServletRequest#getContextPath()} delegates to the servlet context.</li>
     * </ul>
     * Apply further configuration by supplying one or more {@link HttpServletRequestStubbingOperation} instances.
     *
     * @param stubbings additional stubbing operations (must not be {@code null})
     * @return configured request mock
     * @throws AssertionError if {@code stubbings} is {@code null}
     * @see HttpServletRequestStubbingOperation
     * @since 2011-03-04
     */
    public static HttpServletRequest mockHttpServletRequest(HttpServletRequestStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttributeNames()).thenReturn(new IteratorEnumeration<>(emptyIterator()));
        when(request.getParameterMap()).thenReturn(new TreeMap<>());
        doAnswer(REQUEST_PARAMETER_NAMES_ANSWER).when(request).getParameterNames();
        doAnswer(REQUEST_PARAMETER_VALUES_ANSWER).when(request).getParameterValues(anyString());
        doAnswer(REQUEST_PARAMETER_ANSWER).when(request).getParameter(anyString());
        HttpServletRequestStubbingOperation.stubHttpSession("test").of(request);
        doAnswer(REQUEST_SERVLET_CONTEXT_ANSWER).when(request).getServletContext();
        doAnswer(REQUEST_CONTEXT_PATH_ANSWER).when(request).getContextPath();
        for (HttpServletRequestStubbingOperation stubbing : stubbings) {
            stubbing.of(request);
        }
        return request;
    }

    /**
     * Create a new {@link HttpServletResponse} mock with the following defaults:
     * <ul>
     *     <li>Mocked {@link PrintWriter} returned by {@link HttpServletResponse#getWriter()}.</li>
     *     <li>Mocked {@link ServletOutputStream} returned by {@link HttpServletResponse#getOutputStream()}.</li>
     *     <li>{@link HttpServletResponse#encodeRedirectURL(String)} and {@link HttpServletResponse#encodeURL(String)}
     *     simply echo their input parameter.</li>
     * </ul>
     * Additional behavior can be provided through {@link HttpServletResponseStubbingOperation} instances.
     *
     * @param stubbings additional stubbing operations (must not be {@code null})
     * @return configured response mock
     * @throws AssertionError if {@code stubbings} is {@code null}
     * @see HttpServletResponseStubbingOperation
     * @since 2011-03-04
     */
    public static HttpServletResponse mockHttpServletResponse(HttpServletResponseStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpServletResponse response = mock(HttpServletResponse.class);
        try {
            PrintWriter writer = mock(PrintWriter.class);
            when(response.getWriter()).thenReturn(writer);
            ServletOutputStream out = mock(ServletOutputStream.class);
            when(response.getOutputStream()).thenReturn(out);
            when(response.encodeRedirectURL(anyString())).thenAnswer(REPLY_PARAMETER_ANSWER);
            when(response.encodeURL(anyString())).thenAnswer(REPLY_PARAMETER_ANSWER);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        for (HttpServletResponseStubbingOperation stubbing : stubbings) {
            stubbing.of(response);
        }
        return response;
    }

    /**
     * Create a new {@link ServletContext} mock with the following defaults:
     * <ul>
     *     <li>Empty attribute names enumeration.</li>
     *     <li>Empty init parameter names enumeration.</li>
     * </ul>
     * Further configuration can be applied via {@link ServletContextStubbingOperation} instances.
     *
     * @param stubbings additional stubbing operations (must not be {@code null})
     * @return configured servlet context mock
     * @throws AssertionError if {@code stubbings} is {@code null}
     * @see ServletContextStubbingOperation
     * @since 2011-03-04
     */
    public static ServletContext mockServletContext(ServletContextStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        ServletContext context = mock(ServletContext.class);
        when(context.getAttributeNames()).thenReturn(new IteratorEnumeration<>(emptyIterator()));
        when(context.getInitParameterNames()).thenReturn(new IteratorEnumeration<>(emptyIterator()));
        for (ServletContextStubbingOperation stubbing : stubbings) {
            stubbing.of(context);
        }
        return context;
    }

    /**
     * Create a new {@link PageContext} mock with the following defaults:
     * <ul>
     *     <li>An {@link HttpServletRequest} mock (see {@link #mockHttpServletRequest(HttpServletRequestStubbingOperation...)})</li>
     *     <li>An {@link HttpServletResponse} mock (see {@link #mockHttpServletResponse(HttpServletResponseStubbingOperation...)})</li>
     *     <li>{@link PageContext#getSession()} delegates to the underlying request's session.</li>
     *     <li>{@link PageContext#getServletContext()} delegates to the session's servlet context.</li>
     * </ul>
     * Apply further configuration via {@link PageContextStubbingOperation} instances.
     *
     * @param stubbings additional stubbing operations (must not be {@code null})
     * @return configured page context mock
     * @throws AssertionError if {@code stubbings} is {@code null}
     * @see PageContextStubbingOperation
     * @since 2011-03-04
     */
    public static PageContext mockPageContext(PageContextStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        PageContext context = mock(PageContext.class);
        PageContextStubbingOperation.stubHttpServletRequest().of(context);
        PageContextStubbingOperation.stubHttpServletResponse().of(context);
        doAnswer(REQUEST_SESSION_ANSWER).when(context).getSession();
        doAnswer(SESSION_SERVLET_CONTEXT_ANSWER).when(context).getServletContext();
        for (PageContextStubbingOperation stubbing : stubbings) {
            stubbing.of(context);
        }
        return context;
    }

    /**
     * Create a new {@link HttpSession} mock with the following defaults:
     * <ul>
     *     <li>The supplied {@code id} returned by {@link HttpSession#getId()}.</li>
     *     <li>Empty attribute names enumeration.</li>
     *     <li>A mocked {@link ServletContext} (see {@link #mockServletContext(ServletContextStubbingOperation...)})</li>
     * </ul>
     * Additional behavior can be provided through {@link HttpSessionStubbingOperation} instances.
     *
     * @param id session identifier to be returned by {@link HttpSession#getId()}
     * @param stubbings additional stubbing operations (must not be {@code null})
     * @return configured session mock
     * @throws AssertionError if {@code stubbings} is {@code null}
     * @see HttpSessionStubbingOperation
     * @since 2011-03-04
     */
    public static HttpSession mockHttpSession(String id, HttpSessionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn(id);
        when(session.getAttributeNames()).thenReturn(new IteratorEnumeration<>(emptyIterator()));
        HttpSessionStubbingOperation.stubServletContext().of(session);
        for (HttpSessionStubbingOperation stubbing : stubbings) {
            stubbing.of(session);
        }
        return session;
    }

    /**
     * Create a new {@link Cookie} mock with its {@link Cookie#getName()} and {@link Cookie#getValue()} methods
     * returning the supplied values. No further defaults are applied beyond what Mockito provides.
     * Additional behavior can be applied via provided {@link CookieStubbingOperation} instances.
     *
     * @param name cookie name (may be {@code null} if test scenario requires it)
     * @param value cookie value (may be {@code null} if test scenario requires it)
     * @param stubbings additional stubbing operations (ignored if {@code null} or empty)
     * @return configured cookie mock
     * @see CookieStubbingOperation
     * @since 2011-03-04
     */
    public static Cookie mockCookie(String name, String value, CookieStubbingOperation... stubbings) {
        Cookie result = mock(Cookie.class);
        when(result.getName()).thenReturn(name);
        when(result.getValue()).thenReturn(value);
        for (CookieStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    private static final Answer<ServletContext> SESSION_SERVLET_CONTEXT_ANSWER = invocationOnMock -> {
        PageContext mock = (PageContext) invocationOnMock.getMock();
        HttpSession session = mock != null ? mock.getSession() : null;
        return session != null ? session.getServletContext() : null;
    };

    private static final Answer<HttpSession> REQUEST_SESSION_ANSWER = invocationOnMock -> {
        PageContext mock = (PageContext) invocationOnMock.getMock();
        HttpServletRequest request = mock != null ? (HttpServletRequest) mock.getRequest() : null;
        return request != null ? request.getSession() : null;
    };

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ServletMockUtils() {
    }
}
