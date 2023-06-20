package de.ibmix.magkit.mockito;

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

import info.magnolia.cms.security.User;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * The StubbingOperations for info.magnolia.cms.security.User mocks.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2032-06-16
 */
public abstract class UserStubbingOperation {

    public static UserStubbingOperation stubbName(final String name) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(name).when(user).getName();
            }
        };
    }

    public static UserStubbingOperation stubbIdentifier(final String uuid) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(uuid).when(user).getIdentifier();
            }
        };
    }

    public static UserStubbingOperation stubbPassword(final String password) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(password).when(user).getPassword();
            }
        };
    }

    public static UserStubbingOperation stubbLanguage(final String language) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(language).when(user).getLanguage();
            }
        };
    }

    public static UserStubbingOperation stubbEnabled(final boolean enabled) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                doReturn(enabled).when(user).isEnabled();
            }
        };
    }

    public static UserStubbingOperation stubbProperty(final String name, final String value) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                assertThat(name, notNullValue());
                doReturn(value).when(user).getProperty(name);
            }
        };
    }

    public static UserStubbingOperation stubbGroups(final String... groupNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> groupList = groupNames == null ? Collections.emptyList() : Arrays.asList(groupNames);
                doReturn(groupList).when(user).getGroups();
                doReturn(groupList).when(user).getAllGroups();
            }
        };
    }

    public static UserStubbingOperation stubbRoles(final String... roleNames) {
        return new UserStubbingOperation() {
            @Override
            public void of(User user) {
                assertThat(user, notNullValue());
                Collection<String> roleList = roleNames == null ? Collections.emptyList() : Arrays.asList(roleNames);
                doReturn(roleList).when(user).getRoles();
                doReturn(roleList).when(user).getAllRoles();
            }
        };
    }

    public abstract void of(User user);
}
