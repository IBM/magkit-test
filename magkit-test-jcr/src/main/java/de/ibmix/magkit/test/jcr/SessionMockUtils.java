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

import de.ibmix.magkit.assertions.Require;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static de.ibmix.magkit.test.jcr.RepositoryStubbingOperation.stubLogin;
import static de.ibmix.magkit.test.jcr.SessionStubbingOperation.stubRootNode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility / factory methods for creating Mockito based {@link Session} mocks for JCR related unit tests.
 * <p>
 * This class centralizes the boilerplate required to set up a {@code Session} including:
 * <ul>
 *     <li>Creation (or reuse) of a mocked {@link Repository} via {@link RepositoryMockUtils#mockRepository(RepositoryStubbingOperation...)}.</li>
 *     <li>Providing a synthetic root {@link Node} ("/") with a stable identifier and type ("rep:root").</li>
 *     <li>Convenience stubbing so that {@link Session#getProperty(String)} delegates to {@link Session#getItem(String)}.</li>
 *     <li>Application of arbitrary {@link SessionStubbingOperation} customizations supplied by the caller.</li>
 * </ul>
 * The root node is configured with an empty name (""), a fixed UUID like identifier and type to mimic a typical
 * repository root. Additional behaviour can be layered on top through the provided stubbing operations to keep tests
 * concise (DRY principle).
 * <p>
 * Use {@link #mockSession(String, SessionStubbingOperation...)} when you need a session bound to a workspace name and
 * optional custom stubbings. Use {@link #mockPlainSession()} when you only require a standalone session skeleton
 * without repository login logic. Call {@link #cleanSession()} between tests if you need to reset shared repository
 * state produced by {@link RepositoryMockUtils}.
 * <p>
 * Thread-safety: The produced mocks themselves are standard Mockito mocks; thread-safety guarantees therefore depend
 * on Mockito and the test usage pattern. This utility does not synchronize access and is intended for typical single
 * threaded unit test execution.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public final class SessionMockUtils {

    private SessionMockUtils() {
    }

    /**
     * Generic {@link Answer} used to implement {@link Session#getProperty(String)} on a mock session by delegating
     * to {@link Session#getItem(String)}. This keeps property retrieval logic DRY when only the generic item lookup
     * was stubbed by other operations.
     * <p>
     * Returns {@code null} if the underlying session mock is {@code null}; otherwise casts the located item to
     * {@link Property}. A ClassCastException may be thrown at runtime if the retrieved item is not a Property – this
     * mirrors the behaviour one would get when misusing the real JCR API.
     */
    public static final Answer<Property> PROPERTY_ANSWER = invocation -> {
        Session s = (Session) invocation.getMock();
        String path = (String) invocation.getArguments()[0];
        return s == null ? null : (Property) s.getItem(path);
    };

    /**
     * Create (or reuse) a mocked {@link Session} for the given workspace name and apply the provided stubbing
     * operations in order.
     * <p>
     * Behaviour:
     * <ol>
     *     <li>Validates that {@code workspace} is not blank and {@code stubbings} is not {@code null}.</li>
     *     <li>Obtains (or creates) a mocked {@link Repository} via {@link RepositoryMockUtils#mockRepository(RepositoryStubbingOperation...)}.</li>
     *     <li>Performs a repository login. If the login yields {@code null}, a new plain session is created via
     *     {@link #mockPlainSession()}, a matching workspace is mocked and login stubbing is installed.</li>
     *     <li>Applies each {@link SessionStubbingOperation} to the resulting session.</li>
     * </ol>
     * The returned session has at minimum a root node ("/") with identifier and type already stubbed.
     *
     * @param workspace the (non blank) workspace name to associate with the session.
     * @param stubbings ordered list of additional stubbing operations to customize the session mock.
     * @return the prepared session mock (never {@code null}).
     * @throws RepositoryException if underlying repository operations throw (rare for mocks unless user provided
     *                             stubbings escalate exceptions).
     * @throws IllegalArgumentException if preconditions are violated
     */
    public static Session mockSession(String workspace, SessionStubbingOperation... stubbings) throws RepositoryException {
        Require.Argument.notBlank(workspace, "workspace must not be null");
        Require.Argument.notNull(stubbings, "stubbings must not be null");
        Repository repository = RepositoryMockUtils.mockRepository();
        Session result = repository.login(workspace);
        if (result == null) {
            result = mockPlainSession();
            WorkspaceMockUtils.mockWorkspace(workspace, WorkspaceStubbingOperation.stubSession(result));
            stubLogin(result).of(repository);
        }
        for (SessionStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        // Clear all invocations to avoid confusion when verifying invocations later:
        Mockito.clearInvocations(result);
        return result;
    }

    /**
     * Create a minimal, standalone mocked {@link Session} not associated with a specific workspace login.
     * <p>
     * The returned session has:
     * <ul>
     *     <li>A mocked root node path "/" with empty name, fixed identifier and type "rep:root".</li>
     *     <li>{@link Session#getRootNode()} stubbed to return the root node.</li>
     *     <li>{@link Session#getProperty(String)} delegated to {@link Session#getItem(String)} via {@link #PROPERTY_ANSWER}.</li>
     * </ul>
     * This is useful for focused unit tests that do not need repository / workspace semantics.
     *
     * @return the plain session mock.
     * @throws RepositoryException propagated for consistency (mocked operations normally will not throw unless
     *                             additional stubbings introduce it).
     */
    public static Session mockPlainSession() throws RepositoryException {
        Session result = mock(Session.class);
        Node root = NodeMockUtils.mockPlainNode("/");
        stubIdentifier("cafebabe-cafe-babe-cafe-babecafebabe").of(root);
        when(root.getName()).thenReturn("");
        stubRootNode(root).of(result);
        stubType("rep:root").of(root);
        doAnswer(PROPERTY_ANSWER).when(result).getProperty(anyString());
        return result;
    }

    /**
     * Reset / clean up repository related static state. Should be called between tests if mocked repository state
     * must not leak between test cases and test isolation is important.
     */
    public static void cleanSession() {
        RepositoryMockUtils.cleanRepository();
    }
}
