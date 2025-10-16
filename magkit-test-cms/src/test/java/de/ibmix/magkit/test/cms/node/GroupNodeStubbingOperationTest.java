package de.ibmix.magkit.test.cms.node;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
 * %%
 * Copyright (C) 2025 IBM iX
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
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Set;

import static de.ibmix.magkit.test.cms.node.GroupNodeStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.node.GroupNodeStubbingOperation.stubGroups;
import static de.ibmix.magkit.test.cms.node.GroupNodeStubbingOperation.stubRoles;
import static de.ibmix.magkit.test.cms.node.GroupNodeStubbingOperation.stubTitle;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockGroupNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockRoleNode;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GroupNodeStubbingOperation} covering title/description and reference list (groups/roles) stubbing.
 * Verifies creation, empty lists, ordering and overwrite behavior.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-13
 */
public class GroupNodeStubbingOperationTest {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubTitleAndDescription() throws RepositoryException {
        Node group = mockGroupNode("editors", stubTitle("Editors"), stubDescription("Editorial users"));
        assertEquals(NodeTypes.Group.NAME, group.getPrimaryNodeType().getName());
        assertTrue(group.hasProperty("title"));
        assertEquals("Editors", group.getProperty("title").getString());
        assertTrue(group.hasProperty("description"));
        assertEquals("Editorial users", group.getProperty("description").getString());
    }

    @Test
    public void stubGroupsListCreation() throws RepositoryException {
        Node parent = mockGroupNode("parent");
        Node child1 = mockGroupNode("childA");
        Node child2 = mockGroupNode("childB");
        stubGroups(child1, child2).of(parent);
        Node groups = parent.getNode("groups");
        assertNotNull(groups);
        assertEquals(NodeTypes.ContentNode.NAME, groups.getPrimaryNodeType().getName());
        assertTrue(groups.hasProperty("00"));
        assertEquals(child1.getIdentifier(), groups.getProperty("00").getString());
        assertTrue(groups.hasProperty("01"));
        assertEquals(child2.getIdentifier(), groups.getProperty("01").getString());
    }

    @Test
    public void stubGroupsEmptyListCreatesContainerWithoutProperties() throws RepositoryException {
        Node parent = mockGroupNode("parent-empty");
        stubGroups().of(parent);
        Node groups = parent.getNode("groups");
        assertNotNull(groups);
        PropertyIterator it = groups.getProperties();
        // collect only real numeric list properties (exclude jcr:primaryType etc.)
        Set<String> listPropNames = new HashSet<>();
        while (it.hasNext()) {
            String name = it.nextProperty().getName();
            if (name.matches("0\\d")) {
                listPropNames.add(name);
            }
        }
        assertTrue(listPropNames.isEmpty());
    }

    @Test
    public void stubGroupsOverwriteRemovesOldEntries() throws RepositoryException {
        Node parent = mockGroupNode("parent-overwrite");
        Node child1 = mockGroupNode("childA2");
        Node child2 = mockGroupNode("childB2");
        Node child3 = mockGroupNode("childC2");
        stubGroups(child1, child2, child3).of(parent);
        // overwrite with single entry
        stubGroups(child2).of(parent);
        Node groups = parent.getNode("groups");
        assertTrue(groups.hasProperty("00"));
        assertEquals(child2.getIdentifier(), groups.getProperty("00").getString());
        assertTrue(!groups.hasProperty("01") || !child3.getIdentifier().equals(groups.getProperty("01").getString()),
            "Old property 01 should not reference childC anymore");
    }

    @Test
    public void stubRolesListCreationAndEmptyOverwrite() throws RepositoryException {
        Node parent = mockGroupNode("parent-roles");
        Node role1 = mockRoleNode("roleA");
        Node role2 = mockRoleNode("roleB");
        stubRoles(role1, role2).of(parent);
        Node roles = parent.getNode("roles");
        assertEquals(role1.getIdentifier(), roles.getProperty("00").getString());
        assertEquals(role2.getIdentifier(), roles.getProperty("01").getString());
        // overwrite with empty list
        stubRoles().of(parent);
        roles = parent.getNode("roles");
        assertFalse(roles.hasProperty("00"));
        assertFalse(roles.hasProperty("01"));
    }
}
