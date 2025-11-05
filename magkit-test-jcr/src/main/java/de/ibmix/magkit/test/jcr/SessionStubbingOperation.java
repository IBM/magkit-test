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
import de.ibmix.magkit.test.ExceptionStubbingOperation;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Stubbing operations for Mockito based {@link Session} mocks.
 * <p>
 * This utility exposes focused factory methods that return immutable {@link SessionStubbingOperation} instances.
 * Each operation can be:
 * <ul>
 *   <li>Applied at creation time: {@code Session s = mockSession("ws", stubAttribute("key", value), stubWorkspace(workspace))}</li>
 *   <li>Or later: {@code stubAttribute("key", value).of(existingSession)}</li>
 * </ul>
 * Goals:
 * <ul>
 *   <li>Keep test code short (fluent, chainable, composable).</li>
 *   <li>Preserve internal consistency (e.g. adding an {@link Item} updates several lookup methods).</li>
 *   <li>Avoid brittle manual Mockito stubbing that may desynchronise caches (DRY principle).</li>
 * </ul>
 * Error handling contract:
 * <ul>
 *   <li>All operations assert the target {@link Session} (or required arguments) are non-null and may throw assertion errors early.</li>
 *   <li>Declared {@link RepositoryException}s are only propagated when underlying mocked calls were configured to throw (rare).</li>
 * </ul>
 * Thread-safety: Operations are stateless and therefore thread-safe. Applying them concurrently to the *same* mock
 * is not synchronized and should normally only occur in single-threaded test code.
 * <p>
 * Custom operations: Implement your own by extending this class in a test source set if project specific stubbing is
 * required. Prefer reusing the provided building blocks such as {@link #stubItem(Item)} to remain consistent.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public abstract class SessionStubbingOperation implements ExceptionStubbingOperation<Session, RepositoryException> {

    private SessionStubbingOperation() {
    }

    /**
     * Stub a session attribute (key/value) and augment the session's attribute name list.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>Returns an operation that sets {@link Session#getAttribute(String)} for {@code name}.</li>
     *   <li>Extends the array returned by {@link Session#getAttributeNames()} with the new name (preserving existing order).</li>
     * </ul>
     * No attempt is made to prevent duplicate names; repeated application may append the same key multiple times if the
     * underlying mock returns dynamically updated arrays.
     *
     * @param name attribute key (must not be null; empty allowed but discouraged for readability).
     * @param value attribute value (may be {@code null}).
     * @return operation to apply attribute stubbing.
     */
    public static SessionStubbingOperation stubAttribute(final String name, final Object value) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getAttribute(name)).thenReturn(value);
                String[] names = ArrayUtils.add(session.getAttributeNames(), name);
                when(session.getAttributeNames()).thenReturn(names);
            }
        };
    }

    /**
     * Register an {@link Item} (either {@link Node} or {@link Property}) with a session so that standard lookup
     * methods resolve it consistently.
     * <p>
     * For both node and property items:
     * <ul>
     *   <li>Stubs {@link Session#getItem(String)} and {@link Session#itemExists(String)}.</li>
     * </ul>
     * Additional behaviour for nodes:
     * <ul>
     *   <li>Stubs {@link Session#getNode(String)} and {@link Session#nodeExists(String)}.</li>
     *   <li>If the node has a non-empty identifier, stubs {@link Session#getNodeByUUID(String)} and {@link Session#getNodeByIdentifier(String)}.</li>
     * </ul>
     * Existing stubbings for the same path are silently overridden.
     *
     * @param item the item to register; its {@link Item#getPath()} and for nodes {@link Node#getIdentifier()} are consulted.
     * @return operation adding the item to the session lookup space.
     */
    public static SessionStubbingOperation stubItem(final Item item) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) throws RepositoryException {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getItem(item.getPath())).thenReturn(item);
                when(session.itemExists(item.getPath())).thenReturn(true);
                if (item.isNode()) {
                    Node node = (Node) item;
                    when(session.getNode(node.getPath())).thenReturn(node);
                    when(session.nodeExists(node.getPath())).thenReturn(true);
                    String uuid = node.getIdentifier();
                    if (isNotEmpty(uuid)) {
                        when(session.getNodeByUUID(uuid)).thenReturn(node);
                        when(session.getNodeByIdentifier(uuid)).thenReturn(node);
                    }
                }
            }
        };
    }

    /**
     * Remove a previously registered {@link Item} (node or property) from session lookup results.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>Stubs relevant getters to return {@code null} / non-existence flags.</li>
     *   <li>If the item is a node, recursively removes all descendant nodes and properties by iterating
     *   {@link Node#getNodes()} and {@link Node#getProperties()}.</li>
     *   <li>For removed properties, {@link Session#getProperty(String)} is explicitly stubbed to return {@code null}.</li>
     * </ul>
     * Caveats:
     * <ul>
     *   <li>Recursion depth equals the node subtree – extremely deep or cyclic mock graphs (should not occur) may impact performance.</li>
     *   <li>Does not attempt to shrink any previously exposed attribute / name lists; only lookup responses are updated.</li>
     * </ul>
     *
     * @param item the root item to remove from the session visibility graph.
     * @return operation that performs the removal stubbing.
     */
    public static SessionStubbingOperation stubRemoveItem(final Item item) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) throws RepositoryException {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getItem(item.getPath())).thenReturn(null);
                when(session.itemExists(item.getPath())).thenReturn(false);
                if (item.isNode()) {
                    when(session.getNode(item.getPath())).thenReturn(null);
                    when(session.nodeExists(item.getPath())).thenReturn(false);
                    Node node = (Node) item;
                    String uuid = node.getIdentifier();
                    if (isNotEmpty(uuid)) {
                        when(session.getNodeByUUID(uuid)).thenReturn(null);
                        when(session.getNodeByIdentifier(uuid)).thenReturn(null);
                    }
                    NodeIterator nodes = node.getNodes();
                    while (nodes.hasNext()) {
                        stubRemoveItem(nodes.nextNode()).of(session);
                    }
                    PropertyIterator properties = node.getProperties();
                    while (properties.hasNext()) {
                        Property p = properties.nextProperty();
                        stubRemoveItem(p).of(session);
                        when(session.getProperty(p.getPath())).thenReturn(null);
                    }
                }
            }
        };
    }

    /**
     * INTERNAL: Install a root node for a fresh session mock. Ensures session ↔ node bidirectional linkage and that
     * the node is registered via {@link #stubItem(Item)}. Not intended for direct use in test code.
     *
     * @param node the designated root node (path must be "/").
     * @return operation establishing the root relation.
     */
    static SessionStubbingOperation stubRootNode(final Node node) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) throws RepositoryException {
                Require.Argument.notNull(session, "session must not be null");
                doReturn(node).when(session).getRootNode();
                doReturn(session).when(node).getSession();
                stubItem(node).of(session);
            }
        };
    }

    /**
     * Stub the owning {@link Repository} reference of a session.
     *
     * @param value repository mock to expose (may be {@code null} if deliberate test case).
     * @return operation configuring {@link Session#getRepository()}.
     */
    public static SessionStubbingOperation stubRepository(final Repository value) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getRepository()).thenReturn(value);
            }
        };
    }

    /**
     * Stub the {@link Workspace} exposed by {@link Session#getWorkspace()}.
     * Typically combined with workspace-level stubbings elsewhere.
     *
     * @param value workspace mock to return (may be {@code null}).
     * @return operation configuring the workspace.
     */
    public static SessionStubbingOperation stubWorkspace(final Workspace value) {
        return new SessionStubbingOperation() {
            @Override
            public void of(Session session) {
                Require.Argument.notNull(session, "session must not be null");
                when(session.getWorkspace()).thenReturn(value);
            }
        };
    }

    /**
     * Directly install a {@link ValueFactory} for the session.
     * <p>
     * Use this overload when you already created / customised a value factory mock. If you only need to augment or
     * lazily create one, prefer {@link #stubValueFactory(ValueFactoryStubbingOperation...)}.
     *
     * @param valueFactory the factory to return from {@link Session#getValueFactory()} (may be {@code null}).
     * @return operation configuring value factory access.
     */
    public static SessionStubbingOperation stubValueFactory(final ValueFactory valueFactory) {
        return new SessionStubbingOperation() {
            @Override
            public void of(final Session context) throws RepositoryException {
                Require.Argument.notNull(context, "session must not be null");
                when(context.getValueFactory()).thenReturn(valueFactory);
            }
        };
    }

    /**
     * Ensure a {@link ValueFactory} exists (creating one if missing) and apply additional {@link ValueFactoryStubbingOperation}s.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>If session has no factory yet, creates one via {@link ValueFactoryMockUtils#mockValueFactory(ValueFactoryStubbingOperation...)}.</li>
     *   <li>Otherwise applies each provided stubbing to the existing factory.</li>
     * </ul>
     * Idempotent regarding repeated application of the *same* stubbings only if the individual value factory operations
     * themselves are idempotent.
     *
     * @param stubbings zero or more value factory operations (array may be empty).
     * @return operation that maintains / enriches the value factory.
     */
    public static SessionStubbingOperation stubValueFactory(final ValueFactoryStubbingOperation... stubbings) {
        return new SessionStubbingOperation() {
            @Override
            public void of(final Session context) throws RepositoryException {
                Require.Argument.notNull(context, "session must not be null");
                ValueFactory factory = context.getValueFactory();
                if (factory == null) {
                    factory = ValueFactoryMockUtils.mockValueFactory(stubbings);
                    when(context.getValueFactory()).thenReturn(factory);
                } else {
                    for (ValueFactoryStubbingOperation stub : stubbings) {
                        stub.of(factory);
                    }
                }
            }
        };
    }
}
