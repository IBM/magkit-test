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

import info.magnolia.cms.security.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test UserStubbingOperation.
 *
 * @author wolf.bubenik@ibmic.de
 * @since 2023-10-31
 */
public class UserStubbingOperationTest {

    private User _user;

    @BeforeEach
    public void setUp() throws Exception {
        _user = mock(User.class);
    }

    @Test
    public void stubName() {
        assertNull(_user.getName());

        UserStubbingOperation.stubName("test").of(_user);
        assertEquals("test", _user.getName());
    }

    @Test
    public void stubIdentifier() {
        assertNull(_user.getIdentifier());

        UserStubbingOperation.stubIdentifier("test").of(_user);
        assertEquals("test", _user.getIdentifier());
    }

    @Test
    public void stubPassword() {
        assertNull(_user.getPassword());

        UserStubbingOperation.stubPassword("test").of(_user);
        assertEquals("test", _user.getPassword());
    }

    @Test
    public void stubLanguage() {
        assertNull(_user.getLanguage());

        UserStubbingOperation.stubLanguage("en").of(_user);
        assertEquals("en", _user.getLanguage());
    }

    @Test
    public void stubEnabled() {
        assertFalse(_user.isEnabled());

        UserStubbingOperation.stubEnabled(true).of(_user);
        assertTrue(_user.isEnabled());
    }

    @Test
    public void stubProperty() {
        assertNull(_user.getProperty("name"));

        UserStubbingOperation.stubProperty("name", "value").of(_user);
        assertEquals("value", _user.getProperty("name"));
    }

    @Test
    public void stubGroups() {
        assertTrue(_user.getGroups().isEmpty());

        UserStubbingOperation.stubGroups(null).of(_user);
        assertTrue(_user.getGroups().isEmpty());

        UserStubbingOperation.stubGroups("g1", "g2").of(_user);
        assertEquals(2, _user.getGroups().size());
        assertTrue(_user.getAllGroups().isEmpty());

        UserStubbingOperation.stubGroups().of(_user);
        assertTrue(_user.getGroups().isEmpty());
    }

    @Test
    public void stubAllGroups() {
        assertTrue(_user.getAllGroups().isEmpty());

        UserStubbingOperation.stubAllGroups(null).of(_user);
        assertTrue(_user.getAllGroups().isEmpty());

        UserStubbingOperation.stubAllGroups("g1", "g2").of(_user);
        assertEquals(2, _user.getAllGroups().size());
        assertTrue(_user.getGroups().isEmpty());

        UserStubbingOperation.stubAllGroups().of(_user);
        assertTrue(_user.getAllGroups().isEmpty());
    }

    @Test
    public void stubRoles() {
        assertTrue(_user.getRoles().isEmpty());

        UserStubbingOperation.stubRoles(null).of(_user);
        assertTrue(_user.getRoles().isEmpty());

        UserStubbingOperation.stubRoles("r1", "r2").of(_user);
        assertEquals(2, _user.getRoles().size());
        assertTrue(_user.hasRole("r1"));
        assertTrue(_user.hasRole("r2"));

        UserStubbingOperation.stubRoles().of(_user);
        assertTrue(_user.getRoles().isEmpty());
    }

    @Test
    public void stubAllRoles() {
        assertTrue(_user.getAllRoles().isEmpty());

        UserStubbingOperation.stubAllRoles(null).of(_user);
        assertTrue(_user.getAllRoles().isEmpty());

        UserStubbingOperation.stubAllRoles("r1", "r2").of(_user);
        assertEquals(2, _user.getAllRoles().size());
        assertTrue(_user.hasRole("r1"));
        assertTrue(_user.hasRole("r2"));

        UserStubbingOperation.stubAllRoles().of(_user);
        assertTrue(_user.getAllRoles().isEmpty());
    }
}
