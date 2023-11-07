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
 * Servlet mock utils clas that provides methods for creation of mocks of
 * HttpServletRequest, HttpServletResponse, ServletContext, PageContext, HttpSession and Cookie.
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
     * Method for creating a Mockito mock of a HttpServletRequest with defaults:
     * An empty attribute name iterator, an empty parameter map and a default session with ID "test".
     *
     * @param stubbings an array of HttpServletRequestStubbingOperation
     * @return a HttpServletRequest mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
     */
    public static HttpServletRequest mockHttpServletRequest(HttpServletRequestStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttributeNames()).thenReturn(new IteratorEnumeration(emptyIterator()));
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
     * Method for creating a Mockito mock of a HttpServletResponse with defaults:
     * A PrintWriter mock, ServletOutputStream mock and default (do nothing) behaviour for URL encoding methods.
     *
     * @param stubbings an array of HttpServletResponseStubbingOperation
     * @return a HttpServletResponse mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
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
     * Method for creating a Mockito mock of a ServletContext with defaults:
     * A ServletContext mock and empty iterators for attribute names and init parameter names.
     *
     * @param stubbings an array of ServletContextStubbingOperation
     * @return a ServletContext mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
     */
    public static ServletContext mockServletContext(ServletContextStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        ServletContext context = mock(ServletContext.class);
        when(context.getAttributeNames()).thenReturn(new IteratorEnumeration(emptyIterator()));
        when(context.getInitParameterNames()).thenReturn(new IteratorEnumeration(emptyIterator()));
        for (ServletContextStubbingOperation stubbing : stubbings) {
            stubbing.of(context);
        }
        return context;
    }

    /**
     * Method for creating a Mockito mock of a PageContext with defaults:
     * A HttpServletRequest mock, HttpServletResponse mock and returning Session and ServletContext from request session.
     *
     * @param stubbings an array of PageContextStubbingOperation
     * @return a PageContext mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
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
     * Method for creating a Mockito mock of a HttpSession with defaults:
     * an empty iterator for attribute names and a ServletContext mock.
     *
     * @param id  the session ID as String
     * @param stubbings an array of HttpSessionStubbingOperation
     * @return a HttpSession mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
     */
    public static HttpSession mockHttpSession(String id, HttpSessionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpSession session = mock(HttpSession.class);
        when(session.getId()).thenReturn(id);
        when(session.getAttributeNames()).thenReturn(new IteratorEnumeration(emptyIterator()));
        HttpSessionStubbingOperation.stubServletContext().of(session);
        for (HttpSessionStubbingOperation stubbing : stubbings) {
            stubbing.of(session);
        }
        return session;
    }

    /**
     * Method for creating a Mockito mock of a Cookie with no defaults.
     *
     * @param name  the Cookie name as String
     * @param value  the Cookie value as String
     * @param stubbings an array of CookieStubbingOperation
     * @return a Cookie mock with stubbed behaviour
     * @throws AssertionError when stubbings are null
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

    private ServletMockUtils() {
    }
}
