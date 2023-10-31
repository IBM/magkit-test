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

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.Group;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test GroupStubbingOperation.
 *
 * @author wolf.bubenik@ibmic.de
 * @since 2023-10-31
 */
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
