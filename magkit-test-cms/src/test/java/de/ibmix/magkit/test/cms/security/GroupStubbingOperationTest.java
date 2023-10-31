package de.ibmix.magkit.test.cms.security;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class GroupStubbingOperationTest {

    private Group _group;

    @Before
    public void setUp() throws Exception {
        _group = mock(Group.class);
    }

    @Test
    public void stubName() {
        assertThat(_group.getName(), nullValue());

        GroupStubbingOperation.stubName("test").of(_group);
        assertThat(_group.getName(), is("test"));
    }

    @Test
    public void stubId() {
        assertThat(_group.getId(), nullValue());

        GroupStubbingOperation.stubId("test").of(_group);
        assertThat(_group.getId(), is("test"));
    }

    @Test
    public void stubProperty() {
        assertThat(_group.getProperty("name"), nullValue());

        GroupStubbingOperation.stubProperty("name", "value").of(_group);
        assertThat(_group.getProperty("name"), is("value"));
    }

    @Test
    public void stubGroups() {
        assertThat(_group.getGroups().isEmpty(), is(true));

        GroupStubbingOperation.stubGroups("g1", "g2").of(_group);
        assertThat(_group.getGroups().size(), is(2));
        assertThat(_group.getAllGroups().isEmpty(), is(true));
    }

    @Test
    public void stubAllGroups() {
        assertThat(_group.getAllGroups().isEmpty(), is(true));

        GroupStubbingOperation.stubAllGroups("g1", "g2").of(_group);
        assertThat(_group.getAllGroups().size(), is(2));
        assertThat(_group.getGroups().isEmpty(), is(true));
    }

    @Test
    public void stubRoles() throws AccessDeniedException {
        assertThat(_group.getRoles().isEmpty(), is(true));

        GroupStubbingOperation.stubRoles(null).of(_group);
        assertThat(_group.getRoles().isEmpty(), is(true));

        GroupStubbingOperation.stubRoles("r1", "r2").of(_group);
        assertThat(_group.getRoles().size(), is(2));
        assertThat(_group.hasRole("r1"), is(true));
        assertThat(_group.hasRole("r2"), is(true));

        GroupStubbingOperation.stubRoles().of(_group);
        assertThat(_group.getRoles().isEmpty(), is(true));
    }
}
