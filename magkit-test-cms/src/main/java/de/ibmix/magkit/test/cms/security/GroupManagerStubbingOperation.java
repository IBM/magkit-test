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
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.auth.ACL;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;

/**
 * Utility class that provides factory methods for GroupManagerStubbingOperation that stub the behaviour of a GroupManager mock.
 * Stubbing operations to be used as parameters in SecurityMockUtils.mockGroupManager(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-02
 */
public abstract class GroupManagerStubbingOperation implements StubbingOperation<GroupManager> {

    public static GroupManagerStubbingOperation stubGroup(final Group group) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(group, notNullValue());
                String name = group.getName();
                try {
                    doReturn(group).when(mock).getGroup(name);
                    Collection<Group> allGroups = mock.getAllGroups();
                    if (!allGroups.contains(group)) {
                        allGroups.add(group);
                        doReturn(allGroups).when(mock).getAllGroups();
                    }
                } catch (AccessDeniedException e) {
                    // ignore, will not happen on mocks
                }
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllGroups(final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                Arrays.stream(groups).forEach(group -> stubGroup(group).of(mock));
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllSuperGroups(final String groupName, final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(Arrays.asList(groups)).when(mock).getAllSuperGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllSubGroups(final String groupName, final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(Arrays.asList(groups)).when(mock).getAllSubGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubDirectSubGroups(final String groupName, final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(Arrays.asList(groups)).when(mock).getDirectSubGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubDirectSuperGroups(final String groupName, final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(Arrays.asList(groups)).when(mock).getDirectSuperGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubGroupsWithRole(final String roleName, final Group... groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(roleName, notNullValue());
                doReturn(Arrays.asList(groups)).when(mock).getGroupsWithRole(roleName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubAcl(final String groupName, ACL acl) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(acl, notNullValue());
                assertThat(acl.getName(), notNullValue());
                assertThat(groupName, notNullValue());
                Map<String, ACL> acls = mock.getACLs(groupName);
                acls.put(acl.getName(), acl);
                doReturn(acls).when(mock).getACLs(groupName);
            }
        };
    }
}
