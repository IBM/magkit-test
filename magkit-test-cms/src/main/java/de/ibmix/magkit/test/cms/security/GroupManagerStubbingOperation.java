package de.ibmix.magkit.test.cms.security;

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import info.magnolia.cms.security.GroupManager;
import info.magnolia.cms.security.auth.ACL;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;

public abstract class GroupManagerStubbingOperation implements StubbingOperation<GroupManager> {

    public static GroupManagerStubbingOperation stubGroup(final Group group) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(group, notNullValue());
                try {
                    doReturn(group).when(mock).getGroup(group.getName());
                    Collection<Group> allGroups = mock.getAllGroups();
                    if (!allGroups.contains(group)) {
                        allGroups.add(group);
                        doReturn(allGroups).when(mock).getAllGroups();
                    }
                } catch (AccessDeniedException e) {
                    // ignore, will not happen on mocks
                }
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllGroups(final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                groups.forEach(group -> stubGroup(group).of(mock));
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllSuperGroups(final String groupName, final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(groups).when(mock).getAllSuperGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubAllSubGroups(final String groupName, final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(groups).when(mock).getAllSubGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubDirectSubGroups(final String groupName, final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(groups).when(mock).getDirectSubGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubDirectSuperGroups(final String groupName, final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(groupName, notNullValue());
                doReturn(groups).when(mock).getDirectSuperGroups(groupName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubGroupsWithRole(final String roleName, final Collection<Group> groups) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(groups, notNullValue());
                assertThat(roleName, notNullValue());
                doReturn(groups).when(mock).getGroupsWithRole(roleName);
            }
        };
    }

    public static GroupManagerStubbingOperation stubAcl(final String groupName, ACL acl) {
        return new GroupManagerStubbingOperation() {
            @Override
            public void of(GroupManager mock) {
                assertThat(mock, notNullValue());
                assertThat(acl, notNullValue());
                assertThat(groupName, notNullValue());
                Map<String, ACL> acls = mock.getACLs(groupName);
                acls.put(acl.getName(), acl);
                doReturn(acls).when(mock).getACLs(groupName);
            }
        };
    }
}
