package de.ibmix.magkit.test.cms.security;

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.Role;
import info.magnolia.cms.security.RoleManager;
import info.magnolia.cms.security.auth.ACL;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;

public abstract class RoleManagerStubbingOperation implements StubbingOperation<RoleManager> {

    public static RoleManagerStubbingOperation stubRole(final Role role) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(role, notNullValue());
                assertThat(role.getName(), notNullValue());
                String name = role.getName();
                doReturn(role).when(mock).getRole(name);
                if (isNotEmpty(role.getId())) {
                    stubRoleNameById(role.getId(), name).of(mock);
                }
            }
        };
    }

    public static RoleManagerStubbingOperation stubRoleNameById(final String id, final String name) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(id, notNullValue());
                doReturn(name).when(mock).getRoleNameById(id);
            }
        };
    }

    public static RoleManagerStubbingOperation stubAcl(final String role, final ACL acl) {
        return new RoleManagerStubbingOperation() {
            @Override
            public void of(RoleManager mock) {
                assertThat(mock, notNullValue());
                assertThat(role, notNullValue());
                assertThat(acl, notNullValue());
                Map<String, ACL> acls = mock.getACLs(role);
                acls.put(acl.getName(), acl);
                doReturn(acls).when(mock).getACLs(role);
            }
        };
    }
}
