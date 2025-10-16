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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation;
import de.ibmix.magkit.test.cms.templating.TemplateMockUtils;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Factory collection for Magnolia area node specific stubbing operations.
 * <p>
 * Provides a concise helper to associate a mocked {@code AreaDefinition} (via {@link TemplateMockUtils#mockAreaDefinition(String, AreaDefinitionStubbingOperation...)})
 * with a node and to stub the Magnolia template property ({@code mgnl:template}). This is typically used when building page hierarchies
 * containing area nodes that must expose a template id for rendering or component resolution logic.
 * </p>
 * <p><strong>Behavior:</strong>
 * <ul>
 *   <li>Does not validate {@code templateId}; a {@code null} or blank id will be passed through to the underlying mock factory and written verbatim.</li>
 *   <li>Fails fast with a Hamcrest assertion if the target node is {@code null}.</li>
 *   <li>Stubs the property after mocking the area definition so subsequent test logic can retrieve both aspects.</li>
 * </ul>
 * </p>
 * <p>Example usage:</p>
 * <pre>
 *   Node area = MagnoliaNodeMockUtils.mockAreaNode("main",
 *       AreaNodeStubbingOperation.stubAreaTemplate("my-module:main",
 *           AreaDefinitionStubbingOperation.stubTitle("Main Area"))
 *   );
 * </pre>
 * <p>
 * Thread-safety: operations are stateless; each test should create its own mock graph.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-10
 */
public abstract class AreaNodeStubbingOperation extends MagnoliaNodeStubbingOperation {

    /**
     * Create a stubbing operation that mocks an area definition for the given template id and writes the Magnolia template property
     * ({@link NodeTypes.Renderable#TEMPLATE}) onto the target node.
     * <p>
     * The provided {@code stubbings} are applied to the mocked area definition before the template property is set on the node.
     * </p>
     * <p>Example:</p>
     * <pre>
     *   AreaNodeStubbingOperation.stubAreaTemplate("my-module:sidebar",
     *       AreaDefinitionStubbingOperation.stubTitle("Sidebar"),
     *       AreaDefinitionStubbingOperation.stubDialog("sidebarDialog")
     *   );
     * </pre>
     *
     * @param templateId Magnolia template id (e.g. {@code my-module:main}); may be null or blank
     * @param stubbings optional stubbing operations applied to the mocked area definition (may be empty)
     * @return stubbing operation to be invoked via {@link MagnoliaNodeStubbingOperation#of(Node)}
     */
    public static MagnoliaNodeStubbingOperation stubAreaTemplate(final String templateId, final AreaDefinitionStubbingOperation... stubbings) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node should not be null");
                TemplateMockUtils.mockAreaDefinition(templateId, stubbings);
                stubProperty(NodeTypes.Renderable.TEMPLATE, templateId).of(node);
            }
        };
    }
}
