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

import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Factory and base type for {@code Repository} related stubbing operations.
 * <p>
 * A {@code RepositoryStubbingOperation} encapsulates a piece of Mockito stubbing logic that can be applied to a
 * {@link Repository} mock (usually the thread-local mock managed by {@link RepositoryMockUtils}). Implementations
 * are typically provided by static factory methods in this class. They enable concise, reusable and composable
 * stubbing of repository behavior in tests without scattering Mockito calls throughout test code.
 * </p>
 * <p><strong>Usage pattern:</strong>
 * <pre>{@code
 * Repository repo = RepositoryMockUtils.mockRepository(
 *     RepositoryStubbingOperation.stubLogin(mock(Session.class))
 * );
 * }
 * </pre>
 * Multiple operations can be passed to {@link RepositoryMockUtils#mockRepository(RepositoryStubbingOperation...)} and
 * will be executed in the given order, allowing incremental setup.
 * </p>
 * <p><strong>Error handling:</strong><br>
 * The functional method {@link ExceptionStubbingOperation#of(Repository)} of(Repository)} is allowed to throw {@link RepositoryException} so that stubbing
 * logic requiring repository related exceptions can be expressed directly.
 * </p>
 * <p><strong>Thread safety:</strong><br>
 * Operations themselves are usually stateless (returned as anonymous inner classes / lambdas) and thus thread-safe;
 * they act only on the passed mock instance.
 * </p>
 * <p><strong>Extensibility:</strong><br>
 * You can implement additional custom stubbing operations by subclassing this abstract class or providing a lambda
 * (since it is a SAM type through {@link ExceptionStubbingOperation}). Prefer adding new static factory methods here
 * for discoverability.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2014-02-04
 * @see RepositoryMockUtils
 */
public abstract class RepositoryStubbingOperation implements ExceptionStubbingOperation<Repository, RepositoryException> {

    /**
     * Create a {@code RepositoryStubbingOperation} that stubs the {@code login()} related methods of a {@link Repository} mock.
     * <p>
     * Behavior configured:
     * <ul>
     *   <li>{@code repository.login()} returns the provided {@code session}.</li>
     *   <li>If {@code session} is not {@code null}, {@code session.getRepository()} is stubbed to return the {@code repository}.</li>
     *   <li>If {@code session != null} and {@code session.getWorkspace() != null}, then
     *       {@code repository.login(session.getWorkspace().getName())} returns the same {@code session}.</li>
     * </ul>
     * </p>
     * <p><strong>Null handling:</strong><br>
     * Passing {@code null} as {@code session} stubs {@code repository.login()} to return {@code null}. Workspace name
     * based login will not be stubbed in that case.</p>
     * <p><strong>Typical use:</strong>
     * <pre>{@code
     * Session session = mock(Session.class);
     * when(session.getWorkspace()).thenReturn(mock(Workspace.class));
     * Repository repo = RepositoryMockUtils.mockRepository(
     *     RepositoryStubbingOperation.stubLogin(session)
     * );
     * // repo.login() now yields 'session'
     * }</pre>
     * </p>
     * <p><strong>Composition:</strong><br>
     * This operation can be combined with other repository stubbing operations in a single call to
     * {@link RepositoryMockUtils#mockRepository(RepositoryStubbingOperation...)} to build up richer behavior.</p>
     *
     * @param session the {@link Session} to be returned by {@code login()} (may be {@code null})
     * @return non-null stubbing operation instance
     */
    public static RepositoryStubbingOperation stubLogin(final Session session) {
        return new RepositoryStubbingOperation() {
            @Override
            public void of(final Repository repository) throws RepositoryException {
                assertThat(repository, notNullValue());
                when(repository.login()).thenReturn(session);
                if (session != null) {
                    when(session.getRepository()).thenReturn(repository);
                    if (session.getWorkspace() != null) {
                        when(repository.login(session.getWorkspace().getName())).thenReturn(session);
                    }
                }
            }
        };
    }
}
