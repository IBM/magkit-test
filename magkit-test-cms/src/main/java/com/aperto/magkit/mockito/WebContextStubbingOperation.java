package com.aperto.magkit.mockito;

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

import com.aperto.magkit.mockito.jcr.SessionStubbingOperation;
import com.aperto.magkit.mockito.servlet.HttpServletRequestStubbingOperation;
import com.aperto.magkit.mockito.servlet.HttpServletResponseStubbingOperation;
import com.aperto.magkit.mockito.servlet.HttpSessionStubbingOperation;
import com.aperto.magkit.mockito.servlet.ServletMockUtils;
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

import static com.aperto.magkit.mockito.jcr.SessionMockUtils.mockSession;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for WebContextStubbingOperation.
 * Stubbing operations to be used as parameters in ContextMockUtils.mockWebContext(...).
 *
 * @author wolf.bubenik
 * @since 02.03.2011
 */
public abstract class WebContextStubbingOperation {
    public abstract void of(WebContext context) throws RepositoryException;

    /**
     * Creates a WebContextStubbingOperation that stubs getLocale() to return the provided value.
     *
     * @param locale the java.util Locale to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubLocale(final Locale locale) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                assertThat(context, notNullValue());
                when(context.getLocale()).thenReturn(locale);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getAggregationState() to return the provided value.
     *
     * @param aggState a info.magnolia.cms.core.AggregationState instance or null
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubAggregationState(final AggregationState aggState) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                assertThat(context, notNullValue());
                when(context.getAggregationState()).thenReturn(aggState);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getAccessManager(repositoryId) and getAccessManager(repositoryId, workspaceId) to return the provided value.
     *
     * @param repositoryId the repository ID/name as java.lang.String
     * @param am           the info.magnolia.cms.security.AccessManager to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubAccessManager(final String repositoryId, final AccessManager am) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                assertThat(context, notNullValue());
                String repository = isBlank(repositoryId) ? WEBSITE : repositoryId;
                when(context.getAccessManager(repository)).thenReturn(am);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getRequest() to return the provided value.
     * if the provided request is not null,
     * - getContextPath() will be stubbed to return request.getContextPath() (@see WebContextStubbingOperation.stubContextPath(String contextPath))
     * - getParameters() will be stubbed using request.getParameterMap() (@see WebContextStubbingOperation.stubParameters(Map<String, String[]> parameters))
     * will be stubbed as well.
     *
     * @param request the javax.servlet.http.HttpServletRequest to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubRequest(final HttpServletRequest request) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                assertThat(context, notNullValue());
                when(context.getRequest()).thenReturn(request);
            }
        };
    }

    public static WebContextStubbingOperation stubExistingRequest(final HttpServletRequestStubbingOperation... stubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
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
     * Creates a WebContextStubbingOperation that stubs getParameters() to return a Map<String,String> containing the first value for each key.
     * If the provided parameters map is not null or empty,
     * - getParameterValues(String name)
     * - getParameter(String name)
     * will be stubbed as well.
     *
     * @param parameterMap the parameters name - values Map
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubParameters(final Map<String, String[]> parameterMap) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                WebContextStubbingOperation.stubExistingRequest(HttpServletRequestStubbingOperation.stubParameterMap(parameterMap)).of(context);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getParameterValues(String name) to return the provided values.
     * If the values[] is not null or empty, getParameter(String name) will be stubbed to return the first value.
     * The stubbing of getParameters() will be updated as well.
     *
     * @param name   the parameter name as java.lang.String
     * @param values the parameter values as jav.lang.String[]
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubParameter(final String name, final String... values) {
        return new WebContextStubbingOperation() {
            @Override
            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                assertThat(name, notNullValue());
                stubExistingRequest(HttpServletRequestStubbingOperation.stubParameter(name, values)).of(context);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getAttribute(String name) to return the provided value.
     * Uses stubParameterValues(name, new String[] {value}}) to archive a consistent stubbing of
     * - getParameter(String name)
     * - getParameterValues(String name)
     * - getParameters()
     *
     * @param name  the parameter name as java.lang.String
     * @param value the parameter value as java.lang.String
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubAttribute(final String name, final Object value) {
        return stubAttribute(name, value, Context.LOCAL_SCOPE);
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getAttribute(String name, int scope) and getAttribute(String name) to return the provided value.
     *
     * @param name  the parameter name as java.lang.String
     * @param value the parameter value as java.lang.String
     * @param scope the attribute scope is int
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubAttribute(final String name, final Object value, final int scope) {
        return new WebContextStubbingOperation() {
            @Override
            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                assertThat(name, notNullValue());
                if (context.getRequest() == null) {
                    stubExistingRequest().of(context);
                }
                // mimic RequestAttributeStrategy:
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
                        // NOT jet supported
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getResponse() to return the provided value.
     *
     * @param response the javax.servlet.http.HttpServletResponse to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubResponse(final HttpServletResponse response) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) {
                assertThat(context, notNullValue());
                when(context.getResponse()).thenReturn(response);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getResponse() to return the provided value.
     *
     * @param stubbings the HttpServletResponseStubbingOperations for a new HttpServletResponse mock
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubExistingResponse(final HttpServletResponseStubbingOperation... stubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
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
     * Creates a WebContextStubbingOperation that stubs getServletContext() to return the provided value.
     * <p/>
     * If the provided ServletContext is not null getContextPath() will be stubbed to return servletContext.getContextPath().
     *
     * @param servletContext the ServletContext to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubServletContext(final ServletContext servletContext) {
        return stubExistingRequest(HttpServletRequestStubbingOperation.stubHttpSession("id",
            HttpSessionStubbingOperation.stubServletContext(servletContext)
            )
        );
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getContextPath() to return the provided value.
     *
     * @param path the webb applications context path as java.lang.String
     * @return a new WebContextStubbingOperation instance.
     */
    public static WebContextStubbingOperation stubContextPath(final String path) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                HttpServletRequest request = context.getRequest();
                if (request == null) {
                    stubRequest(ServletMockUtils.mockHttpServletRequest(HttpServletRequestStubbingOperation.stubContextPath(path))).of(context);
                } else {
                    HttpServletRequestStubbingOperation.stubContextPath(path).of(request);
                }
            }
        };
    }

    public static WebContextStubbingOperation stubUser(final User user) {
        return new WebContextStubbingOperation() {

            @Override
            public void of(final WebContext context) {
                assertThat(context, notNullValue());
                when(context.getUser()).thenReturn(user);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getJcrSession(String name) to return the provided value.
     *
     * @param session the javax.jcr.Session to be returned
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubJcrSession(final String workspace, final Session session) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                when(context.getJCRSession(workspace)).thenReturn(session);
            }
        };
    }

    /**
     * Creates a WebContextStubbingOperation that stubs getJcrSession(String name) to return the provided value.
     *
     * @param workspace the Workspace of the session to be mocked
     * @return a new WebContextStubbingOperation instance
     */
    public static WebContextStubbingOperation stubJcrSession(final String workspace, final SessionStubbingOperation... sessionStubbings) {
        return new WebContextStubbingOperation() {

            public void of(WebContext context) throws RepositoryException {
                assertThat(context, notNullValue());
                Session session = context.getJCRSession(workspace);
                if (session == null) {
                    session = mockSession(workspace, sessionStubbings);
                    when(context.getJCRSession(workspace)).thenReturn(session);
                } else {
                    for (SessionStubbingOperation stubbing : sessionStubbings) {
                        stubbing.of(session);
                    }
                }
            }
        };
    }
}
