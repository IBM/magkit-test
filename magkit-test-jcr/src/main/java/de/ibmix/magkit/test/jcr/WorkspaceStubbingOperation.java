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
import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;

import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Declarative factory for creating {@link Workspace}-scoped stubbing operations used by
 * {@link WorkspaceMockUtils} and tests directly.
 * <p>
 * Each static factory method returns an immutable, single-use {@link WorkspaceStubbingOperation} implementation
 * (a small anonymous inner class) that applies a specific Mockito stubbing to a provided {@link Workspace} mock
 * when its {@link #of(Workspace)} method is invoked. Multiple operations can be combined (varargs) and are
 * executed in the order supplied by the caller, enabling fine-grained composition without duplicating code.
 * </p>
 * <h3>Design Rationale</h3>
 * <ul>
 *   <li>Promotes DRY test code by encapsulating recurring stubbing patterns (name, session, query manager, etc.).</li>
 *   <li>Improves readability via intention-revealing method names like {@code stubSession(...)}.</li>
 *   <li>Allows conditional creation of dependent mocks (e.g. sessions) only when needed.</li>
 * </ul>
 * <h3>Typical Usage</h3>
 * <pre>{@code
 * Workspace ws = WorkspaceMockUtils.mockWorkspace(
 *     WorkspaceStubbingOperation.stubName("edit"),
 *     WorkspaceStubbingOperation.stubSession( // configure a fresh session mock
 *         SessionStubbingOperation.stubUserId("author")
 *     )
 * );
 * }</pre>
 * <p>
 * All operations defensively assert that the target {@code Workspace} instance is non-null. Assertions are used
 * intentionally (instead of explicit exceptions) to fail fast in test environments. Operations that may throw
 * {@link RepositoryException} declare it so callers can propagate or rethrow as needed.
 * </p>
 * <p>
 * Thread-safety: The factory methods are stateless and thread-safe. The returned operations hold only the values
 * captured in their closure. Mockito mocks themselves are generally not designed for concurrent mutation and
 * should be confined to a single test thread unless explicitly synchronized.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public abstract class WorkspaceStubbingOperation implements ExceptionStubbingOperation<Workspace, RepositoryException> {

    private WorkspaceStubbingOperation() {
    }

    /**
     * Create a stubbing operation that defines the value returned by {@link Workspace#getName()}.
     * <p>
     * This operation simply configures Mockito to return the supplied {@code name} string.
     * No validation is performed here; callers may validate emptiness upstream if desired.
     * </p>
     *
     * @param name the workspace name to return (may be {@code null} if tests intentionally cover that case)
     * @return a non-null stubbing operation
     */
    public static WorkspaceStubbingOperation stubName(final String name) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) {
                Require.Argument.notNull(ws, "workspace must not be null");
                when(ws.getName()).thenReturn(name);
            }
        };
    }

    /**
     * Create a stubbing operation that associates an existing {@link Session} with a {@link Workspace} mock.
     * <p>
     * The following interactions are configured:
     * </p>
     * <ul>
     *   <li>{@code ws.getSession()} returns the provided {@code session}.</li>
     *   <li>{@code session.getWorkspace()} returns the same {@code ws} (bidirectional consistency).</li>
     * </ul>
     * <p>
     * This variant does not create a new session; it strictly binds the supplied one.
     * </p>
     *
     * @param session the session to bind (may be a Mockito mock or purpose-built test instance)
     * @return a non-null stubbing operation
     */
    public static WorkspaceStubbingOperation stubSession(final Session session) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) {
                Require.Argument.notNull(ws, "workspace must not be null");
                when(ws.getSession()).thenReturn(session);
                when(session.getWorkspace()).thenReturn(ws);
            }
        };
    }

    /**
     * Create a stubbing operation that applies one or more {@link SessionStubbingOperation}s to the
     * {@link Session} associated with the target {@link Workspace}.
     * <p>
     * Behavior:
     * </p>
     * <ol>
     *   <li>If {@code workspace.getSession()} already returns a non-null session, each provided
     *       {@code SessionStubbingOperation} is applied to that existing session.</li>
     *   <li>If no session is present, a new session mock is created via {@link SessionMockUtils#mockSession(String, SessionStubbingOperation...)}
     *       using the workspace name, then bound using {@link #stubSession(Session)} to keep the bidirectional link.</li>
     * </ol>
     * <p>
     * This guarantees a session is always available to subsequent operations. The workspace name is used as a
     * basis for the newly created session when necessary.
     * </p>
     *
     * @param stubbings zero or more session stubbing operations to apply (may be empty)
     * @return a non-null stubbing operation
     */
    public static WorkspaceStubbingOperation stubSession(final SessionStubbingOperation... stubbings) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace context) throws RepositoryException {
                Require.Argument.notNull(context, "workspace must not be null");
                Session s = context.getSession();
                if (s == null) {
                    s = mockSession(context.getName(), stubbings);
                    stubSession(s).of(context);
                } else {
                    for (SessionStubbingOperation stub : stubbings) {
                        stub.of(s);
                    }
                }
            }
        };
    }

    /**
     * Create a stubbing operation that assigns a {@link QueryManager} to the {@link Workspace} by configuring
     * {@link Workspace#getQueryManager()} to return the supplied instance.
     *
     * @param queryManager the query manager to associate (may be a mock or test double)
     * @return a non-null stubbing operation
     * @throws RepositoryException only declared for consistency with other operations (not thrown here)
     */
    public static WorkspaceStubbingOperation stubQueryManager(final QueryManager queryManager) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(final Workspace ws) throws RepositoryException {
                Require.Argument.notNull(ws, "workspace must not be null");
                when(ws.getQueryManager()).thenReturn(queryManager);
            }
        };
    }

    /**
     * Create a stubbing operation that assigns an {@link ObservationManager} to the {@link Workspace} by configuring
     * {@link Workspace#getObservationManager()} to return the supplied instance.
     * <p>
     * Mockito's {@code doReturn(...).when(ws).getObservationManager()} form is used to handle potential final / proxy
     * constraints cleanly.
     * </p>
     *
     * @param observationManager the observation manager to associate (may be a mock)
     * @return a non-null stubbing operation
     * @throws RepositoryException only declared for interface compatibility (not thrown here)
     */
    public static WorkspaceStubbingOperation stubObservationManager(final ObservationManager observationManager) {
        return new WorkspaceStubbingOperation() {
            @Override
            public void of(Workspace ws) throws RepositoryException {
                Require.Argument.notNull(ws, "workspace must not be null");
                doReturn(observationManager).when(ws).getObservationManager();
            }
        };
    }
}
