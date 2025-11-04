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
import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.WebContext;

import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAccessManager;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility/factory methods to create Mockito based mocks for Magnolia security related components
 * (AccessManager, UserManager, GroupManager, RoleManager and their domain objects).<br>
 * <p>
 * The methods follow a lazy creation pattern: if a mock for the requested object already exists (as returned by
 * the currently mocked {@link SecuritySupport} or {@link WebContext}), it is reused; otherwise a new mock is created,
 * registered (where appropriate) and optionally further configured through provided stubbing operations.
 * </p>
 * <p>
 * Each factory method accepts zero or more dedicated *StubbingOperation functional interfaces allowing concise,
 * chainable customization of the produced mocks without exposing Mockito calls at the call site. This keeps
 * test code expressive and focused on intent.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>Never returns {@code null}; will assert required arguments (realm, repository id, stubbing arrays).</li>
 *   <li>Ensures idempotent creation inside a single test execution path (reuses already registered mocks).</li>
 *   <li>Vararg stubbing parameters are applied in order of appearance.</li>
 * </ul>
 * Typical usage example:
 * <pre>
 *   User user = SecurityMockUtils.mockUser("author", UserStubbingOperation.stubEnabled(true));
 *   Role editors = SecurityMockUtils.mockRole("editors");
 * </pre>
 *
 * <p><b>Thread safety:</b> Implementation is backed by ComponentProvider that uses ThreadLocal and is thread-safe; intended for multithreaded test initialization code.</p>
 *
 * <p><b>Error handling:</b> Parameter validation relies on Hamcrest {@code assertThat}; failing preconditions
 * raise {@link IllegalArgumentException}.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-04-30
 */
public final class SecurityMockUtils extends ComponentsMockUtils {

    /**
     * Clears the registered {@link SecuritySupport} component from the Magnolia component provider / registry used
     * in tests so that subsequent calls will create a fresh mock instance.
     */
    public static void cleanSecuritySupport() {
        clearComponentProvider(SecuritySupport.class);
    }

    /**
     * Returns the (possibly newly created) Mockito mock instance of {@link SecuritySupport} registered in the
     * component provider. Subsequent calls will return the same instance until {@link #cleanSecuritySupport()} is invoked.
     *
     * @return mocked {@link SecuritySupport} (never {@code null})
     */
    public static SecuritySupport mockSecuritySupport() {
        return mockComponentInstance(SecuritySupport.class);
    }

    /**
     * Convenience overload creating or retrieving an {@link AccessManager} mock for the default website repository
     * and applying the provided stubbing operations.
     *
     * @param stubbings ordered varargs of operations to configure the resulting mock
     * @return mocked {@link AccessManager}
     * @throws RepositoryException if one of the provided stubbing operations triggers repository interaction issues
     * @throws IllegalArgumentException      if {@code stubbings} array reference is {@code null}
     */
    public static AccessManager mockAccessManager(AccessManagerStubbingOperation... stubbings) throws RepositoryException {
        return mockAccessManager(WEBSITE, stubbings);
    }

    /**
     * Creates or retrieves an {@link AccessManager} mock for the given repository id from the current {@link WebContext}.
     * If none exists yet a new mock is created, registered via {@link de.ibmix.magkit.test.cms.context.WebContextStubbingOperation#stubAccessManager(String, AccessManager)}
     * and subsequently configured by the supplied stubbing operations.
     *
     * @param repositoryId the logical Magnolia repository id (must be non blank)
     * @param stubbings    ordered varargs of operations to configure the access manager mock (must not be {@code null})
     * @return mocked {@link AccessManager} for the given repository id
     * @throws RepositoryException if a stubbing operation throws it
     * @throws IllegalArgumentException      if {@code repositoryId} is blank or {@code stubbings} array reference is {@code null}
     */
    public static AccessManager mockAccessManager(String repositoryId, AccessManagerStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        Require.Argument.notBlank(repositoryId, "repositoryId should not be null");
        WebContext context = mockWebContext();
        AccessManager am = context.getAccessManager(repositoryId);
        if (am == null) {
            am = mockComponentInstance(AccessManager.class);
            stubAccessManager(repositoryId, am).of(context);
        }
        for (AccessManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(am);
        }
        return am;
    }

