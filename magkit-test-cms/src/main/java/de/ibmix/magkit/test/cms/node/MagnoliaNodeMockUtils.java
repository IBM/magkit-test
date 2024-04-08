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
import de.ibmix.magkit.test.cms.context.WebContextStubbingOperation;
import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import info.magnolia.jcr.util.NodeTypes;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;

/**
 * Utility class for mocking jcr nodes with magnolia node types.
 * MgnlContext.getJcrSession("name") will be stubbed as well.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-08-26
 */
public final class MagnoliaNodeMockUtils {

    public static Node mockContentNodeNode(String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, path, NodeTypes.ContentNode.NAME, nodeStubbings);
    }

    public static Node mockContentNodeNode(String repository, String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(repository, path, NodeTypes.ContentNode.NAME, nodeStubbings);
    }

    public static Node mockContentNode(String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, path, NodeTypes.Content.NAME, nodeStubbings);
    }

    public static Node mockContentNode(String repository, String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(repository, path, NodeTypes.Content.NAME, nodeStubbings);
    }

    public static Node mockPageNode(NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, NodeStubbingOperation.UNTITLED, NodeTypes.Page.NAME, nodeStubbings);
    }

    /**
     * Mocks a page node with mgnl:page node type.
     *
     * @param name node name
     * @param nodeStubbings for stub behaviour
     * @return mocked node
     * @throws RepositoryException repository exception
     */
    public static Node mockPageNode(String name, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Page.NAME, nodeStubbings);
    }

    /**
     * Mocks a page node with mgnl area node type.
     *
     * @param name node name
     * @param nodeStubbings for stub behaviour
     * @return mocked node
     * @throws RepositoryException repository exception
     */
    public static Node mockAreaNode(String name, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Area.NAME, nodeStubbings);
    }

    /**
     * Mocks a page node with mgnl component node type.
     *
     * @param name node name
     * @param nodeStubbings for stub behaviour
     * @return mocked node
     * @throws RepositoryException repository exception
     */
    public static Node mockComponentNode(String name, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Component.NAME, nodeStubbings);
    }

    /**
     * Mocks a page node with mgnl component node type.
     *
     * @param repository workspace name
     * @param name node name
     * @param nodeType node type name
     * @param nodeStubbings for stub behaviour
     * @return mocked node
     * @throws RepositoryException repository exception
     */
    public static Node mockMgnlNode(String repository, String name, String nodeType, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        ContextMockUtils.mockWebContext(WebContextStubbingOperation.stubJcrSession(repository));
        return mockNode(repository, name, ArrayUtils.add(nodeStubbings, stubType(nodeType)));
    }

    private MagnoliaNodeMockUtils() {
    }
}
