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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing SecurityMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-03
 */
public class SecurityMockUtilsTest {

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockAccessManagerWebsiteRepositoryTest() throws RepositoryException {
        assertFalse(MgnlContext.hasInstance());

        AccessManagerStubbingOperation op1 = mock(AccessManagerStubbingOperation.class);
        AccessManagerStubbingOperation op2 = mock(AccessManagerStubbingOperation.class);
        AccessManager am = mockAccessManager(op1, op2);

        verify(op1, times(1)).of(am);
        verify(op2, times(1)).of(am);

        assertTrue(MgnlContext.hasInstance());
        assertEquals(am, MgnlContext.getAccessManager(WEBSITE));
    }

    @Test
    public void mockAccessManagerTestForNull() {
        assertThrows(IllegalArgumentException.class, () -> mockAccessManager(null));
    }

    @Test
    public void testMockSecuritySupport() {
        SecuritySupport support = mockSecuritySupport();
        assertNotNull(support);

        // check that we get same instance again
        assertEquals(support, mockSecuritySupport());

        // check that mock is available through Components:
        assertEquals(support, getComponentSingleton(SecuritySupport.class));
    }

    @Test
    public void testMockUserManager() {
        SecuritySupport support = getComponentSingleton(SecuritySupport.class);
        assertNull(support);

        UserManager manager = mockUserManager("test");
        Require.Argument.notNull(manager, "manager should not be null");
        assertNotNull(manager.getAllUsers());
        assertEquals(0, manager.getAllUsers().size());
        assertNull(manager.getAnonymousUser());
        assertNull(manager.getSystemUser());

        support = getComponentSingleton(SecuritySupport.class);
        assertNotNull(support);
        assertEquals(manager, support.getUserManager("test"));
    }

    @Test
    public void testMockUser() {
        UserStubbingOperation op = mock(UserStubbingOperation.class);
        User user = mockUser("test", op);
        assertNotNull(user);
        verify(op, times(1)).of(user);
        assertEquals("test", user.getName());
        assertEquals(36, user.getIdentifier().length());

        // Repeated mocking of same user results in same user object:
        User user2 = mockUser("test");
        assertEquals(user, user2);

        // text implicit mocking of GroupManager:
        assertEquals(user, mockUserManager(WEBSITE).getUser("test"));
        assertEquals(user, mockUserManager(WEBSITE).getUserById(user.getIdentifier()));
    }

    @Test
    public void testMockGroup() throws AccessDeniedException {
        GroupStubbingOperation op = mock(GroupStubbingOperation.class);
        Group group = mockGroup("test", op);
        assertNotNull(group);
        verify(op, times(1)).of(group);
        assertEquals("test", group.getName());
        assertEquals(36, group.getId().length());

        // Repeated mocking of same group results in same group object:
        Group group2 = mockGroup("test");
        assertEquals(group, group2);

        // text implicit mocking of GroupManager:
        assertEquals(group, mockGroupManager().getGroup("test"));
    }

    @Test
    public void testMockRole() {
        Role role = mockRole("test");
        assertNotNull(role);
        assertEquals("test", role.getName());
        assertEquals(36, role.getId().length());

        // Repeated mocking of same role results in same role object:
        Role role2 = mockRole("test");
        assertEquals(role, role2);

        // text implicit mocking of RoleManager:
        assertEquals(role, mockRoleManager().getRole("test"));
        assertEquals("test", mockRoleManager().getRoleNameById(role.getId()));
    }
}
