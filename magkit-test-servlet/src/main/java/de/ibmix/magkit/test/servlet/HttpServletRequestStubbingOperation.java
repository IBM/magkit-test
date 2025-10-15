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

import de.ibmix.magkit.assertations.Require;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.apache.commons.lang3.ArrayUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.IteratorUtils.arrayIterator;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;

/**
 * Abstract base class for stubbing operations on {@link HttpServletRequest} mocks.
 * <p>
 * Subclasses must implement the {@link #of(HttpServletRequest)} method to define how the stubbing is applied to the given request.
 * Factory methods are provided to create common stubbing operations for various request properties.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class HttpServletRequestStubbingOperation {
    /**
     * Applies the stubbing operation to the given {@link HttpServletRequest} mock.
     *
     * @param request the {@link HttpServletRequest} mock to stub
     */
    public abstract void of(HttpServletRequest request);

    /**
     * Creates a stubbing operation that sets the context path of the request.
     * <p>
     * If no session exists, a mock session and servlet context are created. If a session exists but no servlet context, a mock servlet context is created. If a servlet context exists, its context path is stubbed.
     * </p>
     *
     * @param value the context path to be returned by getContextPath()
     * @return a stubbing operation for the context path
     */
    public static HttpServletRequestStubbingOperation stubContextPath(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                HttpSession session = request.getSession();
                if (session == null) {
                    stubHttpSession("test", HttpSessionStubbingOperation.stubServletContext(
                        ServletContextStubbingOperation.stubContextPath(value)
                    )).of(request);
                } else {
                    ServletContext servletContext = session.getServletContext();
                    if (servletContext == null) {
                        HttpSessionStubbingOperation.stubServletContext(
                            ServletContextStubbingOperation.stubContextPath(value)
                        ).of(session);
                    } else {
                        ServletContextStubbingOperation.stubContextPath(value).of(servletContext);
                    }
                }
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the HTTP method of the request.
     *
     * @param value the HTTP method to be returned by getMethod()
     * @return a stubbing operation for the HTTP method
     */
    public static HttpServletRequestStubbingOperation stubMethod(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getMethod();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets a header value for the request.
     *
     * @param name  the name of the header
     * @param value the value to be returned by getHeader(name)
     * @return a stubbing operation for the header
     */
    public static HttpServletRequestStubbingOperation stubHeader(final String name, final String value) {
        Require.Argument.notNull(name, "name must not be null");
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getHeader(name);
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the query string of the request.
     *
     * @param value the query string to be returned by getQueryString()
     * @return a stubbing operation for the query string
     */
    public static HttpServletRequestStubbingOperation stubQueryString(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getQueryString();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the HttpSession for the request.
     *
     * @param value the HttpSession to be returned by getSession()
     * @return a stubbing operation for the session
     */
    public static HttpServletRequestStubbingOperation stubHttpSession(final HttpSession value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getSession();
                doReturn(value).when(request).getSession(anyBoolean());
            }
        };
    }

    /**
     * Creates a stubbing operation that sets a mock HttpSession with the given id and applies additional stubbing operations.
     *
     * @param id                 the session id
     * @param stubbingOperations additional stubbing operations for the session
     * @return a stubbing operation for the session
     */
    public static HttpServletRequestStubbingOperation stubHttpSession(final String id, final HttpSessionStubbingOperation... stubbingOperations) {
        return stubHttpSession(ServletMockUtils.mockHttpSession(id, stubbingOperations));
    }

    /**
     * Creates a stubbing operation that sets a request parameter with the given name and values.
     *
     * @param name   the parameter name
     * @param values the parameter values
     * @return a stubbing operation for the parameter
     */
    public static HttpServletRequestStubbingOperation stubParameter(final String name, final String... values) {
        Require.Argument.notNull(name, "name must not be null");
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                Map<String, String[]> parameterMap = request.getParameterMap();
                if (values != null && values.length > 0) {
                    parameterMap.put(name, values);
                } else {
                    parameterMap.remove(name);
                }
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the request parameter map.
     * If {@code parameters} is {@code null} or empty nothing is changed.
     *
     * @param parameters the parameter map to be set (may be {@code null})
     * @return a stubbing operation for the parameter map
     */
    public static HttpServletRequestStubbingOperation stubParameterMap(final Map<String, String[]> parameters) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                if (parameters == null || parameters.isEmpty()) {
                    return;
                }
                for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                    stubParameter(parameter.getKey(), parameter.getValue()).of(request);
                }
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the server name of the request.
     *
     * @param value the server name to be returned by getServerName()
     * @return a stubbing operation for the server name
     */
    public static HttpServletRequestStubbingOperation stubServerName(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getServerName();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the server port of the request.
     *
     * @param value the server port to be returned by getServerPort()
     * @return a stubbing operation for the server port
     */
    public static HttpServletRequestStubbingOperation stubServerPort(final int value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getServerPort();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the protocol of the request.
     *
     * @param value the protocol to be returned by getProtocol()
     * @return a stubbing operation for the protocol
     */
    public static HttpServletRequestStubbingOperation stubProtocol(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getProtocol();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the request URI.
     *
     * @param value the URI to be returned by getRequestURI()
     * @return a stubbing operation for the request URI
     */
    public static HttpServletRequestStubbingOperation stubRequestUri(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getRequestURI();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the request URL.
     *
     * @param value the URL to be returned by getRequestURL()
     * @return a stubbing operation for the request URL
     */
    public static HttpServletRequestStubbingOperation stubRequestUrl(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(new StringBuffer(value)).when(request).getRequestURL();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the secure flag of the request.
     *
     * @param value true if the request is secure, false otherwise
     * @return a stubbing operation for the secure flag
     */
    public static HttpServletRequestStubbingOperation stubIsSecure(final boolean value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).isSecure();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets an attribute on the request.
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @return a stubbing operation for the attribute
     */
    public static HttpServletRequestStubbingOperation stubAttribute(final String name, final Object value) {
        Require.Argument.notNull(name, "name must not be null");
        return new HttpServletRequestStubbingOperation() {
            @Override
            public void of(HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getAttribute(name);
                IteratorEnumeration<String> nameEnum = (IteratorEnumeration<String>) request.getAttributeNames();
                ((ResettableIterator<String>) nameEnum.getIterator()).reset();
                List<String> names = toList(nameEnum.getIterator());
                if (names.contains(name) && value == null) {
                    names.remove(name);
                } else if (value != null && !names.contains(name)) {
                    names.add(name);
                }
                nameEnum.setIterator(arrayIterator((Object) names.toArray()));
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the local name of the request.
     *
     * @param value the local name to be returned by getLocalName()
     * @return a stubbing operation for the local name
     */
    public static HttpServletRequestStubbingOperation stubLocalName(final String value) {
        return new HttpServletRequestStubbingOperation() {
            @Override
            public void of(final HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getLocalName();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the local port of the request.
     *
     * @param value the local port to be returned by getLocalPort()
     * @return a stubbing operation for the local port
     */
    public static HttpServletRequestStubbingOperation stubLocalPort(final int value) {
        return new HttpServletRequestStubbingOperation() {
            @Override
            public void of(final HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getLocalPort();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets a cookie on the request.
     *
     * @param name      the cookie name
     * @param value     the cookie value
     * @param stubbings additional stubbing operations for the cookie
     * @return a stubbing operation for the cookie
     */
    public static HttpServletRequestStubbingOperation stubCookie(final String name, final String value, final CookieStubbingOperation... stubbings) {
        Cookie cookie = ServletMockUtils.mockCookie(name, value, stubbings);
        return stubCookies(cookie);
    }

    /**
     * Creates a stubbing operation that sets cookies on the request.
     *
     * @param cookies the cookies to be set
     * @return a stubbing operation for the cookies
     */
    public static HttpServletRequestStubbingOperation stubCookies(final Cookie... cookies) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(final HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                Cookie[] oldCookies = request.getCookies();
                Cookie[] newCookies = cookies;
                if (oldCookies != null && cookies != null) {
                    newCookies = ArrayUtils.addAll(oldCookies, cookies);
                }
                doReturn(newCookies).when(request).getCookies();
            }
        };
    }

    /**
     * Creates a stubbing operation that sets the character encoding of the request.
     *
     * @param value the character encoding to be returned by getLocalPort()
     * @return a stubbing operation for the character encoding
     */
    public static HttpServletRequestStubbingOperation stubCharacterEncoding(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(final HttpServletRequest request) {
                Require.Argument.notNull(request, "request must not be null");
                doReturn(value).when(request).getCharacterEncoding();
            }
        };
    }
}
