package de.ibmix.magkit.test.cms.examples;

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
import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.jcr.NodeTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCurrentContentNode;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubMainContentNode;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Compare Magnolia Test-Utils with this API (Magkit-Mock-Utils).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-04-14
 */
public class MockAggregationState {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    /**
     * This test demonstrates the mocking on an AggregationState using the Magkit MockUtils.
     * @throws RepositoryException when the stubbed methods do.
     */
    @Test
    public void mockAggregationStateWithMagkit() throws RepositoryException {
        // An AggregationState mock is created with ContextMockUtils:
        AggregationState state = mockAggregationState(
            stubMainContentNode(mockPageNode("page")),
            stubCurrentContentNode(mockComponentNode("page/component"))
        );

        // Now we have a WebContext in the MgnlContext...
        assertNotNull(MgnlContext.getInstance());
        // ...with the AggregationState mock..
        assertSame(state, ((WebContext) MgnlContext.getInstance()).getAggregationState());
        // ...and the stubbed Nodes:
        assertEquals("/page/component", state.getCurrentContentNode().getPath());
        assertEquals("/page", state.getMainContentNode().getPath());
        // Note that current/active nodes and AggregationState properties are consistent:
        assertEquals("/page/component", state.getHandle());
        assertEquals(RepositoryConstants.WEBSITE, state.getRepository());
    }

    /**
     * This test demonstrates the mocking on an AggregationState using the Magnolia MockUtil.
     *
     * @throws RepositoryException as we do
     * @throws IOException why ever
     */
    @Test
    public void mockAggregationStateWithMagnolia() throws RepositoryException, IOException {
        // Creating a MockContext implicitly creates an AggregationState:
        AggregationState state = ((WebContext) MockUtil.getMockContext(true)).getAggregationState();
        state.setMainContentNode(NodeTestUtil.createNode("/page", RepositoryConstants.WEBSITE, "/page"));
        state.setCurrentContentNode(NodeTestUtil.createNode("/page/component", RepositoryConstants.WEBSITE, "/page/component"));

        // Of course we have a WebContext...
        assertNotNull(MgnlContext.getInstance());
        // ...with the AggregationState mock...
        assertSame(state, ((WebContext) MgnlContext.getInstance()).getAggregationState());
        // and the nodes set:
        assertEquals("/page/component", state.getCurrentContentNode().getPath());
        assertEquals("/page", state.getMainContentNode().getPath());
        // Only the handle is not set...
        assertNull(state.getHandle());
        // .. and the Repository either:
        assertNull(state.getRepository());
    }
}
