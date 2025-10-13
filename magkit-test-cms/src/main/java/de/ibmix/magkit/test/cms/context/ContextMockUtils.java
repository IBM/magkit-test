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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryManagerStubbingOperation;
import de.ibmix.magkit.test.jcr.query.QueryMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryStubbingOperation;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.util.ServletUtil;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.site.ExtendedAggregationState;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAggregationState;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Utility/factory collection for creating Mockito based Magnolia {@link WebContext}, {@link SystemContext}, {@link AggregationState} and JCR query related mocks.
 * <p>All factory methods in this class centralize repetitive boilerplate needed to integrate Magnolia's static {@link MgnlContext} and component provider with Mockito.</p>
 * <p>They make sure that:</p>
 * <ul>
 *     <li>A suitable Magnolia context instance is registered in {@link MgnlContext#setInstance(Context)}.</li>
 *     <li>Default stubbings (request/response, i18n support, parameter/attribute resolution) are applied so tests can focus on their specific behaviour.</li>
 *     <li>Optional, additional {@code *StubbingOperation} instances can further refine the returned mock.</li>
 *     <li>Global state is cleaned with {@link #cleanContext()} to avoid test interference.</li>
 * </ul>
 * <p><strong>Side effects:</strong> Each mock factory potentially mutates the thread-local Magnolia runtime state by replacing the current context instance.
 * Call {@link #cleanContext()} after each test (e.g. in an {@code @AfterEach}) to restore a clean environment.</p>
 * <p><strong>Thread-safety:</strong> Thread-safe because static MgnlContext is backed by ThreadLocal&lt;Context&gt;. Intended for multithreaded unit tests.</p>
 * <p><strong>Typical usage:</strong></p>
 * <pre>{@code
 * @Test
 * void testQueryExecution() throws RepositoryException {
 *     QueryResult result = ContextMockUtils.mockQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base]");
 *     // perform assertions using the mocked result
 * }
 * }</pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-08-27
 */
@SuppressWarnings("deprecation")
public final class ContextMockUtils extends ComponentsMockUtils {

    /**
     * Creates (or reuses) a {@link WebContext} mock, registers it in {@link MgnlContext} and stubs {@link WebContext#getLocale()} to return the provided {@link Locale}.
     * If a web context is already present it will be reused; new default stubbings are only applied when creating a fresh mock.
     *
     * @param locale the locale to be returned by {@link WebContext#getLocale()} (must not be {@code null})
     * @return the managed {@link WebContext} Mockito mock
     * @throws RepositoryException if underlying JCR session stubbing triggers a repository error
     */
    public static WebContext mockWebContext(Locale locale) throws RepositoryException {
        return mockWebContext(WebContextStubbingOperation.stubLocale(locale));
    }

    /**
     * Creates (or reuses) a {@link WebContext} mock and registers it at {@link MgnlContext}. Applies the provided stubbing operations after the default initialization.
     * When a {@link WebContext} already exists it is reused and only the given stubbings are applied (no re-initialization of defaults).
     * Default initialization includes: request/response mocks, parameter and attribute access answers and i18n content support.
     *
     * @param stubbings one or more {@link WebContextStubbingOperation} to apply; must not be {@code null}
     * @return the managed {@link WebContext} Mockito mock
     * @throws RepositoryException if JCR access stubbing fails
     */
    public static WebContext mockWebContext(WebContextStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        WebContext context;
        if (MgnlContext.hasInstance() && MgnlContext.isWebContext()) {
            context = MgnlContext.getWebContext();
        } else {
            context = mockComponentInstance(WebContext.class);
            MgnlContext.setInstance(context);
            mockI18nContentSupport();
            WebContextStubbingOperation.stubExistingRequest().of(context);
            WebContextStubbingOperation.stubExistingResponse().of(context);
            doAnswer(REQUEST_PARAMETER_ANSWER).when(context).getParameter(anyString());
            doAnswer(REQUEST_PARAMETER_VALUES_ANSWER).when(context).getParameterValues(anyString());
            doAnswer(REQUEST_PARAMETERS_ANSWER).when(context).getParameters();
            doAnswer(REQUEST_SERVLET_CONTEXT_ANSWER).when(context).getServletContext();
            doAnswer(REQUEST_CONTEXT_PATH_ANSWER).when(context).getContextPath();
            doAnswer(ATTRIBUTE_ANSWER).when(context).getAttribute(anyString());
            doAnswer(SCOPED_ATTRIBUTE_ANSWER).when(context).getAttribute(anyString(), anyInt());
            doAnswer(ATTRIBUTES_ANSWER).when(context).getAttributes();
            doAnswer(SCOPED_ATTRIBUTES_ANSWER).when(context).getAttributes(anyInt());
        }
        for (WebContextStubbingOperation stubbing : stubbings) {
            stubbing.of(context);
        }
        return context;
    }

    /**
     * Creates (or reuses) a {@link SystemContext} mock and registers it at {@link MgnlContext}. Any provided stubbing operations are applied afterwards.
     * If a system context instance already exists it is reused; otherwise a new mock is created via the Magnolia component provider utilities.
     *
     * @param stubbings optional {@link SystemContextStubbingOperation}s to customize returned mock
     * @return the managed {@link SystemContext} Mockito mock
     */
    public static SystemContext mockSystemContext(SystemContextStubbingOperation... stubbings) {
        SystemContext result = MgnlContext.isSystemInstance() ? (SystemContext) MgnlContext.getInstance() : mockComponentInstance(SystemContext.class);
        MgnlContext.setInstance(result);
        for (SystemContextStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Creates (or reuses) an {@link AggregationState} mock bound to a managed {@link WebContext}. If an aggregation state already exists it is reused and further stubbings are applied.
     * When absent a new {@link ExtendedAggregationState} mock is created. Finally the web context is updated to return the aggregation state.
     *
     * @param stubbings zero or more {@link AggregationStateStubbingOperation} to configure the mock
     * @return the aggregation state mock currently active in the web context
     * @throws RepositoryException if creation of a backing web context triggers a repository error
     */
    public static AggregationState mockAggregationState(AggregationStateStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        WebContext context = mockWebContext();
        AggregationState aggState = context.getAggregationState();
        if (aggState == null) {
            aggState = mock(ExtendedAggregationState.class);
        }
        for (AggregationStateStubbingOperation stubbing : stubbings) {
            stubbing.of(aggState);
        }
        stubAggregationState(aggState).of(context);
        return aggState;
    }

    /**
     * Creates and registers a {@link QueryManager} mock for the specified workspace and applies optional stubbings.
     * Ensures a {@link WebContext} with a JCR session for the workspace exists before delegating to {@link QueryMockUtils#mockQueryManager(String, QueryManagerStubbingOperation...)}.
     *
     * @param workspace the JCR workspace name (e.g. "website")
     * @param stubbings optional {@link QueryManagerStubbingOperation}s to configure the returned manager
     * @return a {@link QueryManager} mock tied to the mocked session of the given workspace
     * @throws RepositoryException if JCR session mocking fails
     */
    @SuppressWarnings("unused")
    public static QueryManager mockQueryManager(final String workspace, QueryManagerStubbingOperation... stubbings) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryManager(workspace, stubbings);
    }

    /**
     * Creates a {@link Query} mock for the given workspace, language and statement.
     * A {@link QueryManager} mock is prepared first (including a JCR session in the web context) and the provided stubbings are applied to the query.
     *
     * @param workspace the JCR workspace name
     * @param language the query language (e.g. "JCR-SQL2" or "xpath")
     * @param statement the query statement to be returned by the mock
     * @param stubbings optional {@link QueryStubbingOperation}s to further customize the query behaviour
     * @return the configured {@link Query} mock
     * @throws RepositoryException if underlying JCR mocking fails
     */
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static Query mockQuery(final String workspace, final String language, final String statement, QueryStubbingOperation... stubbings) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryWithManager(workspace, language, statement, stubbings);
    }

    /**
     * Creates a {@link QueryResult} mock pre-populated with the given node results. Also prepares a {@link Query} mock returning that result.
     * Useful when tests need both the query and its result but want to focus on result processing.
     *
     * @param workspace the JCR workspace name
     * @param queryLang the query language
     * @param queryStatement the query statement
     * @param results zero or more {@link Node} instances to appear as rows in the mocked result
     * @return a {@link QueryResult} mock representing the supplied node list
     * @throws RepositoryException if JCR session or query stubbing fails
     */
    public static QueryResult mockQueryResult(final String workspace, final String queryLang, final String queryStatement, final Node... results) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryResult(workspace, queryLang, queryStatement, results);
    }

    /**
     * Creates a {@link QueryResult} mock pre-populated with the given {@link Row} objects. Also prepares a {@link Query} mock returning that result.
     * Use this when row-level meta data (score, path, selector names) is relevant to the test.
     *
     * @param workspace the JCR workspace name
     * @param queryLang the query language
     * @param queryStatement the query statement
     * @param results zero or more {@link Row} instances to appear in the mocked result
     * @return a {@link QueryResult} mock exposing the supplied rows
     * @throws RepositoryException if JCR session or query stubbing fails
     */
    @SuppressWarnings("unused")
    public static QueryResult mockRowQueryResult(final String workspace, final String queryLang, final String queryStatement, final Row... results) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockRowQueryResult(workspace, queryLang, queryStatement, results);
    }

    /**
     * Creates an empty {@link QueryResult} mock and stubs a corresponding {@link Query} to return it.
     * Convenience for testing handling of zero-hit queries without manual construction of result state.
     *
     * @param workspace the JCR workspace name
     * @param queryLang the query language
     * @param queryStatement the query statement
     * @return a {@link QueryResult} mock with no rows/nodes
     * @throws RepositoryException if JCR session or query stubbing fails
     */
    @SuppressWarnings("unused")
    public static QueryResult mockEmptyQueryResult(final String workspace, final String queryLang, final String queryStatement) throws RepositoryException {
        QueryResult result = QueryMockUtils.mockEmptyQueryResult();
        mockQuery(workspace, queryLang, queryStatement, QueryStubbingOperation.stubResult(result));
        return result;
    }

    /**
     * Clears all globally registered Magnolia mocks and resets the static {@link MgnlContext} and cached components.
     * Removes the current context instance, clears component provider caches and resets any JCR session mocks via {@link SessionMockUtils#cleanSession()}.
     * Invoke after each test to guarantee isolation and avoid state leakage between test cases.
     */
    public static void cleanContext() {
        MgnlContext.setInstance(null);
        clearComponentProvider();
        SessionMockUtils.cleanSession();
    }

    /**
     * Answer implementation delegating {@link WebContext#getParameter(String)} to the underlying {@link HttpServletRequest} parameter retrieval.
     * Returns {@code null} if no request is available.
     */
    private static final Answer<String> REQUEST_PARAMETER_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        return context.getRequest() != null ? context.getRequest().getParameter(name) : null;
    };

    /**
     * Answer implementation delegating {@link WebContext#getParameterValues(String)} to the underlying request.
     * Returns {@code null} if the request or the parameter is absent.
     */
    private static final Answer<String[]> REQUEST_PARAMETER_VALUES_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        return context.getRequest() != null ? context.getRequest().getParameterValues(name) : null;
    };

    /**
     * Answer implementation returning the full parameter map from the request or {@code null} if no request is present.
     */
    private static final Answer<Map<String, String[]>> REQUEST_PARAMETERS_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getParameterMap() : null;
    };

    /**
     * Answer implementation exposing the request context path or {@code null} if unavailable.
     */
    private static final Answer<String> REQUEST_CONTEXT_PATH_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getContextPath() : null;
    };

    /**
     * Answer implementation returning the {@link ServletContext} of the current request or {@code null}.
     */
    private static final Answer<ServletContext> REQUEST_SERVLET_CONTEXT_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getServletContext() : null;
    };

    /**
     * Answer implementation resolving a generic attribute from request scope first, then session scope.
     * Returns {@code null} if neither scope provides the attribute.
     */
    private static final Answer<Object> ATTRIBUTE_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Object result = null;
        HttpServletRequest request = context.getRequest();
        if (request != null) {
            result = context.getRequest().getAttribute(name);
            if (result == null && request.getSession() != null) {
                result = request.getSession().getAttribute(name);
            }
        }
        return result;
    };

    /**
     * Answer implementation resolving an attribute by explicit Magnolia scope constants.
     * Supported scopes: LOCAL (request), SESSION and APPLICATION (system context attributes). Returns {@code null} if not found.
     */
    private static final Answer<Object> SCOPED_ATTRIBUTE_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        int scope = (Integer) invocation.getArguments()[1];
        Object result = null;
        // mimic RequestAttributeStrategy:
        switch (scope) {
            case Context.LOCAL_SCOPE:
                result = getRequestAttribute(context, name);
                break;
            case Context.SESSION_SCOPE:
                result = getSessionAttribute(context, name);
                break;
            case Context.APPLICATION_SCOPE:
                result = ComponentsMockUtils.mockComponentInstance(SystemContext.class).getAttribute(name, Context.APPLICATION_SCOPE);
                break;
            default:
                break;
        }
        return result;
    };

    /**
     * Resolve an attribute from the request by name. Falls back to request parameters and selected request properties (character encoding, URI) if no attribute found.
     *
     * @param context the current web context (must not be {@code null})
     * @param name the attribute name to resolve
     * @return the resolved value or {@code null} if absent
     */
    @SuppressWarnings("deprecation")
    private static Object getRequestAttribute(final WebContext context, final String name) {
        Object result = null;
        if (context.getRequest() != null) {
            result = context.getRequest().getAttribute(name);
            if (result == null) {
                result = context.getRequest().getParameter(name);
            }
            if (result == null) {
                // we also expose some of the request properties as attributes
                if (WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING.equals(name)) {
                    result = context.getRequest().getCharacterEncoding();
                } else if (WebContext.ATTRIBUTE_REQUEST_URI.equals(name)) {
                    result = ServletUtil.stripPathParameters(context.getRequest().getRequestURI());
                }
            }
        }
        return result;
    }

    /**
     * Resolve an attribute from the HTTP session without creating a new session.
     *
     * @param context the current web context
     * @param name the attribute name
     * @return the session attribute value or {@code null}
     */
    private static Object getSessionAttribute(final WebContext context, final String name) {
        Object result = null;
        if (context.getRequest() != null) {
            HttpSession httpsession = context.getRequest().getSession(false);
            if (httpsession != null) {
                result = httpsession.getAttribute(name);
            }
        }
        return result;
    }

    /**
     * Answer implementation aggregating all accessible attributes from request, session and application scopes into a single map.
     * Map keys are attribute names; values are the corresponding objects. Empty map if no request available.
     */
    private static final Answer<Map<String, Object>> ATTRIBUTES_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        Map<String, Object> result = new HashMap<>();
        HttpServletRequest request = context.getRequest();
        if (request != null) {
            addRequestAttributes(context, result);
            addSessionAttributes(context, result);
            addSystemContextAttributes(result);
        }

        return result;
    };

    /**
     * Answer implementation collecting attributes for a single Magnolia scope provided as invocation argument.
     * Throws {@link IllegalArgumentException} for unsupported scopes.
     */
    private static final Answer<Map<String, Object>> SCOPED_ATTRIBUTES_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        int scope = (Integer) invocation.getArguments()[0];
        Map<String, Object> result = new HashMap<>();
        HttpServletRequest request = context.getRequest();
        if (request != null) {
            switch (scope) {
                case Context.LOCAL_SCOPE:
                    addRequestAttributes(context, result);
                    break;
                case Context.SESSION_SCOPE:
                    addSessionAttributes(context, result);
                    break;
                case Context.APPLICATION_SCOPE:
                    addSystemContextAttributes(result);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported scope" + scope);
            }
        }

        return result;
    };

    private static void addRequestAttributes(WebContext context, Map<String, Object> attributes) {
        HttpServletRequest request = context.getRequest();
        if (request != null) {
            Enumeration<String> enumeration = request.getAttributeNames();
            if (enumeration != null) {
                if (enumeration instanceof IteratorEnumeration) {
                    IteratorEnumeration<String> iteratorEnumeration = (IteratorEnumeration<String>) enumeration;
                    ResettableIterator<String> attributeNames = (ResettableIterator<String>) iteratorEnumeration.getIterator();
                    attributeNames.reset();
                    while (attributeNames.hasNext()) {
                        String name = attributeNames.next();
                        attributes.put(name, request.getAttribute(name));
                    }
                } else {
                    while (enumeration.hasMoreElements()) {
                        String name = enumeration.nextElement();
                        attributes.put(name, request.getAttribute(name));
                    }
                }
            }
        }
    }

    private static void addSessionAttributes(WebContext context, Map<String, Object> attributes) {
        HttpServletRequest request = context.getRequest();
        if (request != null && request.getSession() != null) {
            Enumeration<String> enumeration = request.getSession().getAttributeNames();
            if (enumeration != null) {
                if (enumeration instanceof IteratorEnumeration) {
                    IteratorEnumeration<String> iteratorEnumeration = (IteratorEnumeration<String>) enumeration;
                    ResettableIterator<String> attributeNames = (ResettableIterator<String>) iteratorEnumeration.getIterator();
                    attributeNames.reset();
                    while (attributeNames.hasNext()) {
                        String name = attributeNames.next();
                        attributes.put(name, request.getSession().getAttribute(name));
                    }
                } else {
                    while (enumeration.hasMoreElements()) {
                        String name = enumeration.nextElement();
                        attributes.put(name, request.getSession().getAttribute(name));
                    }
                }
            }
        }
    }

    /**
     * Add all application (system context) attributes to the map. Creates or reuses a mocked {@link SystemContext} component.
     *
     * @param attributes target map to populate
     */
    private static void addSystemContextAttributes(Map<String, Object> attributes) {
        SystemContext systemContext = mockComponentInstance(SystemContext.class);
        if (systemContext != null) {
            attributes.putAll(systemContext.getAttributes(Context.APPLICATION_SCOPE));
        }
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private ContextMockUtils() {
    }
}
