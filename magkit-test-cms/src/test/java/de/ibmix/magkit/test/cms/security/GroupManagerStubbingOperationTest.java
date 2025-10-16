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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @BeforeEach
    public void setUp() throws Exception {
        _groupManager = mock(GroupManager.class);
    }

    @Test
    public void stubGroup() throws AccessDeniedException {
        assertNull(_groupManager.getGroup("test"));
        assertEquals(0, _groupManager.getAllGroups().size());

        Group group = mock(Group.class);
        doReturn("test").when(group).getName();
        GroupManagerStubbingOperation.stubGroup(group).of(_groupManager);
        assertEquals(group, _groupManager.getGroup("test"));
        assertEquals(1, _groupManager.getAllGroups().size());

        doReturn("test-id").when(group).getId();
        GroupManagerStubbingOperation.stubGroup(group).of(_groupManager);
        assertEquals(group, _groupManager.getGroup("test"));
        // do not add group twice
        assertEquals(1, _groupManager.getAllGroups().size());
    }

    @Test
    public void stubAllGroups() throws AccessDeniedException {
        assertEquals(0, _groupManager.getAllGroups().size());

        Group g1  = mock(Group.class);
        doReturn("g1").when(g1).getName();
        Group g2  = mock(Group.class);
        doReturn("g2").when(g2).getName();
        GroupManagerStubbingOperation.stubAllGroups(g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getAllGroups().size());
        assertEquals(g1, _groupManager.getGroup("g1"));
        assertEquals(g2, _groupManager.getGroup("g2"));
    }

    @Test
    public void stubAllSuperGroups() {
        assertEquals(0, _groupManager.getAllSuperGroups("group").size());

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubAllSuperGroups("group", g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getAllSuperGroups("group").size());
    }

    @Test
    public void stubAllSubGroups() {
        assertEquals(0, _groupManager.getAllSubGroups("group").size());

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubAllSubGroups("group", g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getAllSubGroups("group").size());
    }

    @Test
    public void stubDirectSubGroups() {
        assertEquals(0, _groupManager.getDirectSubGroups("group").size());

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubDirectSubGroups("group", g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getDirectSubGroups("group").size());
    }

    @Test
    public void stubDirectSuperGroups() {
        assertEquals(0, _groupManager.getDirectSuperGroups("group").size());

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubDirectSuperGroups("group", g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getDirectSuperGroups("group").size());
    }

    @Test
    public void stubGroupsWithRole() {
        assertEquals(0, _groupManager.getGroupsWithRole("role").size());

        Group g1  = mock(Group.class);
        Group g2  = mock(Group.class);
        GroupManagerStubbingOperation.stubGroupsWithRole("role", g1, g2).of(_groupManager);
        assertEquals(2, _groupManager.getGroupsWithRole("role").size());
    }

    @Test
    public void stubAcl() {
        assertTrue(_groupManager.getACLs("test").isEmpty());

        ACL acl = mock(ACL.class);
        doReturn("test-acl").when(acl).getName();
        GroupManagerStubbingOperation.stubAcl("test", acl).of(_groupManager);
        assertEquals(1, _groupManager.getACLs("test").size());
        assertEquals(acl, _groupManager.getACLs("test").get("test-acl"));

        ACL acl2 = mock(ACL.class);
        doReturn("test-acl2").when(acl2).getName();
        GroupManagerStubbingOperation.stubAcl("test", acl2).of(_groupManager);
        assertEquals(2, _groupManager.getACLs("test").size());
    }
}
