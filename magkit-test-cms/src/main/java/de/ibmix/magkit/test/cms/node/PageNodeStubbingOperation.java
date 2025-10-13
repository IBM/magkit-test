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

import de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation;
import de.ibmix.magkit.test.cms.templating.TemplateMockUtils;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Factory helpers for stubbing Magnolia page node specific properties, in particular the template assignment.
 * <p>A page node usually requires a Magnolia template id (mgnl:template) to drive rendering and component availability.
 * This utility offers a concise factory to mock a {@code TemplateDefinition} and write its id onto the target node.</p>
 * <p><strong>Behavior:</strong></p>
 * <ul>
 *   <li>No validation of the provided {@code templateId}; blank or null values are written verbatim.</li>
 *   <li>Fails fast (Hamcrest assertion) if the target node passed to the stubbing operation is null.</li>
 *   <li>Mocks the template definition first so subsequent test logic can query both the node property and the mocked definition.</li>
 * </ul>
 * <p>Example usage:</p>
 * <pre>
 *   Node page = MagnoliaNodeMockUtils.mockPageNode(
 *       PageNodeStubbingOperation.stubTemplate("my-module:homepage")
 *   );
 * </pre>
 * <p>Thread-safety: produced operations are stateless.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-10
 */
public abstract class PageNodeStubbingOperation extends MagnoliaNodeStubbingOperation {

    /**
     * Create a stubbing operation that mocks a template definition for the given template id and writes the Magnolia
     * template property ({@link NodeTypes.Renderable#TEMPLATE}) onto the page node.
     * <p>
     * Provided {@code stubbings} are applied to the mocked template prior to setting the property.
     * </p>
     * <p>Example:</p>
     * <pre>
     *   PageNodeStubbingOperation.stubTemplate("my-module:article",
     *       TemplateDefinitionStubbingOperation.stubTitle("Article Page")
     *   );
     * </pre>
     *
     * @param templateId Magnolia template id (e.g. {@code my-module:article}); may be null or blank
     * @param stubbings optional template definition stubbing operations; may be empty
     * @return stubbing operation applying the template property on execution
     */
    public static MagnoliaNodeStubbingOperation stubTemplate(final String templateId, final TemplateDefinitionStubbingOperation... stubbings) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                TemplateMockUtils.mockTemplateDefinition(templateId, stubbings);
                stubProperty(NodeTypes.Renderable.TEMPLATE, templateId).of(node);
            }
        };
    }
}
