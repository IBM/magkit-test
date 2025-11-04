package de.ibmix.magkit.test.cms.context;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.StubbingOperation;
import de.ibmix.magkit.test.jcr.SessionStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletResponseStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation;
import de.ibmix.magkit.test.servlet.ServletMockUtils;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.context.Context;
import info.magnolia.context.WebContext;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;

import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mockito.Mockito.when;

/**
 * Factory holder for reusable {@code WebContextStubbingOperation} instances to configure a mocked {@link WebContext} for unit tests.
 * <p>
 * Each static factory creates a lightweight operation whose {@link de.ibmix.magkit.test.StubbingOperation#of(Object) of(WebContext)} method applies targeted Mockito stubbings
 * without constructing a new context. Designed for composability via {@link ContextMockUtils#mockWebContext(WebContextStubbingOperation...)}.
 * <p>
 * Key characteristics:
 * <ul>
 *   <li><strong>Idempotent:</strong> Re-applying the same operation overwrites previous stubs with identical values (no accumulation).</li>
 *   <li><strong>Null tolerant:</strong> Providing {@code null} values results in getters returning {@code null}; no exception is thrown.</li>
 *   <li><strong>Blank repository IDs:</strong> Mapped to {@link info.magnolia.repository.RepositoryConstants#WEBSITE} where applicable.</li>
 *   <li><strong>Session/request creation:</strong> Operations that depend on a request or session will lazily create mocks if absent (e.g. {@link #stubAttribute(String, Object, int)}).</li>
 *   <li><strong>Scope handling:</strong> Attribute stubbing mimics Magnolia's request attribute strategy for LOCAL, SESSION, APPLICATION scopes (application scope currently not implemented).</li>
 * </ul>
 * Side effects and coupling:
 * <ul>
 *   <li>Some operations (e.g. {@link #stubParameters(Map)}) delegate to {@link #stubExistingRequest(HttpServletRequestStubbingOperation...)} potentially creating a mock request.</li>
 *   <li>Session related operations may create a new {@link javax.servlet.http.HttpSession} if missing.</li>
 * </ul>
 * Thread-safety: Not thread-safe; intended for single-threaded test execution manipulating static Magnolia context state.
 * <p>
 * Typical usage:
 * <pre>{@code
 * WebContext context = ContextMockUtils.mockWebContext(
 *     WebContextStubbingOperation.stubLocale(Locale.ENGLISH),
 *     WebContextStubbingOperation.stubContextPath("/myapp"),
 *     WebContextStubbingOperation.stubParameter("view", "detail"),
 *     WebContextStubbingOperation.stubJcrSession("website")
 * );
 * }</pre>
 * Combine several operations in one call for concise, expressive test setup.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-03-02
 */
public abstract class WebContextStubbingOperation implements StubbingOperation<WebContext> {

