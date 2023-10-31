package de.ibmix.magkit.test.cms.security;

import info.magnolia.cms.security.Role;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public class RoleStubbingOperationTest {

    private Role _role;
    @Before
    public void setUp() throws Exception {
        _role = mock(Role.class);
    }

    @Test
    public void stubName() {
        assertThat(_role.getName(), nullValue());

        RoleStubbingOperation.stubName("test").of(_role);
        assertThat(_role.getName(), is("test"));
    }

    @Test
    public void stubId() {
        assertThat(_role.getId(), nullValue());

        RoleStubbingOperation.stubId("test").of(_role);
        assertThat(_role.getId(), is("test"));
    }
}
