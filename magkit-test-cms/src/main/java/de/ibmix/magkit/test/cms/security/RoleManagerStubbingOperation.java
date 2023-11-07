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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.auth.ACL;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;

/**
 * Utility class that provides factory methods for RoleManagerStubbingOperation that stub the behaviour of a RoleManager mock.
 * Stubbing operations to be used as parameters in SecurityMockUtils.mockRoleManager(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-02
 */
public abstract class RoleManagerStubbingOperation implements StubbingOperation<RoleManager> {

    /**
     * Create a RoleManagerStubbingOperation that registers a Role at the RoleManager.
     * Stubs method getRole(String name) to return the Role
     * and method getRoleNameById(String id) to return the name of the provided Role.
     *
     * @param role the Role to be registered
     * @return the RoleManagerStubbingOperation, never null
     */
    public static RoleManagerStubbingOperation stubRole(final Role role) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(role, notNullValue());
                assertThat(role.getName(), notNullValue());
                String name = role.getName();
                doReturn(role).when(mock).getRole(name);
                String id = role.getId();
                if (isNotEmpty(id)) {
                    doReturn(name).when(mock).getRoleNameById(id);
                }
            }
        };
    }

    /**
     * Create a RoleManagerStubbingOperation for stubbing method getRoleNameById(String id) to return the name of the provided Role.
     *
     * @param id the Role id as String
     * @param name the Role name to be returned as String
     * @return the RoleManagerStubbingOperation, never null
     */
    public static RoleManagerStubbingOperation stubRoleNameById(final String id, final String name) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(id, notNullValue());
                doReturn(name).when(mock).getRoleNameById(id);
            }
        };
    }

    /**
     * Create a RoleManagerStubbingOperation for adding an access control list (ACL) for a role.
     * Stubs method getACLs(String roleName) to return the given ACL.
     *
     * @param role the name of the role
     * @param acl the ACL for this role
     * @return the RoleManagerStubbingOperation, never null
     */
    public static RoleManagerStubbingOperation stubAcl(final String role, final ACL acl) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(role, notNullValue());
                assertThat(acl, notNullValue());
                assertThat(acl.getName(), notNullValue());
                Map<String, ACL> acls = mock.getACLs(role);
                acls.put(acl.getName(), acl);
                doReturn(acls).when(mock).getACLs(role);
            }
        };
    }
}
