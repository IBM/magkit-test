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
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Before
    public void setUp() throws Exception {
        _roleManager = mock(RoleManager.class);
    }

    @Test
    public void stubRole() {
        assertThat(_roleManager.getRole("test"), nullValue());

        Role role = mock(Role.class);
        doReturn("test").when(role).getName();
        RoleManagerStubbingOperation.stubRole(role).of(_roleManager);
        assertThat(_roleManager.getRole("test"), is(role));
        assertThat(_roleManager.getRoleNameById("test-td"), nullValue());

        doReturn("test-id").when(role).getId();
        RoleManagerStubbingOperation.stubRole(role).of(_roleManager);
        assertThat(_roleManager.getRole("test"), is(role));
        assertThat(_roleManager.getRoleNameById("test-id"), is("test"));
    }

    @Test
    public void stubRoleNameById() {
        assertThat(_roleManager.getRoleNameById("test-td"), nullValue());

        RoleManagerStubbingOperation.stubRoleNameById("test-id", "test").of(_roleManager);
        assertThat(_roleManager.getRoleNameById("test-id"), is("test"));
    }

    @Test
    public void stubAcl() {
        assertThat(_roleManager.getACLs("test").isEmpty(), is(true));

        ACL acl = mock(ACL.class);
        doReturn("test-acl").when(acl).getName();
        RoleManagerStubbingOperation.stubAcl("test", acl).of(_roleManager);
        assertThat(_roleManager.getACLs("test").size(), is(1));
        assertThat(_roleManager.getACLs("test").get("test-acl"), is(acl));

        ACL acl2 = mock(ACL.class);
        doReturn("test-acl2").when(acl2).getName();
        RoleManagerStubbingOperation.stubAcl("test", acl2).of(_roleManager);
        assertThat(_roleManager.getACLs("test").size(), is(2));
    }
}
