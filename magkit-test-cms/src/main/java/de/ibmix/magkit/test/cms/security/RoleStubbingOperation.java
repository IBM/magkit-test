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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.security.Role;

import static org.mockito.Mockito.doReturn;

/**
 * Factory holder for creating {@link Role} related {@link StubbingOperation}s.<br>
 * <p>
 * Supplies small, focused Mockito stubbing operations for configuring a {@link Role} mock in tests while keeping
 * test code expressive and free from repetitive mocking boilerplate. Often used together with
 * {@code SecurityMockUtils.mockRole(...)} or applied directly via {@code op.of(roleMock)}.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All methods return non-null operations.</li>
 *   <li>Operations assert non-null target roles at execution time (throwing {@link IllegalArgumentException} if violated).</li>
 *   <li>Stateless – no shared mutable data.</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *   Role r = Mockito.mock(Role.class);
 *   RoleStubbingOperation.stubName("editors").of(r);
 *   RoleStubbingOperation.stubId("123").of(r);
 * </pre>
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-06-16
 */
public abstract class RoleStubbingOperation implements StubbingOperation<Role> {

    /**
     * Stubs {@link Role#getName()}.
     *
     * @param name name to return (may be null)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null role
     */
    public static RoleStubbingOperation stubName(final String name) {
        return new RoleStubbingOperation() {
            @Override
            public void of(Role role) {
                Require.Argument.notNull(role, "role should not be null");
                doReturn(name).when(role).getName();
            }
        };
    }

    /**
     * Stubs {@link Role#getId()}.
     *
     * @param uuid id / identifier (may be null)
     * @return stubbing operation
     * @throws IllegalArgumentException if executed with null role
     */
    public static RoleStubbingOperation stubId(final String uuid) {
        return new RoleStubbingOperation() {
            @Override
            public void of(Role role) {
                Require.Argument.notNull(role, "role should not be null");
                doReturn(uuid).when(role).getId();
            }
        };
    }

    private RoleStubbingOperation() {}
}
