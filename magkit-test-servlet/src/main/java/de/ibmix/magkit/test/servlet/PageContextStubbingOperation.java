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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockServletContext;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for PageContextStubbingOperation.
 * Stubbing operations to be used as parameters in ServletMockUtils.mockPageContext(...)
 * or for stubbing the behaviour of an existing mock: PageContextStubbingOperation.stubHttpServletRequest(request).of(mock).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class PageContextStubbingOperation {
    public abstract void of(PageContext context);

    public static PageContextStubbingOperation stubHttpServletRequest(final HttpServletRequest request) {
        return new PageContextStubbingOperation() {

            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                when(context.getRequest()).thenReturn(request);
            }
        };
    }

    public static PageContextStubbingOperation stubHttpServletRequest(final HttpServletRequestStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {

            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                HttpServletRequest request = (HttpServletRequest) context.getRequest();
                if (request == null) {
                    request = mockHttpServletRequest(stubbings);
                    when(context.getRequest()).thenReturn(request);
                } else {
                    for (HttpServletRequestStubbingOperation stubbing : stubbings) {
                        stubbing.of(request);
                    }
                }
            }
        };
    }

    public static PageContextStubbingOperation stubHttpServletResponse(final HttpServletResponse response) {
        return new PageContextStubbingOperation() {

            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                when(context.getResponse()).thenReturn(response);
            }
        };
    }

    public static PageContextStubbingOperation stubHttpServletResponse(final HttpServletResponseStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {

            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                HttpServletResponse response = (HttpServletResponse) context.getResponse();
                if (response == null) {
                    stubHttpServletResponse(mockHttpServletResponse(stubbings)).of(context);
                } else {
                    for (HttpServletResponseStubbingOperation stubbing : stubbings) {
                        stubbing.of(response);
                    }
                }

            }
        };
    }

    public static PageContextStubbingOperation stubServletContext(final ServletContext servletContext) {
        return stubHttpSession("test", HttpSessionStubbingOperation.stubServletContext(servletContext));
    }

    public static PageContextStubbingOperation stubServletContext(final ServletContextStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {

            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                ServletContext servletContext = context.getServletContext();
                if (servletContext == null) {
                    servletContext = mockServletContext(stubbings);
                    stubServletContext(servletContext).of(context);
                } else {
                    for (ServletContextStubbingOperation stubbing : stubbings) {
                        stubbing.of(servletContext);
                    }
                }
            }
        };
    }

    public static PageContextStubbingOperation stubHttpSession(final HttpSession session) {
        return stubHttpServletRequest(HttpServletRequestStubbingOperation.stubHttpSession(session));
    }

    public static PageContextStubbingOperation stubHttpSession(final String id, final HttpSessionStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                assertThat(context, notNullValue());
                HttpSession session = context.getSession();
                if (session == null) {
                    stubHttpServletRequest(HttpServletRequestStubbingOperation.stubHttpSession(id, stubbings)).of(context);
                } else {
                    for (HttpSessionStubbingOperation stubbing : stubbings) {
                        stubbing.of(session);
                    }
                }
            }
        };
    }
}
