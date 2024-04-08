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
 * Collection of utility methods for mocking Mgnl Context with Mockito.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-08-27
 */
public final class ContextMockUtils extends ComponentsMockUtils {

    /**
     * Creates a WebContext mock and registers this instance at MgnlContext.
     * If MgnlContext already contains an WebContext instance, this instance will be returned.
     * getLocale() will be stubbed to return the provided locale.
     *
     * @param locale locale
     * @return the WebContext Mockito mock
     * @throws RepositoryException repository exception
     */
    public static WebContext mockWebContext(Locale locale) throws RepositoryException {
        return mockWebContext(WebContextStubbingOperation.stubLocale(locale));
    }

    /**
     * Creates a WebContext mock and registers this instance at MgnlContext.
     * If MgnlContext already contains an WebContext instance, this instance will be returned.
     *
     * @param stubbings web context stubbing operations
     * @return the WebContext Mockito mock
     * @throws RepositoryException repository exception
     */
    public static WebContext mockWebContext(WebContextStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        WebContext context;
        if (MgnlContext.hasInstance() && MgnlContext.isWebContext()) {
            // while mocking, it is assumed that getInstance() will always return a mock
            context = MgnlContext.getWebContext();
        } else {
            // support injection of WebContext mock - mock as component:
            context = mockComponentInstance(WebContext.class);
            MgnlContext.setInstance(context);
            // always provide a I18ContentSupport mock
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

    public static SystemContext mockSystemContext(SystemContextStubbingOperation... stubbings) {
        SystemContext result = MgnlContext.isSystemInstance() ? (SystemContext) MgnlContext.getInstance() : mockComponentInstance(SystemContext.class);
        MgnlContext.setInstance(result);
        for (SystemContextStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Creates an AggregationState mock. WebContext mock is created (if needed) and stubbed to return this AggregationState.
     * If the AggregationState already exists for the WebContext, it is kept and the StubbingOperations are applied for the existing WebContext.
     *
     * @param stubbings an array of AggregationStateStubbingOperation
     * @return the AggregationState Mockito mock
     * @throws RepositoryException repository exception
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

    public static QueryManager mockQueryManager(final String workspace, QueryManagerStubbingOperation... stubbings) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryManager(workspace, stubbings);
    }

    public static Query mockQuery(final String workspace, final String language, final String statement, QueryStubbingOperation... stubbings) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryWithManager(workspace, language, statement, stubbings);
    }

    public static QueryResult mockQueryResult(final String workspace, final String queryLang, final String queryStatement, final Node... results) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockQueryResult(workspace, queryLang, queryStatement, results);
    }

    public static QueryResult mockRowQueryResult(final String workspace, final String queryLang, final String queryStatement, final Row... results) throws RepositoryException {
        mockWebContext(WebContextStubbingOperation.stubJcrSession(workspace));
        return QueryMockUtils.mockRowQueryResult(workspace, queryLang, queryStatement, results);
    }

    public static QueryResult mockEmptyQueryResult(final String workspace, final String queryLang, final String queryStatement) throws RepositoryException {
        QueryResult result = QueryMockUtils.mockEmptyQueryResult();
        mockQuery(workspace, queryLang, queryStatement, QueryStubbingOperation.stubResult(result));
        return result;
    }

    /**
     * Removes the WebContext (any MgnlContext instance) from MgnlContext and delete all other cached mocks (TemplateManager etc.)from magnolia ComponentProvider.
     */
    public static void cleanContext() {
        MgnlContext.setInstance(null);
        // delete all other cached mocks (TemplateManager etc.):
        clearComponentProvider();
        // clear jcr session:
        SessionMockUtils.cleanSession();
    }

    private static final Answer<String> REQUEST_PARAMETER_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        return context.getRequest() != null ? context.getRequest().getParameter(name) : null;
    };

    private static final Answer<String[]> REQUEST_PARAMETER_VALUES_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        return context.getRequest() != null ? context.getRequest().getParameterValues(name) : null;
    };

    private static final Answer<Map<String, String[]>> REQUEST_PARAMETERS_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getParameterMap() : null;
    };

    private static final Answer<String> REQUEST_CONTEXT_PATH_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getContextPath() : null;
    };

    private static final Answer<ServletContext> REQUEST_SERVLET_CONTEXT_ANSWER = invocation -> {
        WebContext context = (WebContext) invocation.getMock();
        return context.getRequest() != null ? context.getRequest().getServletContext() : null;
    };

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
            // add request attributes:
            Enumeration<String> enumeration = request.getAttributeNames();
            if (enumeration != null) {
                ResettableIterator attributeNames = (ResettableIterator) ((IteratorEnumeration) enumeration).getIterator();
                attributeNames.reset();
                while (attributeNames.hasNext()) {
                    String name = attributeNames.next().toString();
                    attributes.put(name, request.getAttribute(name));
                }

            }
        }
    }

    private static void addSessionAttributes(WebContext context, Map<String, Object> attributes) {
        HttpServletRequest request = context.getRequest();
        if (request != null && request.getSession() != null) {
            // add request attributes:
            Enumeration<String> enumeration = request.getSession().getAttributeNames();
            if (enumeration != null) {
                ResettableIterator attributeNames = (ResettableIterator) ((IteratorEnumeration) enumeration).getIterator();
                attributeNames.reset();
                while (attributeNames.hasNext()) {
                    String name = attributeNames.next().toString();
                    attributes.put(name, request.getSession().getAttribute(name));
                }
            }
        }
    }

    private static void addSystemContextAttributes(Map<String, Object> attributes) {
        SystemContext systemContext = mockComponentInstance(SystemContext.class);
        if (systemContext != null) {
            attributes.putAll(systemContext.getAttributes(Context.APPLICATION_SCOPE));
        }
    }

    private ContextMockUtils() {
    }
}

