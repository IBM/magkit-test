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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static info.magnolia.repository.RepositoryConstants.USERS;
import static info.magnolia.repository.RepositoryConstants.USER_GROUPS;
import static info.magnolia.repository.RepositoryConstants.USER_ROLES;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;

/**
 * Factory helper for creating Mockito based {@link Node} mocks preconfigured with Magnolia specific primary types
 * (mgnl:page, mgnl:component, mgnl:area, mgnl:content, mgnl:contentNode, mgnl:user, mgnl:group, mgnl:role).
 * <p>
 * Each factory method will:
 * <ul>
 *   <li>Ensure a mocked WebContext providing a JCR session for the requested workspace via {@code MgnlContext.getJcrSession(workspace)} by invoking {@link ContextMockUtils#mockWebContext(WebContextStubbingOperation...)}.</li>
 *   <li>Create (or reuse if already created earlier in the test) a {@link Node} mock for the given path / name using {@link de.ibmix.magkit.test.jcr.NodeMockUtils#mockNode(String, String, NodeStubbingOperation...)}.</li>
 *   <li>Stub the primary node type to the Magnolia type given by the method using {@link NodeStubbingOperation#stubType(String)}.</li>
 *   <li>Apply all provided {@link NodeStubbingOperation} instances in the order supplied.</li>
 * </ul>
 * </p>
 * <p>
 * The String parameters named {@code path} or {@code name} represent a JCR handle (absolute path) when starting with "/" or a simple node name otherwise. For convenience both are forwarded
 * unchanged to {@link de.ibmix.magkit.test.jcr.NodeMockUtils#mockNode(String, String, NodeStubbingOperation...)} which will normalize and create intermediate nodes as required.
 * </p>
 * <p>Typical usage examples:</p>
 * <pre>
 *   Node page = MagnoliaNodeMockUtils.mockPageNode("/site/en/home", NodeStubbingOperation.stubProperty("title", "Home"));
 *   Node component = MagnoliaNodeMockUtils.mockComponentNode("teaser", NodeStubbingOperation.stubProperty("enabled", true));
 *   Node user = MagnoliaNodeMockUtils.mockUserNode("john", NodeStubbingOperation.stubProperty("fullName", "John Doe"));
 * </pre>
 * <p><b>Thread safety:</b> Implementation is backed by ComponentProvider that uses ThreadLocal and is thread-safe; intended for multithreaded test initialization code.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-08-26
 */
public final class MagnoliaNodeMockUtils {

    /**
     * Create a Magnolia {@code mgnl:contentNode} in the default {@code website} workspace.
     *
     * @param path absolute path or simple node name for the node to mock
     * @param nodeStubbings optional stubbing operations applied to the created node (may be {@code null} or empty)
     * @return mocked node with primary type {@code mgnl:contentNode}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockContentNodeNode(String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, path, NodeTypes.ContentNode.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:contentNode} in the given workspace.
     *
     * @param repository target workspace name (e.g. {@code website}, {@code users})
     * @param path absolute path or simple node name for the node to mock
     * @param nodeStubbings optional stubbing operations applied to the created node
     * @return mocked node with primary type {@code mgnl:contentNode}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockContentNodeNode(String repository, String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(repository, path, NodeTypes.ContentNode.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:content} node (folder like) in the default {@code website} workspace.
     *
     * @param path absolute path or simple node name
     * @param nodeStubbings optional stubbing operations
     * @return mocked node with primary type {@code mgnl:content}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockContentNode(String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, path, NodeTypes.Content.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:content} node (folder like) in the given workspace.
     *
     * @param repository target workspace name
     * @param path absolute path or simple node name
     * @param nodeStubbings optional stubbing operations
     * @return mocked node with primary type {@code mgnl:content}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockContentNode(String repository, String path, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(repository, path, NodeTypes.Content.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:page} node with the default untitled name in the {@code website} workspace.
     *
     * @param nodeStubbings optional page specific stubbing operations (e.g. template, title)
     * @return mocked node with primary type {@code mgnl:page}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockPageNode(PageNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, NodeStubbingOperation.UNTITLED, NodeTypes.Page.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:page} node with the given name/path in the {@code website} workspace.
     *
     * @param name node name or absolute path
     * @param nodeStubbings optional page specific stubbing operations
     * @return mocked node with primary type {@code mgnl:page}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockPageNode(String name, PageNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Page.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:area} node in the {@code website} workspace.
     *
     * @param name node name or absolute path
     * @param nodeStubbings optional stubbing operations applied to the area node
     * @return mocked node with primary type {@code mgnl:area}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockAreaNode(String name, AreaNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Area.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:component} node in the {@code website} workspace.
     *
     * @param name node name or absolute path
     * @param nodeStubbings optional stubbing operations applied to the component node
     * @return mocked node with primary type {@code mgnl:component}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockComponentNode(String name, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(WEBSITE, name, NodeTypes.Component.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:user} node in the {@code users} workspace.
     *
     * @param name user node name or absolute path
     * @param nodeStubbings optional stubbing operations (e.g. roles, password)
     * @return mocked node with primary type {@code mgnl:user}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockUserNode(String name, UserNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(USERS, name, NodeTypes.User.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:group} node in the {@code usergroups} workspace.
     *
     * @param name group node name or absolute path
     * @param nodeStubbings optional stubbing operations (e.g. members)
     * @return mocked node with primary type {@code mgnl:group}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockGroupNode(String name, GroupNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(USER_GROUPS, name, NodeTypes.Group.NAME, nodeStubbings);
    }

    /**
     * Create a Magnolia {@code mgnl:role} node in the {@code userroles} workspace.
     *
     * @param name role node name or absolute path
     * @param nodeStubbings optional stubbing operations (e.g. ACL definitions)
     * @return mocked node with primary type {@code mgnl:role}
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockRoleNode(String name, RoleNodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockMgnlNode(USER_ROLES, name, NodeTypes.Role.NAME, nodeStubbings);
    }

    /**
     * Generic internal factory used by all specialized helpers. Creates (or retrieves) a node mock for the given path/name in the
     * specified repository and stubs its primary type to the provided Magnolia type.
     * <p>WebContext is mocked once per repository invocation enabling {@code MgnlContext.getJcrSession(repository)} calls.</p>
     *
     * @param repository workspace name
     * @param name node name or absolute path (handle). Intermediate nodes are created as needed.
     * @param nodeType Magnolia primary type name to stub (e.g. {@code mgnl:page})
     * @param nodeStubbings optional stubbing operations applied after type stubbing
     * @return mocked node with the specified Magnolia primary type
     * @throws RepositoryException if one of the stubbing operations throws an exception
     */
    public static Node mockMgnlNode(String repository, String name, String nodeType, NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        ContextMockUtils.mockWebContext(WebContextStubbingOperation.stubJcrSession(repository));
        Node mgnlNode = mockNode(repository, name, nodeStubbings);
        stubType(nodeType).of(mgnlNode);
        return mgnlNode;
    }

    /**
     * Hidden constructor: utility class should not be instantiated.
     */
    private MagnoliaNodeMockUtils() {
    }
}
