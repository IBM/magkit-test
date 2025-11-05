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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import javax.jcr.Node;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.*;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @BeforeEach
    public void setUp() throws Exception {
        cleanContext();
        _state = mockAggregationState();
    }

    @Test
    public void testStubSelectorForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubSelector("").of(null));
    }

    @Test
    public void testStubExtensionForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubExtension("").of(null));
    }

    @Test
    public void testStubHandleForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubHandle("").of(null));
    }

    @Test
    public void testStubCharacterEncodingForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubCharacterEncoding("").of(null));
    }

    @Test
    public void testStubRepositoryForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubRepository("").of(null));
    }

    @Test
    public void testStubLocaleForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubLocale(Locale.CANADA).of(null));
    }

    @Test
    public void testStubCurrentUriForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubCurrentUri("").of(null));
    }

    @Test
    public void testStubOriginalBrowserUriForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubOriginalBrowserUri("").of(null));
    }

    @Test
    public void testStubOriginalBrowserUrlForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubOriginalBrowserUrl("").of(null));
    }

    @Test
    public void testStubOriginalUriForNull() {
        assertThrows(IllegalArgumentException.class, () -> stubOriginalUri("").of(null));
    }

    @Test
    public void testStubSelector() throws Exception {
        stubSelector(null).of(_state);
        assertNull(_state.getSelector());
        assertEquals(0, _state.getSelectors().length);

        stubSelector("").of(_state);
        assertEquals("", _state.getSelector());
        assertEquals(0, _state.getSelectors().length);

        stubSelector("selector").of(_state);
        assertEquals("selector", _state.getSelector());
        assertEquals("selector", _state.getSelectors()[0]);
        assertNull(mockWebContext().getAttribute("selector"));

        stubSelector("selector1=test~selector2=aperto~selector2=notAsRequestAttribute").of(_state);
        assertEquals("selector1=test~selector2=aperto~selector2=notAsRequestAttribute", _state.getSelector());
        assertNotNull(_state.getSelectors());
        assertEquals("selector1=test", _state.getSelectors()[0]);
        assertEquals("selector2=aperto", _state.getSelectors()[1]);
        assertEquals("selector2=notAsRequestAttribute", _state.getSelectors()[2]);
        assertEquals(2, mockWebContext().getAttributes().size());
        assertEquals("test", mockWebContext().getAttribute("selector1"));
        assertEquals("aperto", mockWebContext().getAttribute("selector2"));
    }

    @Test
    public void testStubExtension() throws Exception {
        stubExtension("html").of(_state);
        assertEquals("html", _state.getExtension());
    }

    @Test
    public void testStubHandle() throws Exception {
        stubHandle("handle").of(_state);
        assertEquals("handle", _state.getHandle());
    }

    @Test
    public void testStubCharacterEncoding() throws Exception {
        stubCharacterEncoding("UTF-8").of(_state);
        assertEquals("UTF-8", _state.getCharacterEncoding());
    }

    @Test
    public void testStubRepository() throws Exception {
        stubRepository("repository").of(_state);
        assertEquals("repository", _state.getRepository());
    }

    @Test
    public void testStubLocale() throws Exception {
        stubLocale(Locale.CANADA).of(_state);
        assertEquals(Locale.CANADA, _state.getLocale());
    }

    @Test
    public void testStubCurrentUri() throws Exception {
        stubCurrentUri("uri").of(_state);
        assertEquals("uri", _state.getCurrentURI());
    }

    @Test
    public void stubPreviewModeTest() throws Exception {
        assertFalse(_state.isPreviewMode());
        stubPreviewMode(true).of(_state);
        assertTrue(_state.isPreviewMode());
    }

    @Test
    public void testStubOriginalBrowserUri() throws Exception {
        stubOriginalBrowserUri("uri").of(_state);
        assertEquals("uri", _state.getOriginalBrowserURI());
    }

    @Test
    public void testStubOriginalBrowserUrl() throws Exception {
        stubOriginalBrowserUrl("uri").of(_state);
        assertEquals("uri", _state.getOriginalBrowserURL());
    }

    @Test
    public void testStubOriginalUri() throws Exception {
        stubOriginalUri("uri").of(_state);
        assertEquals("uri", _state.getOriginalURI());
    }

    @Test
    public void testStubOriginalUrl() throws Exception {
        stubOriginalUrl("uri").of(_state);
        assertEquals("uri", _state.getOriginalURL());
    }

    @Test
    public void stubChannelTest() throws Exception {
        assertNull(_state.getChannel());

        stubChannel("Landwehrkanal").of(_state);
        assertNotNull(_state.getChannel());
        assertEquals("Landwehrkanal", _state.getChannel().getName());
    }

    // --- Merged additional coverage tests below ---

    @Test
    public void stubMainContentNodeNullDoesNotSetHandleOrRepository() throws Exception {
        assertNull(_state.getMainContentNode());
        assertNull(_state.getHandle());
        assertNull(_state.getRepository());
        stubMainContentNode(null).of(_state);
        assertNull(_state.getMainContentNode());
        assertNull(_state.getHandle());
        assertNull(_state.getRepository());
    }

    @Test
    public void stubMainContentNodeWithNodeSetsHandleAndRepository() throws Exception {
        Node page = mockPageNode("/site/en/home");
        stubMainContentNode(page).of(_state);
        assertEquals(page, _state.getMainContentNode());
        assertEquals(page.getPath(), _state.getHandle());
        assertEquals(page.getSession().getWorkspace().getName(), _state.getRepository());
    }

    @Test
    public void stubMainContentNodeWithPathConvenience() throws Exception {
        stubMainContentNode("/site/en/about").of(_state);
        assertNotNull(_state.getMainContentNode());
        assertEquals("/site/en/about", _state.getMainContentNode().getPath());
        assertEquals("/site/en/about", _state.getHandle());
        assertEquals("website", _state.getRepository());
    }

    @Test
    public void stubCurrentContentNodeWithNodeSetsHandleAndRepository() throws Exception {
        Node page = mockPageNode("/site/de/home");
        stubCurrentContentNode(page).of(_state);
        assertEquals(page, _state.getCurrentContentNode());
        assertEquals(page.getPath(), _state.getHandle());
        assertEquals(page.getSession().getWorkspace().getName(), _state.getRepository());
    }

    @Test
    public void stubCurrentContentNodeWithPathConvenience() throws Exception {
        stubCurrentContentNode("/site/de/about").of(_state);
        assertNotNull(_state.getCurrentContentNode());
        assertEquals("/site/de/about", _state.getCurrentContentNode().getPath());
        assertEquals("/site/de/about", _state.getHandle());
        assertEquals("website", _state.getRepository());
    }

    @Test
    public void stubSelectorsNullArray() throws Exception {
        stubHandle("h").of(_state);
        stubRepository("r").of(_state);
        stubSelectors(null).of(_state);
        assertNull(_state.getSelectors());
        assertEquals("h", _state.getHandle());
        assertEquals("r", _state.getRepository());
    }

    @Test
    public void stubSelectorsIgnoresMalformedAndMultiEquals() throws Exception {
        stubSelectors(new String[]{"foo", "bar=baz", "x=y=z"}).of(_state);
        String[] selectors = _state.getSelectors();
        assertEquals(3, selectors.length);
        assertEquals("foo", selectors[0]);
        assertEquals("bar=baz", selectors[1]);
        assertEquals("x=y=z", selectors[2]);
        assertEquals("baz", mockWebContext().getAttribute("bar"));
        assertNull(mockWebContext().getAttribute("foo"));
        assertNull(mockWebContext().getAttribute("x"));
    }

    @Test
    public void stubSelectorsDoesNotOverwriteExistingAttribute() throws Exception {
        mockWebContext(WebContextStubbingOperation.stubAttribute("exists", "old"));
        stubSelectors(new String[]{"exists=new", "exists=other"}).of(_state);
        assertEquals("old", mockWebContext().getAttribute("exists"));
        assertEquals(2, _state.getSelectors().length);
    }

    @Test
    public void combinedMainAndCurrentContentNodesLastWinsForHandleAndRepository() throws Exception {
        Node main = mockPageNode("/site/en/main");
        Node current = mockPageNode("/site/en/main/section");
        stubMainContentNode(main).of(_state);
        stubCurrentContentNode(current).of(_state);
        assertEquals(main, _state.getMainContentNode());
        assertEquals(current, _state.getCurrentContentNode());
        assertEquals(current.getPath(), _state.getHandle());
        assertEquals(current.getSession().getWorkspace().getName(), _state.getRepository());
    }

    @Test
    public void stubCurrentContentNodeNullDoesNotOverrideHandleOrRepository() throws Exception {
        Node main = mockPageNode("/site/en/main2");
        stubMainContentNode(main).of(_state);
        String expectedHandle = _state.getHandle();
        String expectedRepo = _state.getRepository();
        stubCurrentContentNode(null).of(_state);
        assertNull(_state.getCurrentContentNode());
        assertEquals(expectedHandle, _state.getHandle());
        assertEquals(expectedRepo, _state.getRepository());
    }
}
