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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;

/**
 * Utility class that provides factory methods for HttpServletRequestStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockHttpServletRequest(...)
 * or for stubbing the behaviour of an existing mock: HttpServletRequestStubbingOperation.stubContextPath("path").of(mock).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class HttpServletRequestStubbingOperation {

    public abstract void of(HttpServletRequest request);

    public static HttpServletRequestStubbingOperation stubContextPath(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                HttpSession session = request.getSession();
                if (session == null) {
                    stubHttpSession("test", HttpSessionStubbingOperation.stubServletContext(ServletContextStubbingOperation.stubContextPath(value))).of(request);
                } else {
                    ServletContext servletContext = session.getServletContext();
                    if (servletContext == null) {
                        HttpSessionStubbingOperation.stubServletContext(ServletContextStubbingOperation.stubContextPath(value)).of(session);
                    } else {
                        ServletContextStubbingOperation.stubContextPath(value).of(servletContext);
                    }
                }
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubMethod(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getMethod();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubHeader(final String name, final String value) {
        assertThat(name, notNullValue());
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getHeader(name);
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubQueryString(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getQueryString();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubHttpSession(final HttpSession value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getSession();
                doReturn(value).when(request).getSession(anyBoolean());
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubHttpSession(final String id, final HttpSessionStubbingOperation... stubbingOperations) {
        return stubHttpSession(ServletMockUtils.mockHttpSession(id, stubbingOperations));
    }

    public static HttpServletRequestStubbingOperation stubParameter(final String name, final String... values) {
        assertThat(name, notNullValue());
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                assertThat(name, notNullValue());
                Map<String, String[]> parameterMap = request.getParameterMap();
                if (values != null && values.length > 0) {
                    parameterMap.put(name, values);
                } else {
                    parameterMap.remove(name);
                }
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubParameterMap(final Map<String, String[]> parameters) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                for (Map.Entry<String, String[]> parameter : parameters.entrySet()) {
                    stubParameter(parameter.getKey(), parameter.getValue()).of(request);
                }
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubServerName(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getServerName();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubServerPort(final int value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getServerPort();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubProtocol(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getProtocol();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubRequestUri(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getRequestURI();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubRequestUrl(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(new StringBuffer(value)).when(request).getRequestURL();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubIsSecure(final boolean value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).isSecure();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubAttribute(final String name, final Object value) {
        return new HttpServletRequestStubbingOperation() {
            @Override
            public void of(HttpServletRequest request) {
                assertThat(request, notNullValue());
                assertThat(name, notNullValue());
                doReturn(value).when(request).getAttribute(name);
                IteratorEnumeration<String> nameEnum = (IteratorEnumeration<String>) request.getAttributeNames();
                ((ResettableIterator<String>) nameEnum.getIterator()).reset();
                List<String> names = toList(nameEnum.getIterator());
                if (names.contains(name) && value == null) {
                    names.remove(name);
                } else if (value != null) {
                    names.add(name);
                }
                nameEnum.setIterator(arrayIterator((Object) names.toArray()));
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubLocalName(final String value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(final HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getLocalName();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubLocalPort(final int value) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(final HttpServletRequest request) {
                assertThat(request, notNullValue());
                doReturn(value).when(request).getLocalPort();
            }
        };
    }

    public static HttpServletRequestStubbingOperation stubCookie(final String name, final String value, final CookieStubbingOperation... stubbings) {
        Cookie cookie = ServletMockUtils.mockCookie(name, value, stubbings);
        return stubCookies(cookie);
    }

    public static HttpServletRequestStubbingOperation stubCookies(final Cookie... cookies) {
        return new HttpServletRequestStubbingOperation() {

            @Override
            public void of(final HttpServletRequest request) {
                assertThat(request, notNullValue());
                Cookie[] oldCookies = request.getCookies();
                Cookie[] newCookies = cookies;
                if (oldCookies != null && cookies != null) {
                    newCookies = ArrayUtils.addAll(oldCookies, cookies);
                }
                doReturn(newCookies).when(request).getCookies();
            }
        };
    }
}
