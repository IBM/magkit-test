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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserManagerStubbingOperationTest {

    private UserManager _userManager;

    @BeforeEach
    public void setUp() throws Exception {
        // be paranoid
        ComponentsMockUtils.clearComponentProvider();
        _userManager = SecurityMockUtils.mockUserManager("test");
    }

    @AfterEach
    public void tearDown() throws Exception {
        // be polite
        ComponentsMockUtils.clearComponentProvider();
    }

    @Test
    public void stubUser() {
        assertNull(_userManager.getUser("test"));
        assertEquals(0, _userManager.getAllUsers().size());

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubUser("test", "", op1, op2).of(_userManager);
        User test = _userManager.getUser("test");
        verify(op1, Mockito.times(1)).of(test);
        verify(op2, Mockito.times(1)).of(test);
        assertNotNull(test);
        assertEquals("test", test.getName());
        assertNull(test.getPassword());
        assertEquals(1, _userManager.getAllUsers().size());
        assertEquals(test, _userManager.getAllUsers().iterator().next());
        assertEquals(test, _userManager.getUserById(test.getIdentifier()));
    }

    @Test
    public void stubSystemUser() {
        assertNull(_userManager.getUser(UserManager.SYSTEM_USER));
        assertEquals(0, _userManager.getAllUsers().size());

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubSystemUser(op1, op2).of(_userManager);
        User superuser = _userManager.getSystemUser();
        verify(op1, Mockito.times(1)).of(superuser);
        verify(op2, Mockito.times(1)).of(superuser);
        assertNotNull(superuser);
        assertEquals(UserManager.SYSTEM_USER, superuser.getName());
        assertEquals(UserManager.SYSTEM_USER, superuser.getPassword());
        assertEquals(1, _userManager.getAllUsers().size());
        assertEquals(superuser, _userManager.getAllUsers().iterator().next());
        assertEquals(superuser, _userManager.getUserById(superuser.getIdentifier()));
    }

    @Test
    public void stubAnonymousUser() {
        assertNull(_userManager.getUser(UserManager.ANONYMOUS_USER));
        assertEquals(0, _userManager.getAllUsers().size());

        UserStubbingOperation op1 = mock(UserStubbingOperation.class);
        UserStubbingOperation op2 = mock(UserStubbingOperation.class);
        UserManagerStubbingOperation.stubAnonymousUser(op1, op2).of(_userManager);
        User anonymous = _userManager.getAnonymousUser();
        verify(op1, Mockito.times(1)).of(anonymous);
        verify(op2, Mockito.times(1)).of(anonymous);
        assertNotNull(anonymous);
        assertEquals(UserManager.ANONYMOUS_USER, anonymous.getName());
        assertNull(anonymous.getPassword());
        assertEquals(1, _userManager.getAllUsers().size());
        assertEquals(anonymous, _userManager.getAllUsers().iterator().next());
        assertEquals(anonymous, _userManager.getUserById(anonymous.getIdentifier()));
    }

    @Test
    public void stubMultipleUsers() {
        assertEquals(0, _userManager.getAllUsers().size());

        UserManagerStubbingOperation.stubAnonymousUser().of(_userManager);
        assertEquals(1, _userManager.getAllUsers().size());

        UserManagerStubbingOperation.stubSystemUser().of(_userManager);
        assertEquals(2, _userManager.getAllUsers().size());

        UserManagerStubbingOperation.stubUser("Tom", null).of(_userManager);
        assertEquals(3, _userManager.getAllUsers().size());

        // test for no dublettes in user set
        UserManagerStubbingOperation.stubUser("Tom", null).of(_userManager);
        assertEquals(3, _userManager.getAllUsers().size());
    }

    @Test
    public void stubAllUsers() {
        assertEquals(0, _userManager.getAllUsers().size());

        User tom = createPlainUserMock("Tom", UUID.randomUUID().toString());
        User huck = createPlainUserMock("Huck", UUID.randomUUID().toString());
        User betty = createPlainUserMock("Betty", UUID.randomUUID().toString());
        UserManagerStubbingOperation.stubAllUsers(Set.of(tom, huck, betty)).of(_userManager);
        assertEquals(3, _userManager.getAllUsers().size());
        assertEquals(tom, _userManager.getUser("Tom"));
        assertEquals(huck, _userManager.getUser("Huck"));
        assertEquals(betty, _userManager.getUser("Betty"));
        assertEquals(tom, _userManager.getUserById(tom.getIdentifier()));
        assertEquals(huck, _userManager.getUserById(huck.getIdentifier()));
        assertEquals(betty, _userManager.getUserById(betty.getIdentifier()));

        User newTom = createPlainUserMock("Tom", UUID.randomUUID().toString());
        User jerry = createPlainUserMock("Jerry", UUID.randomUUID().toString());
        UserManagerStubbingOperation.stubAllUsers(Set.of(newTom, jerry)).of(_userManager);
        assertEquals(2, _userManager.getAllUsers().size());
        assertEquals(newTom, _userManager.getUser("Tom"));
        assertNull(_userManager.getUser("Huck"));
        assertNull(_userManager.getUser("Betty"));
        assertEquals(jerry, _userManager.getUser("Jerry"));
        assertNull(_userManager.getUserById(tom.getIdentifier()));
        assertNull(_userManager.getUserById(huck.getIdentifier()));
        assertNull(_userManager.getUserById(betty.getIdentifier()));
        assertEquals(newTom, _userManager.getUserById(newTom.getIdentifier()));
        assertEquals(jerry, _userManager.getUserById(jerry.getIdentifier()));
    }

    @Test
    public void stubLockTimePeriod() {
        assertEquals(0, _userManager.getLockTimePeriod());

        UserManagerStubbingOperation.stubLockTimePeriod(12).of(_userManager);
        assertEquals(12, _userManager.getLockTimePeriod());
    }

    @Test
    public void stubMaxFailedLoginAttempts() {
        assertEquals(0, _userManager.getMaxFailedLoginAttempts());

        UserManagerStubbingOperation.stubMaxFailedLoginAttempts(3).of(_userManager);
        assertEquals(3, _userManager.getMaxFailedLoginAttempts());
    }

    private User createPlainUserMock(String name, String identifier) {
        User result = mock(User.class);
        when(result.getName()).thenReturn(name);
        when(result.getIdentifier()).thenReturn(identifier);
        return result;
    }
}
