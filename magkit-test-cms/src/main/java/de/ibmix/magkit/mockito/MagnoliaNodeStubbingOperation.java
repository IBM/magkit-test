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

import de.ibmix.magkit.mockito.jcr.NodeStubbingOperation;
import com.google.common.base.Preconditions;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

/**
 * Utility class that provides NodeStubbingOperations to stub magnolia related node properties.
 *
 * @author wolf.bubenik
 * @since 03.06.16.
 */
public abstract class MagnoliaNodeStubbingOperation extends NodeStubbingOperation {

    public static MagnoliaNodeStubbingOperation stubTemplate(final String templateId, final TemplateDefinitionStubbingOperation... stubbings) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                TemplateMockUtils.mockTemplateDefinition(templateId, stubbings);
                stubProperty(NodeTypes.Renderable.TEMPLATE, templateId).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubAreaTemplate(final String templateId, final AreaDefinitionStubbingOperation... stubbings) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                TemplateMockUtils.mockAreaDefinition(templateId, stubbings);
                stubProperty(NodeTypes.Renderable.TEMPLATE, templateId).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubCreated(final Calendar creationDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.Created.CREATED, creationDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubCreatedBy(final String createdBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.Created.CREATED_BY, createdBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastModified(final Calendar lastModifiedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED, lastModifiedDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastModifiedBy(final String modifiedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED_BY, modifiedBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastActivated(final Calendar lastActivatedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED, lastActivatedDate).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubLastActivatedBy(final String activatedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED_BY, activatedBy).of(node);
            }
        };
    }

    public static MagnoliaNodeStubbingOperation stubActivationStatus(final boolean activated) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Preconditions.checkArgument(node != null, "The Node must not be null.");
                stubProperty(NodeTypes.Activatable.ACTIVATION_STATUS, activated).of(node);
            }
        };
    }
}
