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
import info.magnolia.cms.security.UserManager;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mockito.Mockito.when;

/**
 * Factory holder for creating {@link UserManager} related {@link StubbingOperation}s.<br>
 * <p>
 * These operations encapsulate typical Mockito stubbings for Magnolia {@link UserManager} in tests and are intended
 * to be passed as vararg parameters to helper methods such as {@code SecurityMockUtils.mockUserManager(...)} or applied
 * directly using {@code operation.of(userManagerMock)}. They streamline test setup by hiding repetitive mock creation
 * and registration logic while remaining composable and side-effect predictable.
 * </p>
 * <p>
 * Contract / behavior notes:
 * </p>
 * <ul>
 *   <li>All public factory methods return non-null operations.</li>
 *   <li>Argument validation relies on {@code assertThat(...)} and therefore raises {@link IllegalArgumentException} on failure.</li>
 *   <li>Where a user does not yet exist it will be created and registered; existing users are reused.</li>
 *   <li>UUID handling for newly created users is delegated to {@link UserStubbingOperation#stubIdentifier(String)} which may provide defaults.</li>
 * </ul>
 * <p>
 * Typical usage example:
 * <pre>
 *   UserManager manager = ... // mocked elsewhere
 *   UserManagerStubbingOperation.stubUser("alice", null,
 *       UserStubbingOperation.stubEnabled(true),
 *       UserStubbingOperation.stubPassword("secret")
 *   ).of(manager);
 * </pre>
 * </p>
 * <p><b>Thread safety:</b> Not thread-safe; intended for single-threaded test setup.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2024-08-15
 */
public abstract class UserManagerStubbingOperation implements StubbingOperation<UserManager> {

