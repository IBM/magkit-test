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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.WebContext;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import java.util.Arrays;
import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubAccessManager;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * An util class to create Mockito mocks of magnolia security classes.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-04-30
 */
public final class SecurityMockUtils extends ComponentsMockUtils {

    public static void cleanSecuritySupport() {
        clearComponentProvider(SecuritySupport.class);
    }

    public static SecuritySupport mockSecuritySupport() {
        return mockComponentInstance(SecuritySupport.class);
    }

    public static AccessManager mockAccessManager(AccessManagerStubbingOperation... stubbings) throws RepositoryException {
        return mockAccessManager(WEBSITE, stubbings);
    }

    public static AccessManager mockAccessManager(String repositoryId, AccessManagerStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        WebContext context = mockWebContext();
        String repoId = isBlank(repositoryId) ? WEBSITE : repositoryId;
        AccessManager am = context.getAccessManager(repoId);
        if (am == null) {
            am = mock(AccessManager.class);
            stubAccessManager(repositoryId, am).of(context);
        }
        for (AccessManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(am);
        }
        return am;
    }

    public static UserManager mockUserManager(String realm) {
        SecuritySupport security = mockSecuritySupport();
        UserManager userManager = security.getUserManager(realm);
        if (userManager == null) {
            userManager = mock(UserManager.class);
            when(security.getUserManager(realm)).thenReturn(userManager);
        }
        return userManager;
    }

    public static GroupManager mockGroupManager() {
        SecuritySupport security = mockSecuritySupport();
        GroupManager manager = security.getGroupManager();
        if (manager == null) {
            manager = mock(GroupManager.class);
            when(security.getGroupManager()).thenReturn(manager);
        }
        return manager;
    }

    public static RoleManager mockRoleManager() {
        SecuritySupport security = mockSecuritySupport();
        RoleManager manager = security.getRoleManager();
        if (manager == null) {
            manager = mock(RoleManager.class);
            when(security.getRoleManager()).thenReturn(manager);
        }
        return manager;
    }

    public static void register(String realm, User user) {
        UserManager userManager = mockUserManager(realm);
        when(userManager.getUser(user.getName())).thenReturn(user);
        when(userManager.getUserById(user.getIdentifier())).thenReturn(user);
    }

    public static void register(Group group) throws AccessDeniedException {
        GroupManager manager = mockGroupManager();
        when(manager.getGroup(group.getName())).thenReturn(group);
    }

    public static void register(Role role) {
        RoleManager manager = mockRoleManager();
        when(manager.getRole(role.getName())).thenReturn(role);
    }

    public static User mockUser(final String name, UserStubbingOperation... stubbings) {
        return mockUser(name, UUID.randomUUID().toString(), stubbings);
    }

    public static User mockUser(final String name, final String uuid, UserStubbingOperation... stubbings) {
        User user = mock(User.class);
        UserStubbingOperation.stubName(name).of(user);
        UserStubbingOperation.stubIdentifier(uuid).of(user);
        if (ArrayUtils.isNotEmpty(stubbings)) {
            Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(user));
        }
        return user;
    }

    public static Group mockGroup(final String name, GroupStubbingOperation... stubbings) {
        return mockGroup(name, UUID.randomUUID().toString(), stubbings);
    }

    public static Group mockGroup(final String name, final String uuid, GroupStubbingOperation... stubbings) {
        Group group = mock(Group.class);
        GroupStubbingOperation.stubName(name).of(group);
        GroupStubbingOperation.stubId(uuid).of(group);
        if (ArrayUtils.isNotEmpty(stubbings)) {
            Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(group));
        }
        return group;
    }

    public static Role mockRole(final String name) {
        return mockRole(name, UUID.randomUUID().toString());
    }

    public static Role mockRole(final String name, final String uuid) {
        Role role = mock(Role.class);
        RoleStubbingOperation.stubName(name).of(role);
        RoleStubbingOperation.stubId(uuid).of(role);
        return role;
    }

}
