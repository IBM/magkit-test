package com.aperto.magkit.mockito;

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

import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 30.04.13
 */
public final class SecurityMockUtils extends ComponentsMockUtils {

    public static void cleanSecuritySupport() {
        clearComponentProvider(SecuritySupport.class);
    }

    public static SecuritySupport mockSecuritySupport() {
        return mockComponentInstance(SecuritySupport.class);
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

    public static void register(String realm, User user) {
        UserManager userManager = mockUserManager(realm);
        when(userManager.getUser(user.getName())).thenReturn(user);
        when(userManager.getUserById(user.getIdentifier())).thenReturn(user);
    }

    public static User mockUser(final String name, UserStubbingOperation... stubbings) {
        return mockUser(name, UUID.randomUUID().toString(), stubbings);
    }

    public static User mockUser(final String name, final String uuid, UserStubbingOperation... stubbings) {
        User user = mock(User.class);
        UserStubbingOperation.stubbName(name).of(user);
        UserStubbingOperation.stubbIdentifier(uuid).of(user);
        if (ArrayUtils.isNotEmpty(stubbings)) {
            Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(user));
        }
        return user;
    }

    public Group mockGroup(final String name, GroupStubbingOperation... stubbings) {
        Group group = mock(Group.class);
        GroupStubbingOperation.stubbName(name).of(group);
        if (ArrayUtils.isNotEmpty(stubbings)) {
            Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(group));
        }
        return group;
    }

    public Role mockRole(final String name, RoleStubbingOperation... stubbings) {
        Role role = mock(Role.class);
        if (ArrayUtils.isNotEmpty(stubbings)) {
            Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(role));
        }
        return role;
    }

}
