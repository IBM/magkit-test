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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.auth.ACL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.mockito.Mockito.doReturn;

/**
 * Factory holder for creating {@link GroupManager} related {@link StubbingOperation}s.<br>
 * <p>
 * Supplies composable Mockito stubbing operations to configure a {@link GroupManager} mock in tests. Intended for
 * use with {@code SecurityMockUtils.mockGroupManager(...)} or by direct application via {@code op.of(groupManagerMock)}.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All factory methods return non-null operations.</li>
 *   <li>Argument validation uses {@code assertThat} and throws {@link IllegalArgumentException} upon failure when executed.</li>
 *   <li>Operations mutate only the passed mock; no shared state.</li>
 * </ul>
 * Example:
 * <pre>
 *   Group g = Mockito.mock(Group.class);
 *   GroupStubbingOperation.stubName("editors").of(g);
 *   GroupManager manager = SecurityMockUtils.mockGroupManager();
 *   GroupManagerStubbingOperation.stubGroup(g).of(manager);
 * </pre>
 * <p><b>Thread safety:</b> Stateless operations; typical single-threaded test usage assumed.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-02
 */
public abstract class GroupManagerStubbingOperation implements StubbingOperation<GroupManager> {

    /**
     * Registers (or refreshes) the given {@link Group} within the target {@link GroupManager} mock.
     * Ensures {@link GroupManager#getGroup(String)} returns the group and adds it to {@link GroupManager#getAllGroups()}.
     *
     * @param group group mock to register (must not be null when executed)
     * @return stubbing operation (never null)
     * @throws IllegalArgumentException if target manager or group is null when executed
     */
    public static GroupManagerStubbingOperation stubGroup(final Group group) {
        Require.Argument.notNull(group, "group should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                String name = group.getName();
                try {
                    doReturn(group).when(groupManager).getGroup(name);
                    Collection<Group> allGroups = groupManager.getAllGroups();
                    if (!allGroups.contains(group)) {
                        allGroups.add(group);
                        doReturn(allGroups).when(groupManager).getAllGroups();
                    }
                } catch (AccessDeniedException e) {
                    // ignored for mocks
                }
            }
        };
    }

    /**
     * Registers all provided groups using {@link #stubGroup(Group)} for each element.
     *
     * @param groups groups to register (array reference must not be null when executed; elements may be null-safe tested downstream)
     * @return stubbing operation aggregating registrations
     * @throws IllegalArgumentException if target manager or {@code groups} is null when executed
     */
    public static GroupManagerStubbingOperation stubAllGroups(final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                Arrays.stream(groups).forEach(group -> stubGroup(group).of(groupManager));
            }
        };
    }

    /**
     * Stubs {@link GroupManager#getAllSuperGroups(String)} to return the provided groups for the given group name.
     *
     * @param groupName target group name
     * @param groups    super groups to return
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, group array or groupName are null when executed
     */
    public static GroupManagerStubbingOperation stubAllSuperGroups(final String groupName, final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        Require.Argument.notNull(groupName, "groupName should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                doReturn(Arrays.asList(groups)).when(groupManager).getAllSuperGroups(groupName);
            }
        };
    }

    /**
     * Stubs {@link GroupManager#getAllSubGroups(String)} to return the provided groups for the given group name.
     *
     * @param groupName target group name
     * @param groups    sub groups to return
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, group array or groupName are null when executed
     */
    public static GroupManagerStubbingOperation stubAllSubGroups(final String groupName, final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        Require.Argument.notNull(groupName, "groupName should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                doReturn(Arrays.asList(groups)).when(groupManager).getAllSubGroups(groupName);
            }
        };
    }

    /**
     * Stubs {@link GroupManager#getDirectSubGroups(String)} for the provided group name.
     *
     * @param groupName group whose direct sub groups are requested
     * @param groups    direct sub groups
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, group array or groupName are null when executed
     */
    public static GroupManagerStubbingOperation stubDirectSubGroups(final String groupName, final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        Require.Argument.notNull(groupName, "groupName should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                doReturn(Arrays.asList(groups)).when(groupManager).getDirectSubGroups(groupName);
            }
        };
    }

    /**
     * Stubs {@link GroupManager#getDirectSuperGroups(String)} for the provided group name.
     *
     * @param groupName group whose direct super groups are requested
     * @param groups    direct super groups
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, group array or groupName are null when executed
     */
    public static GroupManagerStubbingOperation stubDirectSuperGroups(final String groupName, final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        Require.Argument.notNull(groupName, "groupName should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                doReturn(Arrays.asList(groups)).when(groupManager).getDirectSuperGroups(groupName);
            }
        };
    }

    /**
     * Stubs {@link GroupManager#getGroupsWithRole(String)} returning the provided groups.
     *
     * @param roleName role name
     * @param groups   groups owning the role
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, group array or roleName are null when executed
     */
    public static GroupManagerStubbingOperation stubGroupsWithRole(final String roleName, final Group... groups) {
        Require.Argument.notNull(groups, "groups should not be null");
        Require.Argument.notNull(roleName, "roleName should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                doReturn(Arrays.asList(groups)).when(groupManager).getGroupsWithRole(roleName);
            }
        };
    }

    /**
     * Adds or replaces an {@link ACL} entry for the given group name. Updates the map returned by
     * {@link GroupManager#getACLs(String)} to contain the provided ACL keyed by its name.
     *
     * @param groupName group name
     * @param acl       access control list definition
     * @return stubbing operation
     * @throws IllegalArgumentException if manager, groupName, acl or acl name are null when executed
     */
    public static GroupManagerStubbingOperation stubAcl(final String groupName, ACL acl) {
        Require.Argument.notNull(groupName, "groupName should not be null");
        Require.Argument.notNull(acl, "acl should not be null");
        Require.Argument.notNull(acl.getName(), "acl name should not be null");
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager groupManager) {
                Require.Argument.notNull(groupManager, "groupManager should not be null");
                Map<String, ACL> acls = groupManager.getACLs(groupName);
                acls.put(acl.getName(), acl);
                doReturn(acls).when(groupManager).getACLs(groupName);
            }
        };
    }
}