    /**
     * Stubs {@link WebContext#getLocale()}.
     *
     * @param locale locale returned by {@link WebContext#getLocale()} (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubLocale(final Locale locale) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Stubs {@link WebContext#getAggregationState()}.
     *
     * @param aggState aggregation state value (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubAggregationState(final AggregationState aggState) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getAggregationState()).thenReturn(aggState);
            }
        };
    }

    /**
     * Stubs {@link WebContext#getAccessManager(String)} for a repository id. Blank/empty {@code repositoryId} mapped to {@code WEBSITE}.
     *
     * @param repositoryId repository id/name (blank treated as {@link info.magnolia.repository.RepositoryConstants#WEBSITE})
     * @param am access manager returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubAccessManager(final String repositoryId, final AccessManager am) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                when(context.getAccessManager(repository)).thenReturn(am);
            }
        };
    }

    /**
     * Stubs {@link WebContext#getRequest()}.
     * Does not automatically stub parameters or context path – use {@link #stubParameters(Map)} or {@link #stubContextPath(String)} separately for clarity.
     *
     * @param request request returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubRequest(final HttpServletRequest request) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getRequest()).thenReturn(request);
            }
        };
    }

    /**
     * Ensures a request exists and applies provided {@link HttpServletRequestStubbingOperation}s.
     * If no request is present a new mock is created via {@link ServletMockUtils#mockHttpServletRequest(HttpServletRequestStubbingOperation...)}.
     *
     * @param stubbings optional request stubbing operations
     * @return operation applying the stubbings
     */
    public static WebContextStubbingOperation stubExistingRequest(final HttpServletRequestStubbingOperation... stubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                HttpServletRequest request = context.getRequest();
                if (request == null) {
                    stubRequest(ServletMockUtils.mockHttpServletRequest(stubbings)).of(context);
                } else {
                    for (HttpServletRequestStubbingOperation stubbing : stubbings) {
                        stubbing.of(request);
                    }
                }
            }
        };
    }

    /**
     * Stubs request parameters on the current or newly created request using a provided parameter map.
     * Delegates to {@link #stubExistingRequest(HttpServletRequestStubbingOperation...)}.
     *
     * @param parameterMap map of parameter name to array of values (may be {@code null} or empty)
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubParameters(final Map<String, String[]> parameterMap) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                WebContextStubbingOperation.stubExistingRequest(HttpServletRequestStubbingOperation.stubParameterMap(parameterMap)).of(context);
            }
        };
    }

    /**
     * Stubs a single request parameter. Creates a request if absent. Parameter values are applied via {@link HttpServletRequestStubbingOperation#stubParameter(String, String...)}.
     *
     * @param name parameter name (must not be {@code null})
     * @param values one or more values (may be empty or {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubParameter(final String name, final String... values) {
        Require.Argument.notNull(name, "name should not be null");
        return new WebContextStubbingOperation() {
            @Override
            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                stubExistingRequest(HttpServletRequestStubbingOperation.stubParameter(name, values)).of(context);
            }
        };
    }

    /**
     * Convenience for LOCAL scope attribute stubbing. See {@link #stubAttribute(String, Object, int)}.
     *
     * @param name attribute name (must not be {@code null})
     * @param value attribute value (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubAttribute(final String name, final Object value) {
        return stubAttribute(name, value, Context.LOCAL_SCOPE);
    }

    /**
     * Stubs attribute retrieval according to Magnolia scope semantics for LOCAL and SESSION.
     * APPLICATION scope is currently not implemented (no-op).
     * Creates request/session mock if missing for the selected scope.
     *
     * @param name attribute name (must not be {@code null})
     * @param value attribute value (may be {@code null})
     * @param scope one of {@link Context#LOCAL_SCOPE}, {@link Context#SESSION_SCOPE}, {@link Context#APPLICATION_SCOPE}
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubAttribute(final String name, final Object value, final int scope) {
        Require.Argument.notNull(name, "name should not be null");
        return new WebContextStubbingOperation() {
            @Override
            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                if (context.getRequest() == null) {
                    stubExistingRequest().of(context);
                }
                switch (scope) {
                    case Context.LOCAL_SCOPE:
                        HttpServletRequestStubbingOperation.stubAttribute(name, value).of(context.getRequest());
                        break;
                    case Context.SESSION_SCOPE:
                        HttpSession httpsession = context.getRequest().getSession(false);
                        if (httpsession == null) {
                            HttpServletRequestStubbingOperation.stubHttpSession("test").of(context.getRequest());
                            httpsession = context.getRequest().getSession(false);
                        }
                        HttpSessionStubbingOperation.stubAttribute(name, value).of(httpsession);
                        break;
                    case Context.APPLICATION_SCOPE:
                        // Not yet supported.
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Stubs {@link WebContext#getResponse()}.
     *
     * @param response response returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubResponse(final HttpServletResponse response) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getResponse()).thenReturn(response);
            }
        };
    }

    /**
     * Ensures a response exists and applies provided {@link HttpServletResponseStubbingOperation}s.
     * Creates a new response mock if absent via {@link ServletMockUtils#mockHttpServletResponse(HttpServletResponseStubbingOperation...)}.
     *
     * @param stubbings optional response stubbing operations
     * @return operation applying the stubbings
     */
    public static WebContextStubbingOperation stubExistingResponse(final HttpServletResponseStubbingOperation... stubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                HttpServletResponse response = context.getResponse();
                if (response == null) {
                    stubResponse(ServletMockUtils.mockHttpServletResponse(stubbings)).of(context);
                } else {
                    for (HttpServletResponseStubbingOperation stubbing : stubbings) {
                        stubbing.of(response);
                    }
                }
            }
        };
    }

    /**
     * Stubs {@link WebContext#getServletContext()} indirectly by ensuring a request/session exists and associating a servlet context.
     *
     * @param servletContext servlet context returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubServletContext(final ServletContext servletContext) {
        return stubExistingRequest(HttpServletRequestStubbingOperation.stubHttpSession("test",
            HttpSessionStubbingOperation.stubServletContext(servletContext)
            )
        );
    }

    /**
     * Stubs {@link WebContext#getContextPath()}.
     * Creates a request if none exists and applies the context path.
     *
     * @param path context path value (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubContextPath(final String path) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                HttpServletRequest request = context.getRequest();
                if (request == null) {
                    stubRequest(ServletMockUtils.mockHttpServletRequest(HttpServletRequestStubbingOperation.stubContextPath(path))).of(context);
                } else {
                    HttpServletRequestStubbingOperation.stubContextPath(path).of(request);
                }
            }
        };
    }

    /**
     * Stubs {@link WebContext#getUser()}.
     *
     * @param user user returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubUser(final User user) {
        return new WebContextStubbingOperation() {
            @Override
            public void of(final WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                when(context.getUser()).thenReturn(user);
            }
        };
    }

    /**
     * Stubs {@link WebContext#getJCRSession(String)} to return a provided {@link Session}.
     *
     * @param workspace workspace name (must not be blank)
     * @param session session returned (may be {@code null})
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubJcrSession(final String workspace, final Session session) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                try {
                    when(context.getJCRSession(workspace)).thenReturn(session);
                } catch (RepositoryException e) {
                    // ignore, never thrown.
                }
            }
        };
    }

    /**
     * Ensures a JCR {@link Session} exists for a workspace and applies additional {@link SessionStubbingOperation}s.
     * Creates a session via {@link de.ibmix.magkit.test.jcr.SessionMockUtils#mockSession(String, SessionStubbingOperation...)} if absent; otherwise augments existing session.
     *
     * @param workspace workspace name (must not be blank)
     * @param sessionStubbings optional session stubbing operations
     * @return operation applying the stubbing
     */
    public static WebContextStubbingOperation stubJcrSession(final String workspace, final SessionStubbingOperation... sessionStubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                Require.Argument.notNull(context, "context should not be null");
                try {
                    Session session = context.getJCRSession(workspace);
                    if (session == null) {
                        session = mockSession(workspace, sessionStubbings);
                        when(context.getJCRSession(workspace)).thenReturn(session);
                    } else {
                        for (SessionStubbingOperation stubbing : sessionStubbings) {
                            stubbing.of(session);
                        }
                    }
                } catch (RepositoryException e) {
                    // ignore, never thrown.
                }
            }
        };
    }
}
