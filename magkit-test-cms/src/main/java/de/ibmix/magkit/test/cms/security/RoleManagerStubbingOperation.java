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
 * Factory holder for creating {@link RoleManager} related {@link StubbingOperation}s.<br>
 * <p>
 * Provides reusable Mockito stubbing operations to configure a {@link RoleManager} mock in tests, keeping test code
 * declarative while hiding repetitive stubbing logic. Intended for usage with {@code SecurityMockUtils.mockRoleManager(...)},
 * or direct application via {@code op.of(roleManagerMock)}.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All factory methods return non-null operations.</li>
 *   <li>Argument validation via {@code assertThat} produces {@link AssertionError} on failure when executed.</li>
 *   <li>Operations only mutate supplied mocks, no shared state retained.</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *   Role r = Mockito.mock(Role.class);
 *   RoleStubbingOperation.stubName("publisher").of(r);
 *   RoleManagerStubbingOperation.stubRole(r).of(SecurityMockUtils.mockRoleManager());
 * </pre>
 * </p>
 * <p><b>Thread safety:</b> Stateless operations; typical single-threaded test usage assumed.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-02
 */
public abstract class RoleManagerStubbingOperation implements StubbingOperation<RoleManager> {

    /**
     * Registers the provided {@link Role} with the {@link RoleManager} mock making {@link RoleManager#getRole(String)}
     * and (if id present) {@link RoleManager#getRoleNameById(String)} return consistent values.
     *
     * @param role role mock to register (must not be null when executed)
     * @return stubbing operation (never null)
     * @throws AssertionError if target manager or role (or its name) is null when executed
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
     * Stubs {@link RoleManager#getRoleNameById(String)} to return the provided role name for the given id.
     *
     * @param id   role identifier (must not be null when executed)
     * @param name role name to return (may be null if test requires absent mapping)
     * @return stubbing operation
     * @throws AssertionError if manager or id is null when executed
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
     * Adds or replaces an {@link ACL} for the specified role name by updating the map returned from
     * {@link RoleManager#getACLs(String)} so it contains the given ACL keyed by its own name.
     *
     * @param role role name
     * @param acl  access control list instance
     * @return stubbing operation
     * @throws AssertionError if manager, role, acl or acl name are null when executed
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