    /**
     * Creates an operation that ensures a {@link User} with the given {@code name} exists in the target {@link UserManager} mock,
     * creating and registering it if absent. The user receives the provided {@code uuid} (or a fallback determined inside
     * {@link UserStubbingOperation#stubIdentifier(String)}) and then each additional {@link UserStubbingOperation} is applied
     * in the given order.
     *
     * @param name       non-null user name (login)
     * @param uuid       desired identifier; may be {@code null} or empty to trigger a random generation downstream
     * @param stubbings  optional additional user-level stubbings applied after creation/retrieval (non-null array reference required)
     * @return non-null operation adding or updating the user definition inside a {@link UserManager}
     * @throws IllegalArgumentException if target manager or {@code name} is null when executed
     */
    public static UserManagerStubbingOperation stubUser(final String name, final String uuid, UserStubbingOperation... stubbings) {
        Require.Argument.notNull(name, "name should not be null");
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                User user = userManager.getUser(name);
                if (user == null) {
                    user = Mockito.mock(User.class);
                    UserStubbingOperation.stubName(name).of(user);
                    UserStubbingOperation.stubIdentifier(uuid).of(user);
                    stubUser(user).of(userManager);
                }
                User finalUser = user;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    /**
     * Creates an operation that registers the supplied {@link User} mock with a {@link UserManager} and includes it
     * in the collection returned by {@link UserManager#getAllUsers()}. Existing mappings by name or identifier are
     * replaced to keep the returned collection consistent with lookups.
     *
     * @param user user mock to register (must not be null when executed)
     * @return non-null operation registering the user
     * @throws IllegalArgumentException if target manager or {@code user} is null when executed
     */
    public static UserManagerStubbingOperation stubUser(final User user) {
        Require.Argument.notNull(user, "user should not be null");
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                Collection<User> allUsers = userManager.getAllUsers();
                String userName = user.getName();
                if (isNotEmpty(userName)) {
                    when(userManager.getUser(userName)).thenReturn(user);
                }
                String uuid = user.getIdentifier();
                if (isNotEmpty(uuid)) {
                    User existing = userManager.getUserById(uuid);
                    if (existing != null) {
                        allUsers.remove(existing);
                    }
                    when(userManager.getUserById(uuid)).thenReturn(user);
                }
                allUsers.add(user);
            }
        };
    }

    /**
     * Creates an operation that ensures a system user (name and password {@code "superuser"}) is present. If no system
     * user exists a new mock is created with a random UUID and registered; otherwise only the extra stubbings are applied.
     *
     * @param stubbings optional additional user-level stubbings applied after retrieval/creation
     * @return non-null operation providing a system user definition
     * @throws IllegalArgumentException if target manager is null when executed
     */
    public static UserManagerStubbingOperation stubSystemUser(UserStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                User systemUser = userManager.getSystemUser();
                if (systemUser == null) {
                    String identifier = UUID.randomUUID().toString();
                    stubUser(UserManager.SYSTEM_USER, identifier, UserStubbingOperation.stubPassword(UserManager.SYSTEM_USER)).of(userManager);
                    systemUser = userManager.getUserById(identifier);
                    when(userManager.getSystemUser()).thenReturn(systemUser);
                }
                User finalUser = systemUser;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    /**
     * Creates an operation that ensures an anonymous user (name {@code "anonymous"}) is present. If absent a new mock
     * with a random UUID is created and registered; existing anonymous user is reused.
     *
     * @param stubbings optional user-level stubbings applied after retrieval/creation
     * @return non-null operation providing an anonymous user definition
     * @throws IllegalArgumentException if target manager is null when executed
     */
    public static UserManagerStubbingOperation stubAnonymousUser(UserStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                User anonymous = userManager.getAnonymousUser();
                if (anonymous == null) {
                    String identifier = UUID.randomUUID().toString();
                    stubUser(UserManager.ANONYMOUS_USER, identifier).of(userManager);
                    anonymous = userManager.getUserById(identifier);
                    when(userManager.getAnonymousUser()).thenReturn(anonymous);
                }
                User finalUser = anonymous;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    /**
     * Creates an operation that replaces all existing users registered in the {@link UserManager} mock with the
     * provided collection. For each old user, name/id lookups are reset to {@code null} before new mappings are
     * established. A defensive {@link HashSet} copy is created to avoid duplicate entries and external mutation.
     *
     * @param allUsers collection of users to become the new user set (may be {@code null} for an empty set)
     * @return non-null operation configuring the complete user set
     * @throws IllegalArgumentException if target manager is null when executed
     */
    public static UserManagerStubbingOperation stubAllUsers(final Collection<User> allUsers) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                for (User old : userManager.getAllUsers()) {
                    when(userManager.getUserById(old.getIdentifier())).thenReturn(null);
                    when(userManager.getUser(old.getName())).thenReturn(null);
                }
                Collection<User> newUsers = new HashSet<>();
                if (allUsers != null) {
                    newUsers.addAll(allUsers);
                }
                for (User newUser : newUsers) {
                    when(userManager.getUserById(newUser.getIdentifier())).thenReturn(newUser);
                    when(userManager.getUser(newUser.getName())).thenReturn(newUser);
                }
                when(userManager.getAllUsers()).thenReturn(newUsers);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link UserManager#getLockTimePeriod()} with the provided value.
     *
     * @param lockTimePeriod lock time period to return
     * @return non-null operation stubbing the lock time period
     * @throws IllegalArgumentException if target manager is null when executed
     */
    public static UserManagerStubbingOperation stubLockTimePeriod(int lockTimePeriod) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "userManager should not be null");
                when(userManager.getLockTimePeriod()).thenReturn(lockTimePeriod);
            }
        };
    }

    /**
     * Creates an operation that stubs {@link UserManager#getMaxFailedLoginAttempts()} with the given limit.
     *
     * @param maxFailedLoginAttempts number of failed attempts before lockout
     * @return non-null operation stubbing the max failed login attempts
     * @throws IllegalArgumentException if target manager is null when executed
     */
    public static UserManagerStubbingOperation stubMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager userManager) {
                Require.Argument.notNull(userManager, "groupManager should not be null");
                when(userManager.getMaxFailedLoginAttempts()).thenReturn(maxFailedLoginAttempts);
            }
        };
    }
}
