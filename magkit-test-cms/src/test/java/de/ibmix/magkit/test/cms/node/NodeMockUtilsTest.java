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

import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
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
 * @author frank.sommer
 * @since 31.10.12
 */
public class NodeMockUtilsTest {

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
//        assertThat(node.getProperties().hasNext(), is(false));
        verify(op1, times(1)).of(node);
        verify(op2, times(1)).of(node);

        ItemVisitor visitor = mock(ItemVisitor.class);
        node.accept(visitor);
        verify(visitor, times(1)).visit(node);
    }
}
