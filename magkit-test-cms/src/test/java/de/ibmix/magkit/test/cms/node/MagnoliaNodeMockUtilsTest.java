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
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.*;
import static info.magnolia.cms.core.MgnlNodeType.NT_AREA;
import static info.magnolia.cms.core.MgnlNodeType.NT_COMPONENT;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENT;
import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;
import static info.magnolia.cms.core.MgnlNodeType.NT_PAGE;
import static info.magnolia.repository.RepositoryConstants.USERS;
import static info.magnolia.repository.RepositoryConstants.USER_GROUPS;
import static info.magnolia.repository.RepositoryConstants.USER_ROLES;
import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockPageNodeTest() throws RepositoryException {
        PageNodeStubbingOperation op1 = Mockito.mock(PageNodeStubbingOperation.class);
        PageNodeStubbingOperation op2 = Mockito.mock(PageNodeStubbingOperation.class);
        Node node = mockPageNode(op1, op2);
        assertNotNull(node.getPrimaryNodeType());
        assertEquals(NT_PAGE, node.getPrimaryNodeType().getName());
        assertNotNull(node.getNodes());
        assertFalse(node.getNodes().hasNext());
        assertNotNull(node.getProperties());
        verify(op1, times(1)).of(node);
        verify(op2, times(1)).of(node);

        ItemVisitor visitor = mock(ItemVisitor.class);
        node.accept(visitor);
        verify(visitor, times(1)).visit(node);
    }

    @Test
    public void mockPageNodeWithNameTest() throws RepositoryException {
        PageNodeStubbingOperation op = Mockito.mock(PageNodeStubbingOperation.class);
        Node node = mockPageNode("/home", op);
        assertEquals(NT_PAGE, node.getPrimaryNodeType().getName());
        verify(op, times(1)).of(node);
    }

    @Test
    public void mockAreaNodeTest() throws RepositoryException {
        AreaNodeStubbingOperation op1 = Mockito.mock(AreaNodeStubbingOperation.class);
        Node node = mockAreaNode("test", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_AREA, node.getPrimaryNodeType().getName());
    }

    @Test
    public void mockContentNodeNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNodeNode("test", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_CONTENTNODE, node.getPrimaryNodeType().getName());
    }

    @Test
    public void mockContentNodeNodeWithRepositoryTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNodeNode("custom-repo", "/a/b", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_CONTENTNODE, node.getPrimaryNodeType().getName());
        assertEquals(node, MgnlContext.getJCRSession("custom-repo").getNode("/a/b"));
    }

    @Test
    public void mockContentNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNode("test", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_CONTENT, node.getPrimaryNodeType().getName());
    }

    @Test
    public void mockContentNodeWithRepositoryTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockContentNode("other-repo", "content/path", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_CONTENT, node.getPrimaryNodeType().getName());
        assertEquals(node, MgnlContext.getJCRSession("other-repo").getNode("/content/path"));
    }

    @Test
    public void mockComponentNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockComponentNode("test", op1);
        verify(op1, times(1)).of(node);
        assertEquals(NT_COMPONENT, node.getPrimaryNodeType().getName());
    }

    @Test
    public void mockUserGroupRoleNodesTest() throws RepositoryException {
        UserNodeStubbingOperation userOp1 = Mockito.mock(UserNodeStubbingOperation.class);
        Node user1 = mockUserNode("john", userOp1);
        assertEquals(NodeTypes.User.NAME, user1.getPrimaryNodeType().getName());
        verify(userOp1, times(1)).of(user1);
        UserNodeStubbingOperation userOp2 = Mockito.mock(UserNodeStubbingOperation.class);
        Node user2 = mockUserNode("john", userOp2);
        assertEquals(user1, user2);
        verify(userOp2, times(1)).of(user1);
        assertEquals(user1, MgnlContext.getJCRSession(USERS).getNode("/john"));

        GroupNodeStubbingOperation groupOp = Mockito.mock(GroupNodeStubbingOperation.class);
        Node group = mockGroupNode("editors", groupOp);
        assertEquals(NodeTypes.Group.NAME, group.getPrimaryNodeType().getName());
        verify(groupOp, times(1)).of(group);
        assertEquals(group, MgnlContext.getJCRSession(USER_GROUPS).getNode("/editors"));

        RoleNodeStubbingOperation roleOp = Mockito.mock(RoleNodeStubbingOperation.class);
        Node role = mockRoleNode("author-role", roleOp);
        assertEquals(NodeTypes.Role.NAME, role.getPrimaryNodeType().getName());
        verify(roleOp, times(1)).of(role);
        assertEquals(role, MgnlContext.getJCRSession(USER_ROLES).getNode("/author-role"));
    }

    @Test
    public void mockMgnlNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = Mockito.mock(NodeStubbingOperation.class);
        Node node = mockMgnlNode("test-repository", "test/node", "test:nodeType", op1);
        verify(op1, times(1)).of(node);
        assertEquals("test:nodeType", node.getPrimaryNodeType().getName());
        assertNotNull(MgnlContext.getWebContext());
        assertEquals(node.getSession(), MgnlContext.getJCRSession("test-repository"));
        assertEquals(node, MgnlContext.getJCRSession("test-repository").getNode("/test/node"));
    }
}
