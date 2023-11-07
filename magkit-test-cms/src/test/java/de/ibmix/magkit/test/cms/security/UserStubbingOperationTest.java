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
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test UserStubbingOperation.
 *
 * @author wolf.bubenik@ibmic.de
 * @since 2023-10-31
 */
public class UserStubbingOperationTest {

    private User _user;

    @Before
    public void setUp() throws Exception {
        _user = mock(User.class);
    }

    @Test
    public void stubName() {
        assertThat(_user.getName(), nullValue());

        UserStubbingOperation.stubName("test").of(_user);
        assertThat(_user.getName(), is("test"));
    }

    @Test
    public void stubIdentifier() {
        assertThat(_user.getIdentifier(), nullValue());

        UserStubbingOperation.stubIdentifier("test").of(_user);
        assertThat(_user.getIdentifier(), is("test"));
    }

    @Test
    public void stubPassword() {
        assertThat(_user.getPassword(), nullValue());

        UserStubbingOperation.stubPassword("test").of(_user);
        assertThat(_user.getPassword(), is("test"));
    }

    @Test
    public void stubLanguage() {
        assertThat(_user.getLanguage(), nullValue());

        UserStubbingOperation.stubLanguage("en").of(_user);
        assertThat(_user.getLanguage(), is("en"));
    }

    @Test
    public void stubEnabled() {
        assertThat(_user.isEnabled(), is(false));

        UserStubbingOperation.stubEnabled(true).of(_user);
        assertThat(_user.isEnabled(), is(true));
    }

    @Test
    public void stubProperty() {
        assertThat(_user.getProperty("name"), nullValue());

        UserStubbingOperation.stubProperty("name", "value").of(_user);
        assertThat(_user.getProperty("name"), is("value"));
    }

    @Test
    public void stubGroups() {
        assertThat(_user.getGroups().isEmpty(), is(true));

        UserStubbingOperation.stubGroups(null).of(_user);
        assertThat(_user.getGroups().isEmpty(), is(true));

        UserStubbingOperation.stubGroups("g1", "g2").of(_user);
        assertThat(_user.getGroups().size(), is(2));
        assertThat(_user.getAllGroups().isEmpty(), is(true));

        UserStubbingOperation.stubGroups().of(_user);
        assertThat(_user.getGroups().isEmpty(), is(true));
    }

    @Test
    public void stubAllGroups() {
        assertThat(_user.getAllGroups().isEmpty(), is(true));

        UserStubbingOperation.stubAllGroups(null).of(_user);
        assertThat(_user.getAllGroups().isEmpty(), is(true));

        UserStubbingOperation.stubAllGroups("g1", "g2").of(_user);
        assertThat(_user.getAllGroups().size(), is(2));
        assertThat(_user.getGroups().isEmpty(), is(true));

        UserStubbingOperation.stubAllGroups().of(_user);
        assertThat(_user.getAllGroups().isEmpty(), is(true));
    }

    @Test
    public void stubRoles() {
        assertThat(_user.getRoles().isEmpty(), is(true));

        UserStubbingOperation.stubRoles(null).of(_user);
        assertThat(_user.getRoles().isEmpty(), is(true));

        UserStubbingOperation.stubRoles("r1", "r2").of(_user);
        assertThat(_user.getRoles().size(), is(2));
        assertThat(_user.hasRole("r1"), is(true));
        assertThat(_user.hasRole("r2"), is(true));

        UserStubbingOperation.stubRoles().of(_user);
        assertThat(_user.getRoles().isEmpty(), is(true));
    }

    @Test
    public void stubAllRoles() {
        assertThat(_user.getAllRoles().isEmpty(), is(true));

        UserStubbingOperation.stubAllRoles(null).of(_user);
        assertThat(_user.getAllRoles().isEmpty(), is(true));

        UserStubbingOperation.stubAllRoles("r1", "r2").of(_user);
        assertThat(_user.getAllRoles().size(), is(2));
        assertThat(_user.hasRole("r1"), is(true));
        assertThat(_user.hasRole("r2"), is(true));

        UserStubbingOperation.stubAllRoles().of(_user);
        assertThat(_user.getAllRoles().isEmpty(), is(true));
    }
}
