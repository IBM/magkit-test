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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @BeforeEach
    public void setUp() {
        cleanContext();
    }

    @Test
    public void cleanContextTest() throws RepositoryException {
        mockWebContext();
        assertNotNull(MgnlContext.getInstance());
        cleanContext();
        assertFalse(MgnlContext.hasInstance());
        assertThrows(IllegalStateException.class, MgnlContext::getInstance);
    }

    @Test
    public void mockWebContextTest() throws RepositoryException {
        assertFalse(MgnlContext.hasInstance());
        WebContext ctx = mockWebContext();
        assertNotNull(ctx);
        assertTrue(MgnlContext.hasInstance());
        assertTrue(MgnlContext.isWebContext());
        WebContext mgnlCtx = (WebContext) MgnlContext.getInstance();
        assertNotNull(mgnlCtx);
        assertNotNull(MgnlContext.getWebContext());
        assertNotNull(MgnlContext.getWebContextOrNull());
        assertEquals(mgnlCtx.hashCode(), ctx.hashCode());

        // test return existing mock:
        mockWebContext();
        mgnlCtx = MgnlContext.getWebContext();
        assertNotNull(mgnlCtx);
        assertEquals(mgnlCtx.hashCode(), ctx.hashCode());
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
        assertFalse(MgnlContext.hasInstance());
        AggregationState state = mockAggregationState();
        assertNotNull(state);
        assertNotNull(MgnlContext.getWebContext());
        AggregationState mgnlState = MgnlContext.getWebContext().getAggregationState();
        assertEquals(state, mgnlState);
        assertEquals(state, MgnlContext.getAggregationState());
        mockAggregationState();
        mgnlState = MgnlContext.getWebContext().getAggregationState();
        assertEquals(state, mgnlState);
    }

    @Test
    public void mockAggregationStateTestForStubbing() throws RepositoryException {
        AggregationStateStubbingOperation op1 = mock(AggregationStateStubbingOperation.class);
        AggregationStateStubbingOperation op2 = mock(AggregationStateStubbingOperation.class);
        AggregationState state = mockAggregationState(op1, op2);
        verify(op1, times(1)).of(state);
        verify(op2, times(1)).of(state);
    }

    @Test
    public void mockAggregationStateTestNull() {
        assertThrows(IllegalArgumentException.class, () -> mockAggregationState(null));
    }

    @Test
    public void mockSystemContext() throws RepositoryException {
        assertFalse(MgnlContext.hasInstance());
        SystemContextStubbingOperation op1 = mock(SystemContextStubbingOperation.class);
        SystemContextStubbingOperation op2 = mock(SystemContextStubbingOperation.class);
        SystemContext ctx = ContextMockUtils.mockSystemContext(op1, op2);
        assertEquals(ctx, MgnlContext.getInstance());
        assertTrue(MgnlContext.isSystemInstance());
        verify(op1, atLeastOnce()).of(ctx);
        verify(op2, atLeastOnce()).of(ctx);
        ContextMockUtils.mockSystemContext();
        assertEquals(ctx, MgnlContext.getInstance());
        mockWebContext();
        assertTrue(MgnlContext.isWebContext());
        SystemContext newCtx = ContextMockUtils.mockSystemContext();
        assertTrue(MgnlContext.isSystemInstance());
        assertSame(ctx, newCtx);
    }

    @Test
    public void mockWebContextLocaleVariant() throws Exception {
        assertFalse(MgnlContext.hasInstance());
        WebContext ctx = ContextMockUtils.mockWebContext(Locale.GERMANY);
        assertEquals(Locale.GERMANY, ctx.getLocale());
    }

    @Test
    public void requestParameterAnswersAndNullRequest() throws Exception {
        WebContext ctx = mockWebContext();
        mockWebContext(WebContextStubbingOperation.stubParameter("p", "v"), WebContextStubbingOperation.stubParameter("multi", "a", "b"));
        assertEquals("v", ctx.getParameter("p"));
        assertEquals("b", ctx.getParameterValues("multi")[1]);
        assertEquals(2, ctx.getParameterValues("multi").length);
        WebContextStubbingOperation.stubRequest(null).of(ctx);
        assertNull(ctx.getRequest());
        assertNull(ctx.getParameter("p"));
        assertNull(ctx.getParameterValues("multi"));
        assertNull(ctx.getParameters());
        assertNull(ctx.getServletContext());
        assertNull(ctx.getContextPath());
    }

    @Test
    public void attributeAnswerFallsBackToSession() throws Exception {
        WebContext ctx = mockWebContext();
        mockWebContext(WebContextStubbingOperation.stubExistingRequest(HttpServletRequestStubbingOperation.stubHttpSession("sess", HttpSessionStubbingOperation.stubAttribute("sAttr", "val"))));
        assertEquals("val", ctx.getAttribute("sAttr"));
    }

    @Test
    public void scopedAttributeLocalUsesRequestProperties() throws Exception {
        WebContext ctx = mockWebContext();
        mockWebContext(WebContextStubbingOperation.stubExistingRequest(
            HttpServletRequestStubbingOperation.stubRequestUri("/a/b;c=d"),
            HttpServletRequestStubbingOperation.stubCharacterEncoding("UTF-8")
        ));
        assertEquals("/a/b", ctx.getAttribute(WebContext.ATTRIBUTE_REQUEST_URI, WebContext.LOCAL_SCOPE));
        assertEquals("UTF-8", ctx.getAttribute(WebContext.ATTRIBUTE_REQUEST_CHARACTER_ENCODING, WebContext.LOCAL_SCOPE));
    }

    @Test
    public void getAttributesAggregatesAllScopes() throws Exception {
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
        Map<String, Object> all = ctx.getAttributes();
        assertEquals("rv", all.get("r1"));
        assertEquals("sv", all.get("s1"));
        assertEquals("av", all.get("a1"));
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
        assertEquals(1, local.size());
        assertEquals("rv", local.get("r1"));
        Map<String, Object> sess = ctx.getAttributes(WebContext.SESSION_SCOPE);
        assertEquals(1, sess.size());
        assertEquals("sv", sess.get("s1"));
        Map<String, Object> app = ctx.getAttributes(WebContext.APPLICATION_SCOPE);
        assertEquals(1, app.size());
        assertEquals("av", app.get("a1"));
    }

    @Test
    public void mockQueryManagerCreatesAndReuses() throws Exception {
        QueryManager qm1 = mockQueryManager("website");
        assertNotNull(qm1);
        QueryManager qm2 = mockQueryManager("website");
        assertEquals(qm1, qm2);
    }

    @Test
    public void mockQueryWithResultStub() throws Exception {
        QueryResult emptyResult = mockEmptyQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base]");
        Query q = mockQuery("website", "JCR-SQL2", "SELECT * FROM [nt:base]", QueryStubbingOperation.stubResult(emptyResult));
        assertEquals("JCR-SQL2", q.getLanguage());
        assertEquals("SELECT * FROM [nt:base]", q.getStatement());
        assertEquals(emptyResult, q.execute());
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
        assertEquals(2, count);
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
        assertEquals(2, count);
        assertEquals(1.3, score);
    }

    @Test
    public void mockRowQueryResultEmptyRows() throws Exception {
        QueryResult result = mockRowQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base] where 2=1");
        assertFalse(result.getRows().hasNext());
    }

    @Test
    public void mockEmptyQueryResultHasNoNodesOrRows() throws Exception {
        QueryResult result = mockEmptyQueryResult("website", "JCR-SQL2", "SELECT * FROM [nt:base] where 1=0");
        assertFalse(result.getNodes().hasNext());
        assertFalse(result.getRows().hasNext());
    }

    @Test
    public void requestContextPathAndServletContextAnswers() throws Exception {
        WebContext ctx = mockWebContext(
            WebContextStubbingOperation.stubExistingRequest(
                HttpServletRequestStubbingOperation.stubRequestUri("/x")
            ),
            WebContextStubbingOperation.stubContextPath("/app"));
        assertEquals("/app", ctx.getContextPath());
        assertNotNull(ctx.getServletContext());
    }

    @Test
    public void attributesAnswerWhenNoRequest() throws Exception {
        WebContext ctx = mockWebContext();
        WebContextStubbingOperation.stubRequest(null).of(ctx);
        assertTrue(ctx.getAttributes().isEmpty());
        assertTrue(ctx.getAttributes(WebContext.LOCAL_SCOPE).isEmpty());
    }

    @Test
    public void scopedAttributesApplicationEmpty() throws Exception {
        WebContext ctx = mockWebContext();
        assertTrue(ctx.getAttributes(WebContext.APPLICATION_SCOPE).isEmpty());
    }
}
