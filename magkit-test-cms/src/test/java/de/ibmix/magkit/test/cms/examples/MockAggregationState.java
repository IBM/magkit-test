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
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCurrentContentNode;
import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubMainContentNode;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockComponentNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia Test-Utils with this API (Magkit-Mock-Utils).
 *
 * @author wolf.bubenik
 * @since 14.04.16.
 */
public class MockAggregationState {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockAggregationStateWithMagkit() throws RepositoryException {
        // Ein AggregationState wird mit den ContextMockUtils erstellt:
        AggregationState state = mockAggregationState(
            stubMainContentNode(mockPageNode("page")),
            stubCurrentContentNode(mockComponentNode("page/component"))
        );

        // Wir erhalten dann einen WebContext...
        assertThat(MgnlContext.getInstance(), notNullValue());
        // ...mit dem AggregationState Mock..
        assertThat(((WebContext) MgnlContext.getInstance()).getAggregationState(), is(state));
        // ...und den gesetzten Nodes:
        assertThat(state.getCurrentContentNode().getPath(), is("/page/component"));
        assertThat(state.getMainContentNode().getPath(), is("/page"));
        // und mit dazu passenden Handle und Repository:
        assertThat(state.getHandle(), is("/page/component"));
        assertThat(state.getRepository(), is(RepositoryConstants.WEBSITE));
    }

    @Test
    public void mockAggregationStateWithMagnolia() throws RepositoryException, IOException {
        // Ein AggregationState erhält man immer, wenn man einen MockContext erzeugt:
        AggregationState state = ((WebContext) MockUtil.getMockContext(true)).getAggregationState();
        state.setMainContentNode(NodeTestUtil.createNode("/page", RepositoryConstants.WEBSITE, "/page"));
        state.setCurrentContentNode(NodeTestUtil.createNode("/page/component", RepositoryConstants.WEBSITE, "/page/component"));

        // Wir haben dann naturlich einen WebContext...
        assertThat(MgnlContext.getInstance(), notNullValue());
        // ...mit dem AggregationState Mock...
        assertThat(((WebContext) MgnlContext.getInstance()).getAggregationState(), is(state));
        // und den gesetzten Nodes:
        assertThat(state.getCurrentContentNode().getPath(), is("/page/component"));
        assertThat(state.getMainContentNode().getPath(), is("/page"));
        // Nur das handle ist nicht gesetzt...
        assertThat(state.getHandle(), nullValue());
        // ..und das Repository auch nicht:
        assertThat(state.getRepository(), nullValue());
    }
}
