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

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
}
