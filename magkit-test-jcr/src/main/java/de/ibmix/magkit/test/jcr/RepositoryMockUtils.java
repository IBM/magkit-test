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
 * #L% */

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

import static org.mockito.Mockito.mock;

/**
 * Utility class for creating and reusing a Mockito mock of a {@link javax.jcr.Repository} within the scope of the current thread.
 * <p>
 * The repository mock instance is stored in a {@link ThreadLocal}. The first call to {@link #mockRepository(RepositoryStubbingOperation...)} in a thread
 * creates a new mock and applies all provided {@link RepositoryStubbingOperation}s (stubbing operations). Subsequent calls in the same thread return
 * the same mock instance and will apply any additionally provided stubbings on top of the already configured mock.
 * </p>
 * <p>
 * This design allows tests to build up JCR behavior incrementally across helper methods without having to pass the mock around explicitly.
 * </p>
 * <p><strong>Lifecycle / Isolation:</strong><br>
 * Because the mock is {@code ThreadLocal}, parallel test execution will not share the same mock instance across threads. However, consecutive test
 * methods executed on the same thread will reuse the previously created mock unless {@link #cleanRepository()} is called. To guarantee isolation
 * between logically independent tests, invoke {@code cleanRepository()} (e.g. in an {@code @AfterEach} method) so that a fresh mock (including
 * fresh associated mocked objects like Session, Workspace, Node, ...) is created for the next test.
 * </p>
 * <p><strong>Typical usage:</strong></p>
 * <pre>{@code
 * @BeforeEach
 * void setUp() throws RepositoryException {
 *     // Ensure a clean mock per test
 *     RepositoryMockUtils.cleanRepository();
 *     Repository repository = RepositoryMockUtils.mockRepository(
 *         repo -> when(repo.getDescriptor(anyString())).thenReturn("value")
 *     );
 * }
 *
 * @Test
 * void myTest() throws RepositoryException {
 *     Repository repository = RepositoryMockUtils.mockRepository(); // returns the same thread-local instance
 *     // ... test assertions
 * }
 * }
 * </pre>
 * <p><strong>Thread safety:</strong><br>
 * Access to the repository mock is confined to the current thread only. No additional synchronization is required.
 * </p>
 * <p><strong>Resetting:</strong><br>
 * Use {@link #cleanRepository()} to discard the current thread's mock so the next call to {@link #mockRepository(RepositoryStubbingOperation...)} creates a new one.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-02-04
 */
public final class RepositoryMockUtils {

    /**
     * Hidden constructor to prevent instantiation.
     */
    private RepositoryMockUtils() { /* utility class */ }

    private static final ThreadLocal<Repository> REPOSITORY = new ThreadLocal<>();

    /**
     * Obtain the {@link Repository} mock for the current thread, creating it if necessary, and apply the provided stubbing operations.
     * <p>
     * If a mock already exists in the current thread (from a previous call), that instance is reused and the given stubbings are executed on it.
     * This enables incremental configuration. To force creation of a brand-new mock (clearing previous stubbings), call {@link #cleanRepository()} first.
     * </p>
     *
     * @param stubbings optional sequence of {@link RepositoryStubbingOperation} lambdas / implementations that perform Mockito stubbing on the mock
     * @return the thread-local {@link Repository} mock instance (never {@code null})
     * @throws RepositoryException if a stubbing operation signals a repository problem (pass-through from custom stubbing implementations)
     */
    public static Repository mockRepository(RepositoryStubbingOperation... stubbings) throws RepositoryException {
        Repository result = REPOSITORY.get();
        if (result == null) {
            result = mock(Repository.class);
            REPOSITORY.set(result);
        }
        for (RepositoryStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Discard (unset) the current thread's repository mock so that the next call to {@link #mockRepository(RepositoryStubbingOperation...)} creates a new one.
     * <p>
     * Recommended to call in a test framework teardown hook (e.g. {@code @AfterEach}) to guarantee test isolation.
     * This method affects only the calling thread; other threads keep their own mock instances.
     * </p>
     */
    public static void cleanRepository() {
        REPOSITORY.set(null);
    }
}
