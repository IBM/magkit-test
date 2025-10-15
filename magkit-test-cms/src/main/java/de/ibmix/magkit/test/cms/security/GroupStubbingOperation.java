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
 * Factory holder for creating {@link Group} related {@link StubbingOperation}s.<br>
 * <p>
 * Provides focused Mockito stubbing operations for configuring Magnolia {@link Group} mocks. Commonly combined with
 * {@code SecurityMockUtils.mockGroup(...)} or applied directly via {@code op.of(groupMock)} to keep test setup clear
 * and intention-revealing.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All methods return non-null operations.</li>
 *   <li>Each operation asserts a non-null target group when executed (throwing {@link AssertionError} if violated).</li>
 *   <li>Operations are stateless and side-effect only on the supplied mock.</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *   Group g = Mockito.mock(Group.class);
 *   GroupStubbingOperation.stubName("authors").of(g);
 *   GroupStubbingOperation.stubRoles("editor").of(g);
 * </pre>
 * </p>
 * <p><b>Thread safety:</b> Stateless operations; typical single-threaded test usage assumed.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class GroupStubbingOperation implements StubbingOperation<Group> {

    /**
     * Stubs {@link Group#getName()}.
     *
     * @param name name value (may be null)
     * @return stubbing operation
     * @throws AssertionError if executed with null group
     */
    public static GroupStubbingOperation stubName(final String name) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                doReturn(name).when(group).getName();
            }
        };
    }

    /**
     * Stubs {@link Group#getId()}.
     *
     * @param uuid group identifier (may be null)
     * @return stubbing operation
     * @throws AssertionError if executed with null group
     */
    public static GroupStubbingOperation stubId(final String uuid) {
        return new GroupStubbingOperation() {
            @Override
            public void of(Group group) {
                assertThat(group, notNullValue());
                doReturn(uuid).when(group).getId();
            }
        };
    }

    /**
     * Stubs a single property lookup for {@link Group#getProperty(String)}.
     *
     * @param name  property key (must not be null when executed)
     * @param value property value
     * @return stubbing operation
     * @throws AssertionError if executed with null group or null name
     */
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

    /**
     * Stubs {@link Group#getGroups()} to return the provided subgroup names.
     *
     * @param groupNames subgroup names (nullable array -> empty list)
     * @return stubbing operation
     * @throws AssertionError if executed with null group
     */
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

    /**
     * Stubs {@link Group#getAllGroups()} to return all (transitive) subgroup names provided.
     *
     * @param groupNames group names (nullable array -> empty list)
     * @return stubbing operation
     * @throws AssertionError if executed with null group
     */
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

    /**
     * Stubs {@link Group#getRoles()} and {@link Group#hasRole(String)} for the provided role names.
     * Any {@link AccessDeniedException} potentially thrown by {@link Group#hasRole(String)} is suppressed since
     * mocked invocations won't trigger repository access.
     *
     * @param roleNames role names (nullable array -> empty list)
     * @return stubbing operation
     * @throws AssertionError if executed with null group
     */
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
                        // ignored for mocks
                    }
                });
            }
        };
    }

    private GroupStubbingOperation() {}
}
