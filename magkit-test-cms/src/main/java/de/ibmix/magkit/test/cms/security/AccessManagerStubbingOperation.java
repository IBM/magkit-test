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
import info.magnolia.cms.security.AccessManager;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Factory holder for creating {@link AccessManager} related {@link StubbingOperation}s.<br>
 * <p>
 * Provides concise, composable Mockito stubbing operations to configure behaviour of an {@link AccessManager} mock
 * in tests without exposing Mockito invocation details at the call site. Intended for usage with helper methods like
 * {@code SecurityMockUtils.mockAccessManager(...)} or by directly applying the returned operation via
 * {@code operation.of(accessManagerMock)}.
 * </p>
 * <p>
 * Contract / guarantees:
 * </p>
 * <ul>
 *   <li>All factory methods return non-null operations.</li>
 *   <li>Execution validates required arguments via {@code assertThat} throwing {@link IllegalArgumentException} on failure.</li>
 *   <li>No persistent state is stored in this class; operations are stateless wrappers.</li>
 * </ul>
 * <p>Example:</p>
 * <pre>
 *   AccessManager am = SecurityMockUtils.mockAccessManager();
 *   AccessManagerStubbingOperation.stubPermissions("/content/foo", 0x1L, true).of(am);
 * </pre>
 * <p><b>Thread safety:</b> Stateless; individual operations rely on thread-safe mock usage in typical single-threaded tests.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-09-01
 */
public abstract class AccessManagerStubbingOperation implements StubbingOperation<AccessManager> {

    /**
     * Create an operation that stubs permission related responses for the given path.
     * <ul>
     *   <li>{@link AccessManager#getPermissions(String)} returns {@code permissions} for the resolved path key.</li>
     *   <li>{@link AccessManager#isGranted(String, long)} returns {@code isGranted} when invoked with the same path and permissions.</li>
     * </ul>
     * If {@code path} is blank, a generic matcher ({@code anyString()}) is used so the stubbing applies to any path queried.
     *
     * @param path        repository path to stub (blank to match any path)
     * @param permissions bit mask representing permissions
     * @param isGranted   result to return for {@code isGranted(path, permissions)}
     * @return non-null stubbing operation
     * @throws IllegalArgumentException if applied to a null {@link AccessManager}
     */
    public static AccessManagerStubbingOperation stubPermissions(final String path, final long permissions, final boolean isGranted) {
        return new AccessManagerStubbingOperation() {
            @Override
            public void of(AccessManager am) {
                Require.Argument.notNull(am, "accessManager should not be null");
                String pathKey = isBlank(path) ? anyString() : path;
                when(am.getPermissions(pathKey)).thenReturn(permissions);
                when(am.isGranted(eq(pathKey), eq(permissions))).thenReturn(isGranted);
            }
        };
    }
}
