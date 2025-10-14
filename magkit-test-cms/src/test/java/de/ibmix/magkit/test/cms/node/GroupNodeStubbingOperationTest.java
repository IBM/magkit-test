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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link GroupNodeStubbingOperation} covering title/description and reference list (groups/roles) stubbing.
 * Verifies creation, empty lists, ordering and overwrite behavior.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-13
 */
public class GroupNodeStubbingOperationTest {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubTitleAndDescription() throws RepositoryException {
        Node group = mockGroupNode("editors", stubTitle("Editors"), stubDescription("Editorial users"));
        assertThat(group.getPrimaryNodeType().getName(), is(NodeTypes.Group.NAME));
        assertThat(group.hasProperty("title"), is(true));
        assertThat(group.getProperty("title").getString(), is("Editors"));
        assertThat(group.hasProperty("description"), is(true));
        assertThat(group.getProperty("description").getString(), is("Editorial users"));
    }

    @Test
    public void stubGroupsListCreation() throws RepositoryException {
        Node parent = mockGroupNode("parent");
        Node child1 = mockGroupNode("childA");
        Node child2 = mockGroupNode("childB");
        stubGroups(child1, child2).of(parent);
        Node groups = parent.getNode("groups");
        assertThat(groups, notNullValue());
        assertThat(groups.getPrimaryNodeType().getName(), is(NodeTypes.ContentNode.NAME));
        assertThat(groups.hasProperty("00"), is(true));
        assertThat(groups.getProperty("00").getString(), is(child1.getIdentifier()));
        assertThat(groups.hasProperty("01"), is(true));
        assertThat(groups.getProperty("01").getString(), is(child2.getIdentifier()));
    }

    @Test
    public void stubGroupsEmptyListCreatesContainerWithoutProperties() throws RepositoryException {
        Node parent = mockGroupNode("parent-empty");
        stubGroups().of(parent);
        Node groups = parent.getNode("groups");
        assertThat(groups, notNullValue());
        PropertyIterator it = groups.getProperties();
        // collect only real numeric list properties (exclude jcr:primaryType etc.)
        Set<String> listPropNames = new HashSet<>();
        while (it.hasNext()) {
            String name = it.nextProperty().getName();
            if (name.matches("0\\d")) {
                listPropNames.add(name);
            }
        }
        assertThat(listPropNames.isEmpty(), is(true));
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
        assertThat(groups.hasProperty("00"), is(true));
        assertThat(groups.getProperty("00").getString(), is(child2.getIdentifier()));
        // previous higher index properties should no longer match childC identifier
        assertThat("Old property 01 should not reference childC anymore", !groups.hasProperty("01") || !child3.getIdentifier().equals(groups.getProperty("01").getString()));
    }

    @Test
    public void stubRolesListCreationAndEmptyOverwrite() throws RepositoryException {
        Node parent = mockGroupNode("parent-roles");
        Node role1 = mockRoleNode("roleA");
        Node role2 = mockRoleNode("roleB");
        stubRoles(role1, role2).of(parent);
        Node roles = parent.getNode("roles");
        assertThat(roles.getProperty("00").getString(), is(role1.getIdentifier()));
        assertThat(roles.getProperty("01").getString(), is(role2.getIdentifier()));
        // overwrite with empty list
        stubRoles().of(parent);
        roles = parent.getNode("roles");
        assertThat(roles.hasProperty("00"), is(false));
        assertThat(roles.hasProperty("01"), is(false));
    }
}

