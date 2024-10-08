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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Utility class that provides factory methods for UserStubbingOperation.
 * Stubbing operations to be used as parameters in SecurityMockUtils.mockUser(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class UserStubbingOperation implements StubbingOperation<User> {

    public static UserStubbingOperation stubName(final String name) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(name).when(user).getName();
            }
        };
    }

    public static UserStubbingOperation stubIdentifier(final String uuid) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                String identifier = isNotBlank(uuid) ? uuid : UUID.randomUUID().toString();
                doReturn(identifier).when(user).getIdentifier();
            }
        };
    }

    public static UserStubbingOperation stubPassword(final String password) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(password).when(user).getPassword();
            }
        };
    }

    public static UserStubbingOperation stubLanguage(final String language) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(language).when(user).getLanguage();
            }
        };
    }

    public static UserStubbingOperation stubEnabled(final boolean enabled) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(enabled).when(user).isEnabled();
            }
        };
    }

    public static UserStubbingOperation stubProperty(final String name, final String value) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                assertThat(name, notNullValue());
                doReturn(value).when(user).getProperty(name);
            }
        };
    }

    public static UserStubbingOperation stubGroups(final String... groupNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(user).getGroups();
            }
        };
    }

    public static UserStubbingOperation stubAllGroups(final String... groupNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(user).getAllGroups();
            }
        };
    }

    public static UserStubbingOperation stubRoles(final String... roleNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(user).getRoles();
                roleList.forEach(role -> doReturn(true).when(user).hasRole(role));
            }
        };
    }

    public static UserStubbingOperation stubAllRoles(final String... roleNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(user).getAllRoles();
                roleList.forEach(role -> doReturn(true).when(user).hasRole(role));
            }
        };
    }

    private UserStubbingOperation() {}
}
