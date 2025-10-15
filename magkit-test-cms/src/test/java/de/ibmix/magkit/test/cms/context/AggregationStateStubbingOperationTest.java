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
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.*;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * No surprise here. Tests for AggregationStateStubbingOperation.
 *
 * Merged with former AggregationStateAdditionalStubbingOperationTest for consolidated coverage.
 *
 * @author wolf.bubenik
 * @since 2011-04-14
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
        stubSelector("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubExtensionForNull() throws Exception {
        stubExtension("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubHandleForNull() throws Exception {
        stubHandle("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubCharacterEncodingForNull() throws Exception {
        stubCharacterEncoding("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubRepositoryForNull() throws Exception {
        stubRepository("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubLocaleForNull() throws Exception {
        stubLocale(Locale.CANADA).of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubCurrentUriForNull() throws Exception {
        stubCurrentUri("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalBrowserUriForNull() throws Exception {
        stubOriginalBrowserUri("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalBrowserUrlForNull() throws Exception {
        stubOriginalBrowserUrl("").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubOriginalUriForNull() throws Exception {
        stubOriginalUri("").of(null);
    }

    @Test
    public void testStubSelector() throws Exception {
        stubSelector(null).of(_state);
        assertThat(_state.getSelector(), nullValue());
        assertThat(_state.getSelectors().length, is(0));

        stubSelector("").of(_state);
        assertThat(_state.getSelector(), is(""));
        assertThat(_state.getSelectors().length, is(0));

        stubSelector("selector").of(_state);
        assertThat(_state.getSelector(), is("selector"));
        assertThat(_state.getSelectors()[0], is("selector"));
        assertThat(mockWebContext().getAttribute("selector"), nullValue());

        stubSelector("selector1=test~selector2=aperto~selector2=notAsRequestAttribute").of(_state);
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
        stubExtension("html").of(_state);
        assertThat(_state.getExtension(), is("html"));
    }

    @Test
    public void testStubHandle() throws Exception {
        stubHandle("handle").of(_state);
        assertThat(_state.getHandle(), is("handle"));
    }

    @Test
    public void testStubCharacterEncoding() throws Exception {
        stubCharacterEncoding("UTF-8").of(_state);
        assertThat(_state.getCharacterEncoding(), is("UTF-8"));
    }

    @Test
    public void testStubRepository() throws Exception {
        stubRepository("repository").of(_state);
        assertThat(_state.getRepository(), is("repository"));
    }

    @Test
    public void testStubLocale() throws Exception {
        stubLocale(Locale.CANADA).of(_state);
        assertThat(_state.getLocale(), is(Locale.CANADA));
    }

    @Test
    public void testStubCurrentUri() throws Exception {
        stubCurrentUri("uri").of(_state);
        assertThat(_state.getCurrentURI(), is("uri"));
    }

    @Test
    public void stubPreviewModeTest() throws Exception {
        assertThat(_state.isPreviewMode(), is(false));
        stubPreviewMode(true).of(_state);
        assertThat(_state.isPreviewMode(), is(true));
    }

    @Test
    public void testStubOriginalBrowserUri() throws Exception {
        stubOriginalBrowserUri("uri").of(_state);
        assertThat(_state.getOriginalBrowserURI(), is("uri"));
    }

    @Test
    public void testStubOriginalBrowserUrl() throws Exception {
        stubOriginalBrowserUrl("uri").of(_state);
        assertThat(_state.getOriginalBrowserURL(), is("uri"));
    }

    @Test
    public void testStubOriginalUri() throws Exception {
        stubOriginalUri("uri").of(_state);
        assertThat(_state.getOriginalURI(), is("uri"));
    }

    @Test
    public void testStubOriginalUrl() throws Exception {
        stubOriginalUrl("uri").of(_state);
        assertThat(_state.getOriginalURL(), is("uri"));
    }

    @Test
    public void stubChannelTest() throws Exception {
        assertThat(_state.getChannel(), nullValue());

        stubChannel("Landwehrkanal").of(_state);
        assertThat(_state.getChannel(), notNullValue());
        assertThat(_state.getChannel().getName(), is("Landwehrkanal"));
    }

    // --- Merged additional coverage tests below ---

    @Test
    public void stubMainContentNodeNullDoesNotSetHandleOrRepository() throws Exception {
        assertThat(_state.getMainContentNode(), nullValue());
        assertThat(_state.getHandle(), nullValue());
        assertThat(_state.getRepository(), nullValue());
        stubMainContentNode((Node) null).of(_state);
        assertThat(_state.getMainContentNode(), nullValue());
        assertThat(_state.getHandle(), nullValue());
        assertThat(_state.getRepository(), nullValue());
    }

    @Test
    public void stubMainContentNodeWithNodeSetsHandleAndRepository() throws Exception {
        Node page = mockPageNode("/site/en/home");
        stubMainContentNode(page).of(_state);
        assertThat(_state.getMainContentNode(), is(page));
        assertThat(_state.getHandle(), is(page.getPath()));
        assertThat(_state.getRepository(), is(page.getSession().getWorkspace().getName()));
    }

    @Test
    public void stubMainContentNodeWithPathConvenience() throws Exception {
        stubMainContentNode("/site/en/about").of(_state);
        assertThat(_state.getMainContentNode(), notNullValue());
        assertThat(_state.getMainContentNode().getPath(), is("/site/en/about"));
        assertThat(_state.getHandle(), is("/site/en/about"));
        assertThat(_state.getRepository(), is("website"));
    }

    @Test
    public void stubCurrentContentNodeWithNodeSetsHandleAndRepository() throws Exception {
        Node page = mockPageNode("/site/de/home");
        stubCurrentContentNode(page).of(_state);
        assertThat(_state.getCurrentContentNode(), is(page));
        assertThat(_state.getHandle(), is(page.getPath()));
        assertThat(_state.getRepository(), is(page.getSession().getWorkspace().getName()));
    }

    @Test
    public void stubCurrentContentNodeWithPathConvenience() throws Exception {
        stubCurrentContentNode("/site/de/about").of(_state);
        assertThat(_state.getCurrentContentNode(), notNullValue());
        assertThat(_state.getCurrentContentNode().getPath(), is("/site/de/about"));
        assertThat(_state.getHandle(), is("/site/de/about"));
        assertThat(_state.getRepository(), is("website"));
    }

    @Test
    public void stubSelectorsNullArray() throws Exception {
        stubHandle("h").of(_state);
        stubRepository("r").of(_state);
        stubSelectors(null).of(_state);
        assertThat(_state.getSelectors(), nullValue());
        assertThat(_state.getHandle(), is("h"));
        assertThat(_state.getRepository(), is("r"));
    }

    @Test
    public void stubSelectorsIgnoresMalformedAndMultiEquals() throws Exception {
        stubSelectors(new String[]{"foo", "bar=baz", "x=y=z"}).of(_state);
        String[] selectors = _state.getSelectors();
        assertThat(selectors.length, is(3));
        assertThat(selectors[0], is("foo"));
        assertThat(selectors[1], is("bar=baz"));
        assertThat(selectors[2], is("x=y=z"));
        assertThat(mockWebContext().getAttribute("bar"), is("baz"));
        assertThat(mockWebContext().getAttribute("foo"), nullValue());
        assertThat(mockWebContext().getAttribute("x"), nullValue());
    }

    @Test
    public void stubSelectorsDoesNotOverwriteExistingAttribute() throws Exception {
        mockWebContext(WebContextStubbingOperation.stubAttribute("exists", "old"));
        stubSelectors(new String[]{"exists=new", "exists=other"}).of(_state);
        assertThat(mockWebContext().getAttribute("exists"), is("old"));
        assertThat(_state.getSelectors().length, is(2));
    }

    @Test
    public void combinedMainAndCurrentContentNodesLastWinsForHandleAndRepository() throws Exception {
        Node main = mockPageNode("/site/en/main");
        Node current = mockPageNode("/site/en/main/section");
        stubMainContentNode(main).of(_state);
        stubCurrentContentNode(current).of(_state);
        assertThat(_state.getMainContentNode(), is(main));
        assertThat(_state.getCurrentContentNode(), is(current));
        assertThat(_state.getHandle(), is(current.getPath()));
        assertThat(_state.getRepository(), is(current.getSession().getWorkspace().getName()));
    }

    @Test
    public void stubCurrentContentNodeNullDoesNotOverrideHandleOrRepository() throws Exception {
        Node main = mockPageNode("/site/en/main2");
        stubMainContentNode(main).of(_state);
        String expectedHandle = _state.getHandle();
        String expectedRepo = _state.getRepository();
        stubCurrentContentNode((Node) null).of(_state);
        assertThat(_state.getCurrentContentNode(), nullValue());
        assertThat(_state.getHandle(), is(expectedHandle));
        assertThat(_state.getRepository(), is(expectedRepo));
    }
}
