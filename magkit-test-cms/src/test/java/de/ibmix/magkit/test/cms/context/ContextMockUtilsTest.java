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

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.ibmix.magkit.test.jcr.NodeMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryMockUtils;
import de.ibmix.magkit.test.jcr.query.QueryStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation;
import de.ibmix.magkit.test.servlet.HttpSessionStubbingOperation;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryManager;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockRowQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockEmptyQueryResult;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for ContextMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-09-17
 */
public class ContextMockUtilsTest {

    @Before
    public void setUp() {
        cleanContext();
    }

    @Test(expected = IllegalStateException.class)
    public void cleanContextTest() throws RepositoryException {
        mockWebContext();
        assertThat(MgnlContext.getInstance(), notNullValue());
        cleanContext();

        // trigger Exception
        assertThat(MgnlContext.hasInstance(), is(false));
        MgnlContext.getInstance();
    }

    @Test
    public void mockWebContextTest() throws RepositoryException {
        assertThat(MgnlContext.hasInstance(), is(false));
        WebContext ctx = mockWebContext();
        assertThat(ctx, notNullValue());
        assertThat(MgnlContext.hasInstance(), is(true));
        assertThat(MgnlContext.isWebContext(), is(true));
        WebContext mgnlCtx = (WebContext) MgnlContext.getInstance();
        assertThat(mgnlCtx, notNullValue());
        assertThat(MgnlContext.getWebContext(), notNullValue());
        assertThat(MgnlContext.getWebContextOrNull(), notNullValue());
        assertThat(mgnlCtx.hashCode(), is(ctx.hashCode()));

        // test return existing mock:
        mockWebContext();
        mgnlCtx = MgnlContext.getWebContext();
        assertThat(mgnlCtx, notNullValue());
        // assert same instance as first mock
        assertThat(mgnlCtx.hashCode(), is(ctx.hashCode()));
    }

    @Test
    public void mockWebContextTestForStubbing() throws RepositoryException {
        WebContextStubbingOperation op1 = mock(WebContextStubbingOperation.class);
        WebContextStubbingOperation op2 = mock(WebContextStubbingOperation.class);
        WebContext mgnlCtx = mockWebContext(op1, op2);
        verify(op1, times(1)).of(mgnlCtx);
        verify(op2, times(1)).of(mgnlCtx);
    }

    @Test
    public void mockAggregationStateTest() throws RepositoryException {
        assertThat(MgnlContext.hasInstance(), is(false));
        AggregationState state = mockAggregationState();

        // validate that AggregationState and WebContext has been mocked
        assertThat(state, notNullValue());
        assertThat(MgnlContext.getWebContext(), notNullValue());

        // validate that WebContext has been mocked to return AggregationState
        AggregationState mgnlState = MgnlContext.getWebContext().getAggregationState();
        assertThat(mgnlState, is(state));
        assertThat(MgnlContext.getAggregationState(), is(state));

        // test that we get The same mock on repeated call
        mockAggregationState();
        mgnlState = MgnlContext.getWebContext().getAggregationState();
        assertThat(mgnlState, is(state));
    }

    @Test
    public void mockAggregationStateTestForStubbing() throws RepositoryException {
        AggregationStateStubbingOperation op1 = mock(AggregationStateStubbingOperation.class);
        AggregationStateStubbingOperation op2 = mock(AggregationStateStubbingOperation.class);
        AggregationState state = mockAggregationState(op1, op2);
        verify(op1, times(1)).of(state);
        verify(op2, times(1)).of(state);
    }

    @Test(expected = AssertionError.class)
    public void mockAggregationStateTestNull() throws RepositoryException {
        mockAggregationState(null);
    }

    @Test
    public void mockSystemContext() throws RepositoryException {
        assertFalse(MgnlContext.hasInstance());

        SystemContextStubbingOperation op1 = mock(SystemContextStubbingOperation.class);
        SystemContextStubbingOperation op2 = mock(SystemContextStubbingOperation.class);
        SystemContext ctx = ContextMockUtils.mockSystemContext(op1, op2);
        assertThat(MgnlContext.getInstance(), is(ctx));
        assertTrue(MgnlContext.isSystemInstance());
        verify(op1, atLeastOnce()).of(ctx);
        verify(op2, atLeastOnce()).of(ctx);

        ContextMockUtils.mockSystemContext();
        assertThat(MgnlContext.getInstance(), is(ctx));

        mockWebContext();
        assertTrue(MgnlContext.isWebContext());

        SystemContext newCtx = ContextMockUtils.mockSystemContext();
        assertTrue(MgnlContext.isSystemInstance());
        // We get a new instance from MgnlContext but the SystemContext instance is still the same because we mock it using mockComponentInstance(SystemContext.class)
        assertSame(newCtx, ctx);
    }

