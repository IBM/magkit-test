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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.getComponentSingleton;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockAccessManager;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockGroup;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockGroupManager;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockRole;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockRoleManager;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockSecuritySupport;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUser;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockUserManager;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.register;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing SecurityMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-03
 */
public class SecurityMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockAccessManagerWebsiteRepositoryTest() throws RepositoryException {
        assertThat(MgnlContext.hasInstance(), is(false));

        AccessManagerStubbingOperation op1 = mock(AccessManagerStubbingOperation.class);
        AccessManagerStubbingOperation op2 = mock(AccessManagerStubbingOperation.class);
        AccessManager am = mockAccessManager(op1, op2);

        verify(op1, times(1)).of(am);
        verify(op2, times(1)).of(am);

        assertThat(MgnlContext.hasInstance(), is(true));
        assertThat(MgnlContext.getAccessManager(WEBSITE), is(am));
    }

    @Test(expected = AssertionError.class)
    public void mockAccessManagerTestForNull() throws RepositoryException {
        mockAccessManager(null);
    }

    @Test
    public void testMockSecuritySupport() {
        SecuritySupport support = mockSecuritySupport();
        assertThat(support, notNullValue());

        // check that we get same instance again
        assertThat(mockSecuritySupport(), is(support));

        // check that mock is available through Components:
        assertThat(getComponentSingleton(SecuritySupport.class), is(support));
    }

    @Test
    public void testMockUserManager() {
        SecuritySupport support = getComponentSingleton(SecuritySupport.class);
        assertThat(support, nullValue());

        UserManager manager = mockUserManager("test");
        assertThat(manager, notNullValue());

        support = getComponentSingleton(SecuritySupport.class);
        assertThat(support, notNullValue());

        assertThat(support.getUserManager("test"), is(manager));
    }

    @Test
    public void testRegister() {
        UserManager manager = mockUserManager("test");
        User fritz = mock(User.class);
        when(fritz.getName()).thenReturn("Fritz");

        assertThat(manager.getUser("Fritz"), nullValue());

        register("test", fritz);
        assertThat(manager.getUser("Fritz"), is(fritz));
    }

    @Test
    public void testMockUser() {
        UserStubbingOperation op = mock(UserStubbingOperation.class);
        User user = mockUser("test", op);
        assertThat(user, notNullValue());
        verify(op, times(1)).of(user);
        assertThat(user.getName(), is("test"));
        assertThat(user.getIdentifier().length(), is(36));

        // Repeated mocking of same user results in same user object:
        User user2 = mockUser("test");
        assertThat(user2, is(user));

        // text implicit mocking of GroupManager:
        assertThat(mockUserManager(WEBSITE).getUser("test"), is(user));
        assertThat(mockUserManager(WEBSITE).getUserById(user.getIdentifier()), is(user));
    }

    @Test
    public void testMockGroup() throws AccessDeniedException {
        GroupStubbingOperation op = mock(GroupStubbingOperation.class);
        Group group = mockGroup("test", op);
        assertThat(group, notNullValue());
        verify(op, times(1)).of(group);
        assertThat(group.getName(), is("test"));
        assertThat(group.getId().length(), is(36));

        // Repeated mocking of same group results in same group object:
        Group group2 = mockGroup("test");
        assertThat(group2, is(group));

        // text implicit mocking of GroupManager:
        assertThat(mockGroupManager().getGroup("test"), is(group));
    }

    @Test
    public void testMockRole() {
        Role role = mockRole("test");
        assertThat(role, notNullValue());
        assertThat(role.getName(), is("test"));
        assertThat(role.getId().length(), is(36));

        // Repeated mocking of same role results in same role object:
        Role role2 = mockRole("test");
        assertThat(role2, is(role));

        // text implicit mocking of RoleManager:
        assertThat(mockRoleManager().getRole("test"), is(role));
        assertThat(mockRoleManager().getRoleNameById(role.getId()), is("test"));
    }
}
