package de.ibmix.magkit.mockito;

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
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static de.ibmix.magkit.mockito.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.mockito.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.mockito.ContextMockUtils.mockWebContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * No surprise here. Tests for AggregationStateStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 14.04.11
 */
public class AggregationStateStubbingOperationTest {

    private AggregationState _state;

    @Before
    public void setUp() throws Exception {
        cleanContext();
        _state = mockAggregationState();
    }

    @Test(expected = AssertionError.class)
    public void testStubSelectorForNull() throws Exception {
        AggregationStateStubbingOperation.stubSelector("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubExtensionForNull() throws Exception {
        AggregationStateStubbingOperation.stubExtension("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubHandleForNull() throws Exception {
        AggregationStateStubbingOperation.stubHandle("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubCharacterEncodingForNull() throws Exception {
        AggregationStateStubbingOperation.stubCharacterEncoding("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubRepositoryForNull() throws Exception {
        AggregationStateStubbingOperation.stubRepository("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubLocaleForNull() throws Exception {
        AggregationStateStubbingOperation.stubLocale(Locale.CANADA).of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubCurrentUriForNull() throws Exception {
        AggregationStateStubbingOperation.stubCurrentUri("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalBrowserUriForNull() throws Exception {
        AggregationStateStubbingOperation.stubOriginalBrowserUri("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalBrowserUrlForNull() throws Exception {
        AggregationStateStubbingOperation.stubOriginalBrowserUrl("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalUriForNull() throws Exception {
        AggregationStateStubbingOperation.stubOriginalUri("").of(null);
    }

    @Test
    public void testStubSelector() throws Exception {
        AggregationStateStubbingOperation.stubSelector(null).of(_state);
        assertThat(_state.getSelector(), nullValue());
        assertThat(_state.getSelectors().length, is(0));

        AggregationStateStubbingOperation.stubSelector("").of(_state);
        assertThat(_state.getSelector(), is(""));
        assertThat(_state.getSelectors().length, is(0));

        AggregationStateStubbingOperation.stubSelector("selector").of(_state);
        assertThat(_state.getSelector(), is("selector"));
        assertThat(_state.getSelectors()[0], is("selector"));
        assertThat(mockWebContext().getAttribute("selector"), nullValue());

        AggregationStateStubbingOperation.stubSelector("selector1=test~selector2=aperto~selector2=notAsRequestAttribute").of(_state);
        assertThat(_state.getSelector(), is("selector1=test~selector2=aperto~selector2=notAsRequestAttribute"));
        assertThat(_state.getSelectors(), notNullValue());
        assertThat(_state.getSelectors()[0], is("selector1=test"));
        assertThat(_state.getSelectors()[1], is("selector2=aperto"));
        assertThat(_state.getSelectors()[2], is("selector2=notAsRequestAttribute"));
        assertThat(mockWebContext().getAttributes().size(), is(2));
        assertThat(mockWebContext().getAttribute("selector1"), is("test"));
        assertThat(mockWebContext().getAttribute("selector2"), is("aperto"));
    }

    @Test
    public void testStubExtension() throws Exception {
        AggregationStateStubbingOperation.stubExtension("html").of(_state);
        assertThat(_state.getExtension(), is("html"));
    }

    @Test
    public void testStubHandle() throws Exception {
        AggregationStateStubbingOperation.stubHandle("handle").of(_state);
        assertThat(_state.getHandle(), is("handle"));
    }

    @Test
    public void testStubCharacterEncoding() throws Exception {
        AggregationStateStubbingOperation.stubCharacterEncoding("UTF-8").of(_state);
        assertThat(_state.getCharacterEncoding(), is("UTF-8"));
    }

    @Test
    public void testStubRepository() throws Exception {
        AggregationStateStubbingOperation.stubRepository("repository").of(_state);
        assertThat(_state.getRepository(), is("repository"));
    }

    @Test
    public void testStubLocale() throws Exception {
        AggregationStateStubbingOperation.stubLocale(Locale.CANADA).of(_state);
        assertThat(_state.getLocale(), is(Locale.CANADA));
    }

    @Test
    public void testStubCurrentUri() throws Exception {
        AggregationStateStubbingOperation.stubCurrentUri("uri").of(_state);
        assertThat(_state.getCurrentURI(), is("uri"));
    }

    @Test
    public void stubPreviewModeTest() throws Exception {
        assertThat(_state.isPreviewMode(), is(false));
        AggregationStateStubbingOperation.stubPreviewMode(true).of(_state);
        assertThat(_state.isPreviewMode(), is(true));
    }

    @Test
    public void testStubOriginalBrowserUri() throws Exception {
        AggregationStateStubbingOperation.stubOriginalBrowserUri("uri").of(_state);
        assertThat(_state.getOriginalBrowserURI(), is("uri"));
    }

    @Test
    public void testStubOriginalBrowserUrl() throws Exception {
        AggregationStateStubbingOperation.stubOriginalBrowserUrl("uri").of(_state);
        assertThat(_state.getOriginalBrowserURL(), is("uri"));
    }

    @Test
    public void testStubOriginalUri() throws Exception {
        AggregationStateStubbingOperation.stubOriginalUri("uri").of(_state);
        assertThat(_state.getOriginalURI(), is("uri"));
    }

    @Test
    public void testStubOriginalUrl() throws Exception {
        AggregationStateStubbingOperation.stubOriginalUrl("uri").of(_state);
        assertThat(_state.getOriginalURL(), is("uri"));
    }

    @Test
    public void stubChannelTest() throws Exception {
        assertThat(_state.getChannel(), nullValue());

        AggregationStateStubbingOperation.stubChannel("Landwehrkanal").of(_state);
        assertThat(_state.getChannel(), notNullValue());
        assertThat(_state.getChannel().getName(), is("Landwehrkanal"));
    }
}
