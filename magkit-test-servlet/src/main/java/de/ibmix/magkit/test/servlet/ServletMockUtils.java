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

import javax.servlet.FilterChain;
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

import static de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation.stubServletContext;
import static java.util.Collections.enumeration;
import static org.apache.commons.collections4.IteratorUtils.emptyIterator;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Servlet mock utils.
 *
 * @author wolf.bubenik
 * @since 04.03.11
 */
public final class ServletMockUtils {

    private static final Answer<String> REPLY_PARAMETER_ANSWER = invocation -> (String) invocation.getArguments()[0];

    private static final Answer<String> REQUEST_CONTEXT_PATH_ANSWER = invocationOnMock -> {
        HttpServletRequest request = (HttpServletRequest) invocationOnMock.getMock();
        HttpSession session = request != null ? request.getSession() : null;
        ServletContext context = session != null ? session.getServletContext() : null;
        return context != null ? context.getContextPath() : null;
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

    public static HttpServletRequest mockHttpServletRequest(HttpServletRequestStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttributeNames()).thenReturn(new IteratorEnumeration(emptyIterator()));
        when(request.getParameterMap()).thenReturn(new TreeMap<>());
        doAnswer(REQUEST_PARAMETER_NAMES_ANSWER).when(request).getParameterNames();
        doAnswer(REQUEST_PARAMETER_VALUES_ANSWER).when(request).getParameterValues(anyString());
        doAnswer(REQUEST_PARAMETER_ANSWER).when(request).getParameter(anyString());
        HttpServletRequestStubbingOperation.stubHttpSession("test").of(request);
        doAnswer(REQUEST_CONTEXT_PATH_ANSWER).when(request).getContextPath();
        for (HttpServletRequestStubbingOperation stubbing : stubbings) {
            stubbing.of(request);
        }
        return request;
    }

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

    public static FilterChain mockFilterChain() {
        return mock(FilterChain.class);
    }

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
