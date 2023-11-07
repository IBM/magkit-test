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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * A factory class to create StubbingOperations that define the behaviour of info.magnolia.cms.security.Group mocks.
 * To be used standalone or as parameter of SecurityMockUtils.mockGroup(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class GroupStubbingOperation implements StubbingOperation<Group> {

    public static GroupStubbingOperation stubName(final String name) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                doReturn(name).when(group).getName();
            }
        };
    }

    public static GroupStubbingOperation stubId(final String uuid) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                doReturn(uuid).when(group).getId();
            }
        };
    }

    public static GroupStubbingOperation stubProperty(final String name, final String value) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                assertThat(name, notNullValue());
                doReturn(value).when(group).getProperty(name);
            }
        };
    }

    public static GroupStubbingOperation stubGroups(final String... groupNames) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(group).getGroups();
            }
        };
    }

    public static GroupStubbingOperation stubAllGroups(final String... groupNames) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(group).getAllGroups();
            }
        };
    }

    public static GroupStubbingOperation stubRoles(final String... roleNames) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(group).getRoles();
                roleList.forEach(role -> {
                    try {
                        doReturn(true).when(group).hasRole(role);
                    } catch (AccessDeniedException e) {
                        // do nothing, never happen while mocking
                    }
                });
            }
        };
    }

    private GroupStubbingOperation() {}
}
