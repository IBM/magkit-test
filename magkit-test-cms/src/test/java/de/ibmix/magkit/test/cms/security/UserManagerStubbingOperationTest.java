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
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserManagerStubbingOperationTest {

    private UserManager _userManager;

    @Before
    public void setUp() throws Exception {
        // be paranoid
        ComponentsMockUtils.clearComponentProvider();
        _userManager = SecurityMockUtils.mockUserManager("test");
    }

    @After
    public void tearDown() throws Exception {
        // be polite
        ComponentsMockUtils.clearComponentProvider();
    }

    @Test
    public void stubUser() {
        assertThat(_userManager.getUser("test"), nullValue());
        assertThat(_userManager.getAllUsers().size(), is(0));

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubUser("test", "", op1, op2).of(_userManager);
        User test = _userManager.getUser("test");
        verify(op1, Mockito.times(1)).of(test);
        verify(op2, Mockito.times(1)).of(test);
        assertThat(test, notNullValue());
        assertThat(test.getName(), is("test"));
        assertThat(test.getPassword(), nullValue());
        assertThat(_userManager.getAllUsers().size(), is(1));
        assertThat(_userManager.getAllUsers().iterator().next(), is(test));
        assertThat(_userManager.getUserById(test.getIdentifier()), is(test));
    }

    @Test
    public void stubSystemUser() {
        assertThat(_userManager.getUser(UserManager.SYSTEM_USER), nullValue());
        assertThat(_userManager.getAllUsers().size(), is(0));

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubSystemUser(op1, op2).of(_userManager);
        User superuser = _userManager.getSystemUser();
        verify(op1, Mockito.times(1)).of(superuser);
        verify(op2, Mockito.times(1)).of(superuser);
        assertThat(superuser, notNullValue());
        assertThat(superuser.getName(), is(UserManager.SYSTEM_USER));
        assertThat(superuser.getPassword(), is(UserManager.SYSTEM_USER));
        assertThat(_userManager.getAllUsers().size(), is(1));
        assertThat(_userManager.getAllUsers().iterator().next(), is(superuser));
        assertThat(_userManager.getUserById(superuser.getIdentifier()), is(superuser));
    }

    @Test
    public void stubAnonymousUser() {
        assertThat(_userManager.getUser(UserManager.ANONYMOUS_USER), nullValue());
        assertThat(_userManager.getAllUsers().size(), is(0));

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubAnonymousUser(op1, op2).of(_userManager);
        User anonymous = _userManager.getAnonymousUser();
        verify(op1, Mockito.times(1)).of(anonymous);
        verify(op2, Mockito.times(1)).of(anonymous);
        assertThat(anonymous, notNullValue());
        assertThat(anonymous.getName(), is(UserManager.ANONYMOUS_USER));
        assertThat(anonymous.getPassword(), nullValue());
        assertThat(_userManager.getAllUsers().size(), is(1));
        assertThat(_userManager.getAllUsers().iterator().next(), is(anonymous));
        assertThat(_userManager.getUserById(anonymous.getIdentifier()), is(anonymous));
    }

    @Test
    public void stubMultipleUsers() {
        assertThat(_userManager.getAllUsers().size(), is(0));

        UserManagerStubbingOperation.stubAnonymousUser().of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(1));

        UserManagerStubbingOperation.stubSystemUser().of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(2));

        UserManagerStubbingOperation.stubUser("Tom", null).of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(3));

        // test for no dublettes in user set
        UserManagerStubbingOperation.stubUser("Tom", null).of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(3));
    }

    @Test
    public void stubAllUsers() {
        assertThat(_userManager.getAllUsers().size(), is(0));

        User tom = createPlainUserMock("Tom", UUID.randomUUID().toString());
        User huck = createPlainUserMock("Huck", UUID.randomUUID().toString());
        User betty = createPlainUserMock("Betty", UUID.randomUUID().toString());
        UserManagerStubbingOperation.stubAllUsers(Set.of(tom, huck, betty)).of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(3));
        assertThat(_userManager.getUser("Tom"), is(tom));
        assertThat(_userManager.getUser("Huck"), is(huck));
        assertThat(_userManager.getUser("Betty"), is(betty));
        assertThat(_userManager.getUserById(tom.getIdentifier()), is(tom));
        assertThat(_userManager.getUserById(huck.getIdentifier()), is(huck));
        assertThat(_userManager.getUserById(betty.getIdentifier()), is(betty));

        User newTom = createPlainUserMock("Tom", UUID.randomUUID().toString());
        User jerry = createPlainUserMock("Jerry", UUID.randomUUID().toString());
        UserManagerStubbingOperation.stubAllUsers(Set.of(newTom, jerry)).of(_userManager);
        assertThat(_userManager.getAllUsers().size(), is(2));
        assertThat(_userManager.getUser("Tom"), is(newTom));
        assertThat(_userManager.getUser("Huck"), nullValue());
        assertThat(_userManager.getUser("Betty"), nullValue());
        assertThat(_userManager.getUser("Jerry"), is(jerry));
        assertThat(_userManager.getUserById(tom.getIdentifier()), nullValue());
        assertThat(_userManager.getUserById(huck.getIdentifier()), nullValue());
        assertThat(_userManager.getUserById(betty.getIdentifier()), nullValue());
        assertThat(_userManager.getUserById(newTom.getIdentifier()), is(newTom));
        assertThat(_userManager.getUserById(jerry.getIdentifier()), is(jerry));
    }

    @Test
    public void stubLockTimePeriod() {
        assertThat(_userManager.getLockTimePeriod(), is(0));

        UserManagerStubbingOperation.stubLockTimePeriod(12).of(_userManager);
        assertThat(_userManager.getLockTimePeriod(), is(12));
    }

    @Test
    public void stubMaxFailedLoginAttempts() {
        assertThat(_userManager.getMaxFailedLoginAttempts(), is(0));

        UserManagerStubbingOperation.stubMaxFailedLoginAttempts(3).of(_userManager);
        assertThat(_userManager.getMaxFailedLoginAttempts(), is(3));
    }

    private User createPlainUserMock(String name, String identifier) {
        User result = mock(User.class);
        when(result.getName()).thenReturn(name);
        when(result.getIdentifier()).thenReturn(identifier);
        return result;
    }
}
