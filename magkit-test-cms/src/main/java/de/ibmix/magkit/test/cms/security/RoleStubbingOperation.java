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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.Role;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * The StubbingOperations to define the behaviour of info.magnolia.cms.security.Role mocks.
 * Used as parameter in SecurityMockUtils.mockRole(...)
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class RoleStubbingOperation implements StubbingOperation<Role> {

    public static RoleStubbingOperation stubbName(final String name) {
        return new RoleStubbingOperation() {
            @Override
            public void of(Role role) {
                assertThat(role, notNullValue());
                doReturn(name).when(role).getName();
            }
        };
    }

    public static RoleStubbingOperation stubbId(final String uuid) {
        return new RoleStubbingOperation() {
            @Override
            public void of(Role role) {
                assertThat(role, notNullValue());
                doReturn(uuid).when(role).getId();
            }
        };
    }

    private RoleStubbingOperation() {}
}