    /**
     * Creates or retrieves the {@link UserManager} mock for the specified realm from the mocked {@link SecuritySupport}.
     * If it does not yet exist a new mock is created, registered and initialized with an empty user set.
     * All provided stubbing operations are applied afterwards.
     *
     * @param realm     Magnolia security realm (must not be {@code null})
     * @param stubbings optional ordered varargs of operations to configure the user manager mock
     * @return mocked {@link UserManager}
     * @throws IllegalArgumentException if {@code realm} is {@code null}
     */
    public static UserManager mockUserManager(String realm, UserManagerStubbingOperation... stubbings) {
        Require.Argument.notNull(realm, "realm should not be null");
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        SecuritySupport security = mockSecuritySupport();
        UserManager userManager = security.getUserManager(realm);
        if (userManager == null) {
            userManager = mock(UserManager.class);
            when(userManager.getAllUsers()).thenReturn(new HashSet<>());
            when(security.getUserManager(realm)).thenReturn(userManager);
        }
        UserManager finalManager = userManager;
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalManager));
        return userManager;
    }

    /**
     * Creates or retrieves the global {@link GroupManager} mock from the {@link SecuritySupport}. Applies any supplied
     * stubbing operations in order.
     *
     * @param stubbings optional ordered varargs of operations to configure the group manager mock
     * @return mocked {@link GroupManager}
     */
    public static GroupManager mockGroupManager(GroupManagerStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        SecuritySupport security = mockSecuritySupport();
        GroupManager manager = security.getGroupManager();
        if (manager == null) {
            manager = mock(GroupManager.class);
            when(security.getGroupManager()).thenReturn(manager);
        }
        GroupManager finalManager = manager;
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalManager));
        return manager;
    }

    /**
     * Creates or retrieves the global {@link RoleManager} mock from the {@link SecuritySupport}. Applies the provided
     * stubbing operations in order.
     *
     * @param stubbings ordered varargs of operations to configure the role manager mock (must not be {@code null})
     * @return mocked {@link RoleManager}
     * @throws IllegalArgumentException if {@code stubbings} array reference is {@code null}
     */
    public static RoleManager mockRoleManager(RoleManagerStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        SecuritySupport security = mockSecuritySupport();
        RoleManager manager = security.getRoleManager();
        if (manager == null) {
            manager = mock(RoleManager.class);
            when(security.getRoleManager()).thenReturn(manager);
        }
        RoleManager finalManager = manager;
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalManager));
        return finalManager;
    }

    /**
     * Convenience overload creating or retrieving a {@link User} mock in the default website realm. A random UUID is
     * generated for the user if it needs to be newly created. Additional stubbing operations are applied afterwards.
     *
     * @param name      logical user name (login)
     * @param stubbings ordered varargs customizing the user mock (must not be {@code null})
     * @return mocked {@link User}
     * @throws IllegalArgumentException if {@code stubbings} array reference is {@code null}
     */
    public static User mockUser(final String name, UserStubbingOperation... stubbings) {
        return mockUser(WEBSITE, name, UUID.randomUUID().toString(), stubbings);
    }

    /**
     * Creates or retrieves a {@link User} mock for the given realm and user name. If the user does not yet exist a
     * new mock is created (through a stubbing operation adding it to the {@link UserManager}) and initialized with the
     * provided uuid. Supplied stubbing operations are applied after creation.
     *
     * @param realm     Magnolia security realm
     * @param name      logical user name
     * @param uuid      identifier assigned to the user
     * @param stubbings ordered varargs customizing the user mock (must not be {@code null})
     * @return mocked {@link User}
     * @throws IllegalArgumentException if {@code stubbings} array reference is {@code null}
     */
    public static User mockUser(final String realm, final String name, final String uuid, UserStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        UserManager userManager = mockUserManager(realm);
        User user = userManager.getUser(name);
        if (user == null) {
            UserManagerStubbingOperation.stubUser(name, uuid).of(userManager);
        }
        User finalUser = userManager.getUser(name);
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
        return finalUser;
    }

    /**
     * Convenience overload creating or retrieving a {@link Group} mock with a generated UUID.
     *
     * @param name      group name
     * @param stubbings ordered varargs customizing the group mock (must not be {@code null})
     * @return mocked {@link Group}
     * @throws AccessDeniedException if a stubbing operation triggers an access check that fails
     * @throws IllegalArgumentException        if {@code stubbings} array reference is {@code null}
     */
    public static Group mockGroup(final String name, GroupStubbingOperation... stubbings) throws AccessDeniedException {
        return mockGroup(name, UUID.randomUUID().toString(), stubbings);
    }

    /**
     * Creates or retrieves a {@link Group} mock for the given name and UUID. If absent a new mock is created, has its
     * name and id stubbed and is registered with the {@link GroupManager}. Additional stubbing operations are then
     * applied.
     *
     * @param name      group name
     * @param uuid      group identifier
     * @param stubbings ordered varargs customizing the group mock (must not be {@code null})
     * @return mocked {@link Group}
     * @throws AccessDeniedException if a stubbing operation throws it
     * @throws IllegalArgumentException        if {@code stubbings} array reference is {@code null}
     */
    public static Group mockGroup(final String name, final String uuid, GroupStubbingOperation... stubbings) throws AccessDeniedException {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        GroupManager manager = mockGroupManager();
        Group group = manager.getGroup(name);
        if (group == null) {
            group = mock(Group.class);
            GroupStubbingOperation.stubName(name).of(group);
            GroupStubbingOperation.stubId(uuid).of(group);
            GroupManagerStubbingOperation.stubGroup(group).of(manager);
        }
        Group finalGroup = group;
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalGroup));
        return finalGroup;
    }

    /**
     * Convenience overload creating or retrieving a {@link Role} mock with a generated UUID.
     *
     * @param name role name
     * @return mocked {@link Role}
     */
    public static Role mockRole(final String name) {
        return mockRole(name, UUID.randomUUID().toString());
    }

    /**
     * Creates or retrieves a {@link Role} mock for the given name. If absent a new mock is produced, its name and id
     * are stubbed and it is registered with the {@link RoleManager}.
     *
     * @param name role name
     * @param uuid role identifier
     * @return mocked {@link Role}
     */
    public static Role mockRole(final String name, final String uuid) {
        RoleManager roleManager = mockRoleManager();
        Role role = roleManager.getRole(name);
        if (role == null) {
            role = mock(Role.class);
            RoleStubbingOperation.stubName(name).of(role);
            RoleStubbingOperation.stubId(uuid).of(role);
            RoleManagerStubbingOperation.stubRole(role).of(roleManager);
        }
        return role;
    }

}
