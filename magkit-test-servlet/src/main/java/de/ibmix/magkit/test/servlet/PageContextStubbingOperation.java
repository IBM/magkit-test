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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;

import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletResponse;
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockServletContext;
import static org.mockito.Mockito.when;

/**
 * Factory and helper methods to create {@link PageContextStubbingOperation} instances for conveniently
 * configuring Mockito based {@link PageContext} mocks in tests.
 * <p>
 * Each static method returns a lazily executed operation. The returned operation's {@link #of(PageContext)} method
 * applies (and if necessary creates) nested servlet API mocks (request, response, session, servlet context) using
 * the supplied stubbing operations. If a nested object already exists on the given {@link PageContext}, only the
 * provided additional stubbings are applied – no replacement occurs. This makes the operations composable and DRY.
 * </p>
 * <p>
 * Typical usage examples:
 * <pre>
 * PageContext pageContext = mockPageContext(
 *     PageContextStubbingOperation.stubHttpServletRequest(
 *         HttpServletRequestStubbingOperation.stubRequestUri("/test")
 *     ),
 *     PageContextStubbingOperation.stubHttpSession("mySessionId")
 * );
 *
 * // Or augment an existing mock:
 * PageContextStubbingOperation.stubHttpServletResponse(
 *     HttpServletResponseStubbingOperation.stubStatus(200)
 * ).of(existingPageContextMock);
 * </pre>
 * </p>
 * <p>
 * Thread-safety: The operations are stateless lambdas (anonymous classes) and therefore thread-safe; however the
 * underlying mocks they act upon are usually <em>not</em> thread-safe and should not be shared across threads.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-10
 */
public abstract class PageContextStubbingOperation {
    /**
     * Apply this stubbing operation to the supplied {@link PageContext} mock.
     *
     * @param context the (Mockito) {@link PageContext} mock to augment (must not be {@code null})
     */
    public abstract void of(PageContext context);

    /**
     * Create a stubbing operation that sets the {@link HttpServletRequest} of the {@link PageContext} to the supplied instance.
     * Existing request, if any, is replaced by the given one.
     *
     * @param request preconfigured {@link HttpServletRequest} mock (may be real or mock, must not be {@code null})
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpServletRequest(final HttpServletRequest request) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
                when(context.getRequest()).thenReturn(request);
            }
        };
    }

    /**
     * Create a stubbing operation that ensures a {@link HttpServletRequest} is present on the {@link PageContext} and
     * applies the provided request stubbing operations. If no request is present, a new mock is created via
     * {@link ServletMockUtils#mockHttpServletRequest(HttpServletRequestStubbingOperation...)} and attached.
     *
     * @param stubbings optional request stubbing operations (may be empty)
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpServletRequest(final HttpServletRequestStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
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

    /**
     * Create a stubbing operation that sets the {@link HttpServletResponse} of the {@link PageContext} to the supplied instance.
     * Existing response, if any, is replaced.
     *
     * @param response preconfigured {@link HttpServletResponse} mock (must not be {@code null})
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpServletResponse(final HttpServletResponse response) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
                when(context.getResponse()).thenReturn(response);
            }
        };
    }

    /**
     * Create a stubbing operation that ensures a {@link HttpServletResponse} is present on the {@link PageContext} and
     * applies the given response stubbing operations. If absent a new mock is created.
     *
     * @param stubbings optional response stubbing operations
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpServletResponse(final HttpServletResponseStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
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

    /**
     * Create a stubbing operation that ensures a {@link ServletContext} (via a session) is present on the {@link PageContext}
     * and binds the provided context. Uses a synthetic session id "test" if a session needs to be created.
     *
     * @param servletContext the servlet context mock/instance to bind
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubServletContext(final ServletContext servletContext) {
        return stubHttpSession("test", HttpSessionStubbingOperation.stubServletContext(servletContext));
    }

    /**
     * Create a stubbing operation that ensures a {@link ServletContext} is present and applies the supplied servlet context stubbings.
     * A new context mock is created if missing.
     *
     * @param stubbings optional servlet context stubbing operations
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubServletContext(final ServletContextStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
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

    /**
     * Create a stubbing operation that attaches the supplied {@link HttpSession} to the {@link PageContext}'s request,
     * creating or replacing the request as necessary.
     *
     * @param session preconfigured {@link HttpSession} mock/instance
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpSession(final HttpSession session) {
        return stubHttpServletRequest(HttpServletRequestStubbingOperation.stubHttpSession(session));
    }

    /**
     * Create a stubbing operation that ensures a {@link HttpSession} with the given id exists and applies the provided
     * session stubbings. If no session exists, a new one is created via {@link HttpServletRequestStubbingOperation#stubHttpSession(String, HttpSessionStubbingOperation...)}.
     *
     * @param id        desired session id (must not be {@code null})
     * @param stubbings optional session stubbing operations
     * @return operation to apply on a {@link PageContext}
     */
    public static PageContextStubbingOperation stubHttpSession(final String id, final HttpSessionStubbingOperation... stubbings) {
        return new PageContextStubbingOperation() {
            @Override
            public void of(PageContext context) {
                Require.Argument.notNull(context, "context must not be null");
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
