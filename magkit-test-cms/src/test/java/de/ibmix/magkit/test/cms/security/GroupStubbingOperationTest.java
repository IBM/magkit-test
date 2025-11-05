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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test GroupStubbingOperation.
 *
 * @author wolf.bubenik@ibmic.de
 * @since 2023-10-31
 */
public class GroupStubbingOperationTest {

    private Group _group;

    @BeforeEach
    public void setUp() throws Exception {
        _group = mock(Group.class);
    }

    @Test
    public void stubName() {
        assertNull(_group.getName());

        GroupStubbingOperation.stubName("test").of(_group);
        assertEquals("test", _group.getName());
    }

    @Test
    public void stubId() {
        assertNull(_group.getId());

        GroupStubbingOperation.stubId("test").of(_group);
        assertEquals("test", _group.getId());
    }

    @Test
    public void stubProperty() {
        assertNull(_group.getProperty("name"));

        GroupStubbingOperation.stubProperty("name", "value").of(_group);
        assertEquals("value", _group.getProperty("name"));
    }

    @Test
    public void stubGroups() {
        assertTrue(_group.getGroups().isEmpty());

        GroupStubbingOperation.stubGroups("g1", "g2").of(_group);
        assertEquals(2, _group.getGroups().size());
        assertTrue(_group.getAllGroups().isEmpty());
    }

    @Test
    public void stubAllGroups() {
        assertTrue(_group.getAllGroups().isEmpty());

        GroupStubbingOperation.stubAllGroups("g1", "g2").of(_group);
        assertEquals(2, _group.getAllGroups().size());
        assertTrue(_group.getGroups().isEmpty());
    }

    @Test
    public void stubRoles() throws AccessDeniedException {
        assertTrue(_group.getRoles().isEmpty());

        GroupStubbingOperation.stubRoles(null).of(_group);
        assertTrue(_group.getRoles().isEmpty());

        GroupStubbingOperation.stubRoles("r1", "r2").of(_group);
        assertEquals(2, _group.getRoles().size());
        assertTrue(_group.hasRole("r1"));
        assertTrue(_group.hasRole("r2"));

        GroupStubbingOperation.stubRoles().of(_group);
        assertTrue(_group.getRoles().isEmpty());
    }
}
