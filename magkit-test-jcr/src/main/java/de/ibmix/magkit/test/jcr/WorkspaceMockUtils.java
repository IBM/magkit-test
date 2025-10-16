package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
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

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubName;
import static org.mockito.Mockito.mock;

/**
 * Utility factory for creating Mockito-based {@link Workspace} mocks for unit tests.
 * <p>
 * This class centralizes common default stubbing for JCR {@link Workspace} objects to reduce duplication
 * and improve readability in test code. It provides overloaded {@code mockWorkspace} factory methods that:
 * </p>
 * <ul>
 *     <li>Create a Mockito mock of {@link Workspace}.</li>
 *     <li>Always stub the workspace name (default: {@code "test"}).</li>
 *     <li>Apply any additional {@link WorkspaceStubbingOperation}s passed via varargs, in the given order.</li>
 * </ul>
 * <h3>Usage Example</h3>
 * <pre>{@code
 * import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubName;
 *
 * Workspace ws = WorkspaceMockUtils.mockWorkspace(
 *     // additional stubbing operations
 *     stubName("preview")
 * );
 *
 * Workspace custom = WorkspaceMockUtils.mockWorkspace("edit", otherOperation1(), otherOperation2());
 * }</pre>
 * <p>
 * The methods never return {@code null}. A {@link RepositoryException} is propagated if any provided stubbing
 * operation throws it. The factory itself is stateless and therefore thread-safe; the returned mock follows
 * Mockito's usual thread-safety characteristics (not guaranteed for concurrent mutation).
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public final class WorkspaceMockUtils {

    /**
     * Hidden constructor to prevent instantiation.
     */
    private WorkspaceMockUtils() {
    }

    /**
     * Create a {@link Workspace} Mockito mock with the default workspace name {@code "test"}.
     * <p>
     * Additional custom stubbing operations can be supplied; they are executed in the order provided.
     * </p>
     *
     * @param stubbings optional additional stubbing operations; may be empty but never {@code null}
     * @return the configured {@link Workspace} mock (never {@code null})
     * @throws RepositoryException if any stubbing operation throws it
     * @see #mockWorkspace(String, WorkspaceStubbingOperation...)
     */
    public static Workspace mockWorkspace(WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        return mockWorkspace("test", stubbings);
    }

    /**
     * Create a {@link Workspace} Mockito mock with a specific workspace name.
     * <p>
     * Validates that {@code name} is not blank. The name is applied first, then all additional stubbing
     * operations are executed in the order provided.
     * </p>
     *
     * @param name non-blank workspace name to be returned by {@link Workspace#getName()}; must not be blank
     * @param stubbings optional additional stubbing operations; may be empty but never {@code null}
     * @return the configured {@link Workspace} mock (never {@code null})
     * @throws RepositoryException if any stubbing operation throws it
     * @throws AssertionError if {@code name} is blank
     */
    public static Workspace mockWorkspace(String name, WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notBlank(name, "name must not be blank");
        Require.Argument.notNull(stubbings, "stubbings must not be null");
        Workspace result = mock(Workspace.class);
        stubName(name).of(result);
        for (WorkspaceStubbingOperation stub : stubbings) {
            stub.of(result);
        }
        return result;
    }
}
