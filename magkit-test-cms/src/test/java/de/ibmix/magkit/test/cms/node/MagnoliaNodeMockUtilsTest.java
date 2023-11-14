package de.ibmix.magkit.test.cms.node;

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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.context.MgnlContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockAreaNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockContentNodeNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockMgnlNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static info.magnolia.cms.core.MgnlNodeType.NT_AREA;
import static info.magnolia.cms.core.MgnlNodeType.NT_COMPONENT;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENT;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;
import static info.magnolia.cms.core.MgnlNodeType.NT_PAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the NodeMockUtils test class.
 *
 * @author frank.sommer@ibmix.de
 * @since 2012-10-31
 */
public class MagnoliaNodeMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockPageNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        NodeStubbingOperation op2 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockPageNode(op1, op2);
        assertThat(node.getPrimaryNodeType(), notNullValue());
        assertThat(node.getPrimaryNodeType().getName(), is(NT_PAGE));
        assertThat(node.getNodes(), notNullValue());
        assertThat(node.getNodes().hasNext(), is(false));
        assertThat(node.getProperties(), notNullValue());
        verify(op1, times(1)).of(node);
        verify(op2, times(1)).of(node);

        ItemVisitor visitor = mock(ItemVisitor.class);
        node.accept(visitor);
        verify(visitor, times(1)).visit(node);
    }

    @Test
    public void mockAreaNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockAreaNode("test", op1);
        verify(op1, times(1)).of(node);
        assertThat(node.getPrimaryNodeType().getName(), is(NT_AREA));
    }

    @Test
    public void mockContentNodeNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNodeNode("test", op1);
        verify(op1, times(1)).of(node);
        assertThat(node.getPrimaryNodeType().getName(), is(NT_CONTENTNODE));
    }

    @Test
    public void mockContentNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNode("test", op1);
        verify(op1, times(1)).of(node);
        assertThat(node.getPrimaryNodeType().getName(), is(NT_CONTENT));
    }

    @Test
    public void mockComponentNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockComponentNode("test", op1);
        verify(op1, times(1)).of(node);
        assertThat(node.getPrimaryNodeType().getName(), is(NT_COMPONENT));
    }

    @Test
    public void mockMgnlNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockMgnlNode("test-repository", "test/node", "test:nodeType", op1);
        verify(op1, times(1)).of(node);
        assertThat(node.getPrimaryNodeType().getName(), is("test:nodeType"));
        // Verify that WebContext and Session have been mocked...
        assertThat(MgnlContext.getWebContext(), notNullValue());
        assertThat(MgnlContext.getJCRSession("test-repository"), is(node.getSession()));
        //... and that the Node has been added to the session:
        assertThat(MgnlContext.getJCRSession("test-repository").getNode("/test/node"), is(node));
    }
}
