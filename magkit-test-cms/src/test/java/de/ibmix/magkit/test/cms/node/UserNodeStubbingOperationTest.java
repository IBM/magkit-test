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
import java.util.Calendar;
import java.util.GregorianCalendar;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.*;
import static de.ibmix.magkit.test.cms.node.UserNodeStubbingOperation.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UserNodeStubbingOperation} covering all property stubs and reference list behaviors (groups/roles) including overwrites and empty lists.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-13
 */
public class UserNodeStubbingOperationTest {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubAllSimpleProperties() throws RepositoryException {
        Calendar lastAccess = new GregorianCalendar(2025, Calendar.JANUARY, 1, 12, 0, 0);
        Node user = mockUserNode("john",
            stubEmail("john@example.org"),
            stubEnabled(true),
            stubFailedLoginAttempts(5L),
            stubLanguage("en"),
            stubLastAccess(lastAccess),
            stubName("John Doe"),
            stubPassword("secret"),
            stubTitle("Editor")
        );
        assertEquals(NodeTypes.User.NAME, user.getPrimaryNodeType().getName());
        assertEquals("john@example.org", user.getProperty("email").getString());
        assertTrue(user.getProperty("enabled").getBoolean());
        assertEquals(5L, user.getProperty("failedLoginAttempts").getLong());
        assertEquals("en", user.getProperty("language").getString());
        assertEquals(lastAccess, user.getProperty("lastAccess").getDate());
        assertEquals("John Doe", user.getProperty("name").getString());
        assertEquals("secret", user.getProperty("pswd").getString());
        assertEquals("Editor", user.getProperty("title").getString());
    }

    @Test
    public void stubNullableAndBlankValues() throws RepositoryException {
        Node user = mockUserNode("mary",
            stubEmail(null),
            stubLanguage("  "),
            stubTitle(null),
            stubName(null),
            stubLastAccess(null)
        );
        assertTrue(user.hasProperty("email"));
        assertNull(user.getProperty("email").getString());
        assertEquals("  ", user.getProperty("language").getString());
        assertNull(user.getProperty("title").getString());
        assertNull(user.getProperty("name").getString());
        assertNull(user.getProperty("lastAccess").getDate());
    }

    @Test
    public void stubGroupsListAndOverwrite() throws RepositoryException {
        Node user = mockUserNode("groupUser");
        Node g1 = mockGroupNode("groupA");
        Node g2 = mockGroupNode("groupB");
        Node g3 = mockGroupNode("groupC");
        stubGroups(g1, g2, g3).of(user);
        Node groups = user.getNode("groups");
        assertEquals(NodeTypes.ContentNode.NAME, groups.getPrimaryNodeType().getName());
        assertEquals(g1.getIdentifier(), groups.getProperty("00").getString());
        assertEquals(g2.getIdentifier(), groups.getProperty("01").getString());
        assertEquals(g3.getIdentifier(), groups.getProperty("02").getString());
        // overwrite with single
        stubGroups(g2).of(user);
        groups = user.getNode("groups");
        assertEquals(g2.getIdentifier(), groups.getProperty("00").getString());
        assertNotEquals(g1.getIdentifier(), groups.getProperty("00").getString(), "Overwritten list should not still reference old first entry");
        // ensure no stale higher index referencing g3
        assertTrue(!groups.hasProperty("02") || !g3.getIdentifier().equals(groups.getProperty("02").getString()),
            "Old index 02 should be gone or changed");
    }

    @Test
    public void stubGroupsEmptyListCreatesContainerWithoutEntries() throws RepositoryException {
        Node user = mockUserNode("emptyGroupUser");
        stubGroups().of(user);
        Node groups = user.getNode("groups");
        assertNotNull(groups);
        PropertyIterator it = groups.getProperties();
        int listProps = 0;
        while (it.hasNext()) {
            String n = it.nextProperty().getName();
            if (n.matches("0\\d")) {
                listProps++;
            }
        }
        assertEquals(0, listProps);
    }

    @Test
    public void stubRolesAddAndClear() throws RepositoryException {
        Node user = mockUserNode("roleUser");
        // start empty
        stubRoles().of(user);
        Node roles = user.getNode("roles");
        assertNotNull(roles);
        // add roles
        Node r1 = mockRoleNode("roleA");
        Node r2 = mockRoleNode("roleB");
        stubRoles(r1, r2).of(user);
        roles = user.getNode("roles");
        assertEquals(r1.getIdentifier(), roles.getProperty("00").getString());
        assertEquals(r2.getIdentifier(), roles.getProperty("01").getString());
        // clear again
        stubRoles().of(user);
        roles = user.getNode("roles");
        PropertyIterator it = roles.getProperties();
        int listProps = 0;
        while (it.hasNext()) {
            String n = it.nextProperty().getName();
            if (n.matches("0\\d")) {
                listProps++;
            }
        }
        assertEquals(0, listProps);
    }
}

