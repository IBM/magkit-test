package de.ibmix.magkit.test.cms.security;

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

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.auth.ACL;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test GroupManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-03
 */
public class GroupManagerStubbingOperationTest {


    private GroupManager _groupManager;

    @Before
    public void setUp() throws Exception {
        _groupManager = mock(GroupManager.class);
    }

    @Test
    public void stubGroup() throws AccessDeniedException {
        assertThat(_groupManager.getGroup("test"), nullValue());
        assertThat(_groupManager.getAllGroups().size(), is(0));

        Group group = mock(Group.class);
        doReturn("test").when(group).getName();
        GroupManagerStubbingOperation.stubGroup(group).of(_groupManager);
        assertThat(_groupManager.getGroup("test"), is(group));
        assertThat(_groupManager.getAllGroups().size(), is(1));

        doReturn("test-id").when(group).getId();
        GroupManagerStubbingOperation.stubGroup(group).of(_groupManager);
        assertThat(_groupManager.getGroup("test"), is(group));
        // do not add group twice
        assertThat(_groupManager.getAllGroups().size(), is(1));
    }

    @Test
    public void stubAllGroups() throws AccessDeniedException {
        assertThat(_groupManager.getAllGroups().size(), is(0));

        Group g1  = mock(Group.class);
        doReturn("g1").when(g1).getName();
        Group g2  = mock(Group.class);
        doReturn("g2").when(g2).getName();
        GroupManagerStubbingOperation.stubAllGroups(g1, g2).of(_groupManager);
        assertThat(_groupManager.getAllGroups().size(), is(2));
        assertThat(_groupManager.getGroup("g1"), is(g1));
        assertThat(_groupManager.getGroup("g2"), is(g2));
    }

    @Test
    public void stubAllSuperGroups() {
        assertThat(_groupManager.getAllSuperGroups("group").size(), is(0));

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubAllSuperGroups("group", g1, g2).of(_groupManager);
        assertThat(_groupManager.getAllSuperGroups("group").size(), is(2));
    }

    @Test
    public void stubAllSubGroups() {
        assertThat(_groupManager.getAllSubGroups("group").size(), is(0));

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubAllSubGroups("group", g1, g2).of(_groupManager);
        assertThat(_groupManager.getAllSubGroups("group").size(), is(2));
    }

    @Test
    public void stubDirectSubGroups() {
        assertThat(_groupManager.getDirectSubGroups("group").size(), is(0));

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubDirectSubGroups("group", g1, g2).of(_groupManager);
        assertThat(_groupManager.getDirectSubGroups("group").size(), is(2));
    }

    @Test
    public void stubDirectSuperGroups() {
        assertThat(_groupManager.getDirectSuperGroups("group").size(), is(0));

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubDirectSuperGroups("group", g1, g2).of(_groupManager);
        assertThat(_groupManager.getDirectSuperGroups("group").size(), is(2));
    }

    @Test
    public void stubGroupsWithRole() {
        assertThat(_groupManager.getGroupsWithRole("role").size(), is(0));

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubGroupsWithRole("role", g1, g2).of(_groupManager);
        assertThat(_groupManager.getGroupsWithRole("role").size(), is(2));
    }

    @Test
    public void stubAcl() {
        assertThat(_groupManager.getACLs("test").isEmpty(), is(true));

        ACL acl = mock(ACL.class);
        doReturn("test-acl").when(acl).getName();
        GroupManagerStubbingOperation.stubAcl("test", acl).of(_groupManager);
        assertThat(_groupManager.getACLs("test").size(), is(1));
        assertThat(_groupManager.getACLs("test").get("test-acl"), is(acl));

        ACL acl2 = mock(ACL.class);
        doReturn("test-acl2").when(acl2).getName();
        GroupManagerStubbingOperation.stubAcl("test", acl2).of(_groupManager);
        assertThat(_groupManager.getACLs("test").size(), is(2));
    }
}
