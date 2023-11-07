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

import info.magnolia.cms.security.Role;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Test RoleStubbingOperation.
 *
 * @author wolf.bubenik@ibmic.de
 * @since 2023-10-31
 */
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
