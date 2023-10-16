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
import de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation;
import de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation;
import de.ibmix.magkit.test.cms.templating.TemplateMockUtils;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Utility class that provides NodeStubbingOperations to stub magnolia related node properties.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-06-03
 */
public abstract class MagnoliaNodeStubbingOperation extends NodeStubbingOperation {

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

    public static MagnoliaNodeStubbingOperation stubAreaTemplate(final String templateId, final AreaDefinitionStubbingOperation... stubbings) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                TemplateMockUtils.mockAreaDefinition(templateId, stubbings);
                stubProperty(NodeTypes.Renderable.TEMPLATE, templateId).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubCreated(final Calendar creationDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Created.CREATED, creationDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubCreatedBy(final String createdBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Created.CREATED_BY, createdBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastModified(final Calendar lastModifiedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED, lastModifiedDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastModifiedBy(final String modifiedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED_BY, modifiedBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastActivated(final Calendar lastActivatedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED, lastActivatedDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastActivatedBy(final String activatedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED_BY, activatedBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubActivationStatus(final boolean activated) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.ACTIVATION_STATUS, activated).of(node);
            }
        };
    }
}
