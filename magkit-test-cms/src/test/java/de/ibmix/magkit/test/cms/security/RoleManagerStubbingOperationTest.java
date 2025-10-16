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

import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.auth.ACL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test RoleManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-03
 */
public class RoleManagerStubbingOperationTest {

    private RoleManager _roleManager;

    @BeforeEach
    public void setUp() throws Exception {
        _roleManager = mock(RoleManager.class);
    }

    @Test
    public void stubRole() {
        assertNull(_roleManager.getRole("test"));

        Role role = mock(Role.class);
        doReturn("test").when(role).getName();
        RoleManagerStubbingOperation.stubRole(role).of(_roleManager);
        assertEquals(role, _roleManager.getRole("test"));
        assertNull(_roleManager.getRoleNameById("test-td"));

        doReturn("test-id").when(role).getId();
        RoleManagerStubbingOperation.stubRole(role).of(_roleManager);
        assertEquals(role, _roleManager.getRole("test"));
        assertEquals("test", _roleManager.getRoleNameById("test-id"));
    }

    @Test
    public void stubRoleNameById() {
        assertNull(_roleManager.getRoleNameById("test-td"));

        RoleManagerStubbingOperation.stubRoleNameById("test-id", "test").of(_roleManager);
        assertEquals("test", _roleManager.getRoleNameById("test-id"));
    }

    @Test
    public void stubAcl() {
        assertTrue(_roleManager.getACLs("test").isEmpty());

        ACL acl = mock(ACL.class);
        doReturn("test-acl").when(acl).getName();
        RoleManagerStubbingOperation.stubAcl("test", acl).of(_roleManager);
        assertEquals(1, _roleManager.getACLs("test").size());
        assertEquals(acl, _roleManager.getACLs("test").get("test-acl"));

        ACL acl2 = mock(ACL.class);
        doReturn("test-acl2").when(acl2).getName();
        RoleManagerStubbingOperation.stubAcl("test", acl2).of(_roleManager);
        assertEquals(2, _roleManager.getACLs("test").size());
    }
}