    @Test
    public void mockWebContextLocaleVariant() throws Exception {
        assertThat(MgnlContext.hasInstance(), is(false));
        WebContext ctx = ContextMockUtils.mockWebContext(Locale.GERMANY);
        assertThat(ctx.getLocale(), is(Locale.GERMANY));
    }

    @Test
    public void requestParameterAnswersAndNullRequest() throws Exception {
        WebContext ctx = mockWebContext();
        // add parameters via existing request stub chain
        mockWebContext(WebContextStubbingOperation.stubParameter("p", "v"), WebContextStubbingOperation.stubParameter("multi", "a", "b"));
        assertThat(ctx.getParameter("p"), is("v"));
        assertThat(ctx.getParameterValues("multi")[1], is("b"));
        assertThat(ctx.getParameterValues("multi").length, is(2));
        // now remove request entirely -> answers must return null
        WebContextStubbingOperation.stubRequest(null).of(ctx);
        assertThat(ctx.getRequest(), nullValue());
        assertThat(ctx.getParameter("p"), nullValue());
        assertThat(ctx.getParameterValues("multi"), nullValue());
        assertThat(ctx.getParameters(), nullValue());
        assertThat(ctx.getServletContext(), nullValue());
        assertThat(ctx.getContextPath(), nullValue());
    }

    @Test
    public void attributeAnswerFallsBackToSession() throws Exception {
        WebContext ctx = mockWebContext();
        // ensure session with attribute but no request attribute
        mockWebContext(WebContextStubbingOperation.stubExistingRequest(HttpServletRequestStubbingOperation.stubHttpSession("sess", HttpSessionStubbingOperation.stubAttribute("sAttr", "val"))));
        assertThat(ctx.getAttribute("sAttr"), is("val"));
    }

