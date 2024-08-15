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
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

public abstract class UserManagerStubbingOperation implements StubbingOperation<UserManager> {

    public static UserManagerStubbingOperation stubUser(final String name, final String uuid, UserStubbingOperation... stubbings) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                assertThat(name, notNullValue());
                User user = mock.getUser(name);
                if (user == null) {
                    user = Mockito.mock(User.class);
                    UserStubbingOperation.stubName(name).of(user);
                    UserStubbingOperation.stubIdentifier(uuid).of(user);
                    stubUser(user).of(mock);
                }
                User finalUser = user;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    public static UserManagerStubbingOperation stubUser(final User user) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                assertThat(user, notNullValue());
                Collection<User> allUsers = mock.getAllUsers();
                String userName = user.getName();
                if (isNotEmpty(userName)) {
                    when(mock.getUser(userName)).thenReturn(user);
                }
                String uuid = user.getIdentifier();
                if (isNotEmpty(uuid)) {
                    User existing = mock.getUserById(uuid);
                    if (existing != null) {
                        allUsers.remove(existing);
                    }
                    when(mock.getUserById(uuid)).thenReturn(user);
                }
                allUsers.add(user);
            }
        };
    }

    public static UserManagerStubbingOperation stubSystemUser(UserStubbingOperation... stubbings) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                User systemUser = mock.getSystemUser();
                if (systemUser == null) {
                    String identifier = UUID.randomUUID().toString();
                    stubUser(UserManager.SYSTEM_USER, identifier, UserStubbingOperation.stubPassword(UserManager.SYSTEM_USER)).of(mock);
                    systemUser = mock.getUserById(identifier);
                    when(mock.getSystemUser()).thenReturn(systemUser);
                }
                User finalUser = systemUser;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    public static UserManagerStubbingOperation stubAnonymousUser(UserStubbingOperation... stubbings) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                User anonymous = mock.getAnonymousUser();
                if (anonymous == null) {
                    String identifier = UUID.randomUUID().toString();
                    stubUser(UserManager.ANONYMOUS_USER, identifier).of(mock);
                    anonymous = mock.getUserById(identifier);
                    when(mock.getAnonymousUser()).thenReturn(anonymous);
                }
                User finalUser = anonymous;
                Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalUser));
            }
        };
    }

    public static UserManagerStubbingOperation stubAllUsers(final Collection<User> allUsers) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                // remove stubbings for existing users (ignore anonymous & system user):
                for (User old : mock.getAllUsers()) {
                    when(mock.getUserById(old.getIdentifier())).thenReturn(null);
                    when(mock.getUser(old.getName())).thenReturn(null);
                }
                // assert that we always use a Set internally (no dublettes - well almost :-) )
                Collection<User> newUsers = new HashSet<>();
                if (allUsers != null) {
                    newUsers.addAll(allUsers);
                }
                for (User newUser : newUsers) {
                    when(mock.getUserById(newUser.getIdentifier())).thenReturn(newUser);
                    when(mock.getUser(newUser.getName())).thenReturn(newUser);
                }
                when(mock.getAllUsers()).thenReturn(newUsers);
            }
        };
    }

    public static UserManagerStubbingOperation stubLockTimePeriod(int lockTimePeriod) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                when(mock.getLockTimePeriod()).thenReturn(lockTimePeriod);
            }
        };
    }

    public static UserManagerStubbingOperation stubMaxFailedLoginAttempts(int maxFailedLoginAttempts) {
        return new UserManagerStubbingOperation() {
            @Override
            public void of(UserManager mock) {
                assertThat(mock, notNullValue());
                when(mock.getMaxFailedLoginAttempts()).thenReturn(maxFailedLoginAttempts);
            }
        };
    }
}
