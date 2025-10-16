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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockito.Mockito.doReturn;

/**
 * Factory holder for creating {@link User} related {@link StubbingOperation}s.<br>
 * <p>
 * Provides small, composable operations to configure mocked Magnolia {@link User} instances with Mockito without
 * leaking mocking details into test bodies. Intended for usage with higher level helpers like
 * {@code SecurityMockUtils.mockUser(...)} or direct application via {@code op.of(userMock)}.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All public factory methods return non-null operations.</li>
 *   <li>Each operation validates its target via {@code assertThat} and throws {@link IllegalArgumentException} when the user is null.</li>
 *   <li>Stateless: no shared mutable state.</li>
 * </ul>
 * <p>
 * Example:
 * </p>
 * <pre>
 *   User u = Mockito.mock(User.class);
 *   UserStubbingOperation.stubName("alice").of(u);
 *   UserStubbingOperation.stubRoles("editor", "publisher").of(u);
 * </pre>
 * <p><b>Thread safety:</b> Stateless operations; typical single-threaded test usage assumed.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class UserStubbingOperation implements StubbingOperation<User> {

    /**
     * Stubs {@link User#getName()} to return the given name.
     *
     * @param name user name to return (may be null to simulate unnamed user)
     * @return stubbing operation (never null)
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubName(final String name) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                doReturn(name).when(user).getName();
            }
        };
    }

    /**
     * Stubs {@link User#getIdentifier()} to return the provided uuid or a random UUID if blank.
     *
     * @param uuid identifier to use (blank -> random)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubIdentifier(final String uuid) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                String identifier = isNotBlank(uuid) ? uuid : UUID.randomUUID().toString();
                doReturn(identifier).when(user).getIdentifier();
            }
        };
    }

    /**
     * Stubs {@link User#getPassword()}.
     *
     * @param password password value (may be null)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubPassword(final String password) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                doReturn(password).when(user).getPassword();
            }
        };
    }

    /**
     * Stubs {@link User#getLanguage()}.
     *
     * @param language language value (may be null)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubLanguage(final String language) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                doReturn(language).when(user).getLanguage();
            }
        };
    }

    /**
     * Stubs {@link User#isEnabled()}.
     *
     * @param enabled enabled flag to return
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubEnabled(final boolean enabled) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                doReturn(enabled).when(user).isEnabled();
            }
        };
    }

    /**
     * Stubs a single property lookup via {@link User#getProperty(String)} returning the given value.
     *
     * @param name  property key (must not be null when executed)
     * @param value property value (may be null)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user or null name
     */
    public static UserStubbingOperation stubProperty(final String name, final String value) {
        Require.Argument.notNull(name, "name should not be null");
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                doReturn(value).when(user).getProperty(name);
            }
        };
    }

    /**
     * Stubs {@link User#getGroups()} returning an immutable view of the provided names.
     *
     * @param groupNames group names (nullable array -> empty list)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubGroups(final String... groupNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(user).getGroups();
            }
        };
    }

    /**
     * Stubs {@link User#getAllGroups()} returning an immutable view of the provided names.
     *
     * @param groupNames group names (nullable array -> empty list)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubAllGroups(final String... groupNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(user).getAllGroups();
            }
        };
    }

    /**
     * Stubs {@link User#getRoles()} and {@link User#hasRole(String)} for the provided role names.
     *
     * @param roleNames role names (nullable array -> empty list)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubRoles(final String... roleNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(user).getRoles();
                roleList.forEach(role -> doReturn(true).when(user).hasRole(role));
            }
        };
    }

    /**
     * Stubs {@link User#getAllRoles()} and {@link User#hasRole(String)} for the provided role names.
     *
     * @param roleNames role names (nullable array -> empty list)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null target user
     */
    public static UserStubbingOperation stubAllRoles(final String... roleNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                Require.Argument.notNull(user, "user should not be null");
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(user).getAllRoles();
                roleList.forEach(role -> doReturn(true).when(user).hasRole(role));
            }
        };
    }

    private UserStubbingOperation() {}
}