    @Test
    public void scopedAttributeLocalUsesRequestProperties() throws Exception {
        WebContext ctx = mockWebContext();
        mockWebContext(WebContextStubbingOperation.stubExistingRequest(
            HttpServletRequestStubbingOperation.stubRequestUri("/a/b;c=d"),
            HttpServletRequestStubbingOperation.stubCharacterEncoding("UTF-8")
        ));
        assertThat(ctx.getAttribute(WebContext.ATTRIBUTE_REQUEST_URI, WebContext.LOCAL_SCOPE), is("/a/b"));
        assertThat(ctx.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING, WebContext.LOCAL_SCOPE), is("UTF-8"));
    }

    @Test
    public void getAttributesAggregatesAllScopes() throws Exception {
        WebContext ctx = mockWebContext();
        // request attribute via stubbing operation
        mockWebContext(
            WebContextStubbingOperation.stubAttribute("r1", "rv"),
            WebContextStubbingOperation.stubExistingRequest(
                HttpServletRequestStubbingOperation.stubHttpSession("sess", HttpSessionStubbingOperation.stubAttribute("s1", "sv"))
            )
        );
        SystemContext sc = ComponentsMockUtils.mockComponentInstance(SystemContext.class);
        Map<String, Object> appAttrs = new HashMap<>();
        appAttrs.put("a1", "av");
        when(sc.getAttributes(WebContext.APPLICATION_SCOPE)).thenReturn(appAttrs);
        Map<String, Object> all = ctx.getAttributes();
        assertThat(all.get("r1"), is("rv"));
        assertThat(all.get("s1"), is("sv"));
        assertThat(all.get("a1"), is("av"));
    }

    @Test
    public void getScopedAttributesVariants() throws Exception {
        WebContext ctx = mockWebContext();
        mockWebContext(
            WebContextStubbingOperation.stubAttribute("r1", "rv"),
            WebContextStubbingOperation.stubExistingRequest(
                HttpServletRequestStubbingOperation.stubHttpSession("sess", HttpSessionStubbingOperation.stubAttribute("s1", "sv"))
            )
        );
        SystemContext sc = ComponentsMockUtils.mockComponentInstance(SystemContext.class);
        Map<String, Object> appAttrs = new HashMap<>();
        appAttrs.put("a1", "av");
        when(sc.getAttributes(WebContext.APPLICATION_SCOPE)).thenReturn(appAttrs);

        Map<String, Object> local = ctx.getAttributes(WebContext.LOCAL_SCOPE);
        assertThat(local.size(), is(1));
        assertThat(local.get("r1"), is("rv"));

        Map<String, Object> sess = ctx.getAttributes(WebContext.SESSION_SCOPE);
        assertThat(sess.size(), is(1));
        assertThat(sess.get("s1"), is("sv"));

        Map<String, Object> app = ctx.getAttributes(WebContext.APPLICATION_SCOPE);
        assertThat(app.size(), is(1));
        assertThat(app.get("a1"), is("av"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getScopedAttributesUnsupportedScope() throws Exception {
        WebContext ctx = mockWebContext();
        ctx.getAttributes(999);
    }

    @Test
    public void mockQueryManagerCreatesAndReuses() throws Exception {
        QueryManager qm1 = mockQueryManager("website");
        assertThat(qm1, notNullValue());
        QueryManager qm2 = mockQueryManager("website");
        assertThat(qm2, is(qm1));
    }

    @Test
    public void mockQueryWithResultStub() throws Exception {
        QueryResult emptyResult = mockEmptyQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base]");
        Query q = mockQuery("website", "JCR-SQL2", "SELECT * FROM [nt:base]", QueryStubbingOperation.stubResult(emptyResult));
        assertThat(q.getLanguage(), is("JCR-SQL2"));
        assertThat(q.getStatement(), is("SELECT * FROM [nt:base]"));
        assertThat(q.execute(), is(emptyResult));
    }

    @Test
    public void mockQueryResultWithNodes() throws Exception {
        Node n1 = NodeMockUtils.mockNode("/n1");
        Node n2 = NodeMockUtils.mockNode("/n2");
        QueryResult nodesResult = mockQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base]", n1, n2);
        int count = 0;
        var it = nodesResult.getNodes();
        while (it.hasNext()) {
            it.nextNode();
            count++;
        }
        assertThat(count, is(2));
    }

    @Test
    public void mockRowQueryResultWithRows() throws Exception {
        Row r1 = QueryMockUtils.mockRow(0.5);
        Row r2 = QueryMockUtils.mockRow(0.8);
        QueryResult result = mockRowQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base]", r1, r2);
        RowIterator rows = result.getRows();
        int count = 0;
        double score = 0.0;
        while (rows.hasNext()) {
            Row row = rows.nextRow();
            count++;
            score += row.getScore();
        }
        assertThat(count, is(2));
        assertThat(score, is(1.3));
    }

    @Test
    public void mockRowQueryResultEmptyRows() throws Exception {
        QueryResult result = mockRowQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base] where 2=1");
        assertThat(result.getRows().hasNext(), is(false));
    }

    @Test
    public void mockEmptyQueryResultHasNoNodesOrRows() throws Exception {
        QueryResult result = mockEmptyQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base] where 1=0");
        assertThat(result.getNodes().hasNext(), is(false));
        assertThat(result.getRows().hasNext(), is(false));
    }

    @Test
    public void requestContextPathAndServletContextAnswers() throws Exception {
        WebContext ctx = mockWebContext(
            WebContextStubbingOperation.stubExistingRequest(
                HttpServletRequestStubbingOperation.stubRequestUri("/x")
            ),
            WebContextStubbingOperation.stubContextPath("/app"));
        assertThat(ctx.getContextPath(), is("/app"));
        assertThat(ctx.getServletContext(), notNullValue());
    }

    @Test
    public void attributesAnswerWhenNoRequest() throws Exception {
        WebContext ctx = mockWebContext();
        WebContextStubbingOperation.stubRequest(null).of(ctx);
        assertThat(ctx.getAttributes().isEmpty(), is(true));
        assertThat(ctx.getAttributes(WebContext.LOCAL_SCOPE).isEmpty(), is(true));
    }

    @Test
    public void scopedAttributesApplicationEmpty() throws Exception {
        WebContext ctx = mockWebContext();
        assertThat(ctx.getAttributes(WebContext.APPLICATION_SCOPE).isEmpty(), is(true));
    }
}
