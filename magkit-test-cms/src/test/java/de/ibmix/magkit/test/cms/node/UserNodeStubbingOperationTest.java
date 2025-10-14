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
import java.util.Calendar;
import java.util.GregorianCalendar;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.*;
import static de.ibmix.magkit.test.cms.node.UserNodeStubbingOperation.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for {@link UserNodeStubbingOperation} covering all property stubs and reference list behaviors (groups/roles) including overwrites and empty lists.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-13
 */
public class UserNodeStubbingOperationTest {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @After
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
        assertThat(user.getPrimaryNodeType().getName(), is(NodeTypes.User.NAME));
        assertThat(user.getProperty("email").getString(), is("john@example.org"));
        assertThat(user.getProperty("enabled").getBoolean(), is(true));
        assertThat(user.getProperty("failedLoginAttempts").getLong(), is(5L));
        assertThat(user.getProperty("language").getString(), is("en"));
        assertThat(user.getProperty("lastAccess").getDate(), is(lastAccess));
        assertThat(user.getProperty("name").getString(), is("John Doe"));
        assertThat(user.getProperty("pswd").getString(), is("secret"));
        assertThat(user.getProperty("title").getString(), is("Editor"));
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
        assertThat(user.hasProperty("email"), is(true));
        assertThat(user.getProperty("email").getString(), nullValue());
        assertThat(user.getProperty("language").getString(), is("  "));
        assertThat(user.getProperty("title").getString(), nullValue());
        assertThat(user.getProperty("name").getString(), nullValue());
        assertThat(user.getProperty("lastAccess").getDate(), nullValue());
    }

    @Test
    public void stubGroupsListAndOverwrite() throws RepositoryException {
        Node user = mockUserNode("groupUser");
        Node g1 = mockGroupNode("groupA");
        Node g2 = mockGroupNode("groupB");
        Node g3 = mockGroupNode("groupC");
        stubGroups(g1, g2, g3).of(user);
        Node groups = user.getNode("groups");
        assertThat(groups.getPrimaryNodeType().getName(), is(NodeTypes.ContentNode.NAME));
        assertThat(groups.getProperty("00").getString(), is(g1.getIdentifier()));
        assertThat(groups.getProperty("01").getString(), is(g2.getIdentifier()));
        assertThat(groups.getProperty("02").getString(), is(g3.getIdentifier()));
        // overwrite with single
        stubGroups(g2).of(user);
        groups = user.getNode("groups");
        assertThat(groups.getProperty("00").getString(), is(g2.getIdentifier()));
        assertThat("Overwritten list should not still reference old first entry", groups.getProperty("00").getString(), not(g1.getIdentifier()));
        // ensure no stale higher index referencing g3
        assertThat("Old index 02 should be gone or changed", !groups.hasProperty("02") || !g3.getIdentifier().equals(groups.getProperty("02").getString()));
    }

    @Test
    public void stubGroupsEmptyListCreatesContainerWithoutEntries() throws RepositoryException {
        Node user = mockUserNode("emptyGroupUser");
        stubGroups().of(user);
        Node groups = user.getNode("groups");
        assertThat(groups, notNullValue());
        PropertyIterator it = groups.getProperties();
        int listProps = 0;
        while (it.hasNext()) {
            String n = it.nextProperty().getName();
            if (n.matches("0\\d")) {
                listProps++;
            }
        }
        assertThat(listProps, is(0));
    }

    @Test
    public void stubRolesAddAndClear() throws RepositoryException {
        Node user = mockUserNode("roleUser");
        // start empty
        stubRoles().of(user);
        Node roles = user.getNode("roles");
        assertThat(roles, notNullValue());
        // add roles
        Node r1 = mockRoleNode("roleA");
        Node r2 = mockRoleNode("roleB");
        stubRoles(r1, r2).of(user);
        roles = user.getNode("roles");
        assertThat(roles.getProperty("00").getString(), is(r1.getIdentifier()));
        assertThat(roles.getProperty("01").getString(), is(r2.getIdentifier()));
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
        assertThat(listProps, is(0));
    }
}

