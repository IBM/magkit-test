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
import org.apache.jackrabbit.JcrConstants;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import java.util.Calendar;
import java.util.Collection;

import static de.ibmix.magkit.test.jcr.SessionStubbingOperation.stubItem;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Factory collection for creating {@link Node}-scoped Mockito stubbing operations.
 * <p>
 * Each static method returns a {@link NodeStubbingOperation} (an {@link ExceptionStubbingOperation}) that encapsulates
 * an idempotent set of Mockito stubs to be applied to a {@link Node} mock via {@link NodeStubbingOperation#of(Node)}.
 * Operations are intentionally small, composable building blocks and may be combined (varargs) by higher level
 * helpers such as {@code NodeMockUtils.mockNode(String, NodeStubbingOperation...)}.
 * </p>
 * <p><strong>Consistency handling:</strong><br>
 * When a target node is an internal {@code NodeMockUtils.TestNode} implementation, operations attempt to keep bidirectional
 * relations (parent/child, session registration, property collections) consistent. For plain Mockito node mocks the
 * stubbing is deliberately minimal and focused only on the directly requested behavior.
 * </p>
 * <p><strong>Typical usage:</strong>
 * <pre>{@code
 * Node article = NodeMockUtils.mockNode("article",
 *     NodeStubbingOperation.stubType("mgnl:page"),
 *     NodeStubbingOperation.stubIdentifier("1234-uuid"),
 *     NodeStubbingOperation.stubProperty("title", "Hello")
 * );
 * }</pre>
 * </p>
 * <p><strong>Thread-safety:</strong> Operations are stateless; concurrency concerns relate only to the underlying mock which
 * is normally mutated in a single test thread.</p>
 * <p><strong>Error handling:</strong> Assertions fail fast on invalid inputs (e.g. null target nodes) to surface test setup
 * mistakes early. Declared {@link RepositoryException}s are only passed through when dependent API calls are preconfigured to throw.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-10-09
 * @see PropertyStubbingOperation
 * @see SessionStubbingOperation
 */
public abstract class NodeStubbingOperation implements ExceptionStubbingOperation<Node, RepositoryException> {
    public static final String PROPNAME_TITLE = "title";
    public static final String UNTITLED = "untitled";
    public static final String UNTITLED_HANDLE = "/" + UNTITLED;

    /**
     * Stub {@link Node#getSession()} to return the supplied session and register the node + its properties/items in that session.
     * <p>For a {@code TestNode} the session registration keeps internal collections consistent; for a plain mock only
     * {@code getSession()} is stubbed plus minimal registration.</p>
     *
     * @param session session to associate (may be {@code null} if deliberate test case)
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubJcrSession(final Session session) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                doReturn(session).when(node).getSession();
                stubItem(node).of(session);
            }
        };
    }

    /**
     * Stub {@link Node#getPrimaryNodeType()} and related {@code jcr:primaryType} property by name.
     * <p>When {@code typeName} is null no NodeType mock is created and the primary type returns null.</p>
     *
     * @param typeName node type name or {@code null}
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubType(final String typeName) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                NodeType nodeType = null;
                if (typeName != null) {
                    nodeType = mock(NodeType.class);
                    when(nodeType.getName()).thenReturn(typeName);
                    when(nodeType.isNodeType(typeName)).thenReturn(true);
                }
                stubProperty(JcrConstants.JCR_PRIMARYTYPE, typeName).of(node);
                when(node.getPrimaryNodeType()).thenReturn(nodeType);
            }
        };
    }

    /**
     * Convenience for stubbing a textual title property ("title").
     *
     * @param value title value (may be {@code null})
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubTitle(final String value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                stubProperty(PROPNAME_TITLE, value).of(node);
            }
        };
    }

    /**
     * Stub a STRING or multi value STRING property by name.
     * <p>The first element (if any) is returned by {@link Property#getString()} / {@link Property#getValue()}.</p>
     *
     * @param name property name
     * @param values zero or more string values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final String... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a BINARY property (single or multi valued).
     *
     * @param name property name
     * @param values binary values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Binary... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a DATE property (Calendar values).
     *
     * @param name property name
     * @param values calendar values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Calendar... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a BOOLEAN property.
     *
     * @param name property name
     * @param values boolean values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Boolean... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a DOUBLE property.
     *
     * @param name property name
     * @param values double values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Double... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a LONG property.
     *
     * @param name property name
     * @param values long values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Long... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    /**
     * Stub a REFERENCE property referencing another node.
     *
     * @param name property name
     * @param value referenced node (may be {@code null})
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Node value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                Property p = PropertyMockUtils.mockProperty(name, value, PropertyType.REFERENCE);
                stubProperty(p).of(node);
            }
        };
    }

    /**
     * Stub a property from prepared {@link Value} objects.
     *
     * @param name property name
     * @param value JCR values
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final String name, final Value... value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                Property p = PropertyMockUtils.mockProperty(name, value);
                stubProperty(p).of(node);
            }
        };
    }

    /**
     * Attach a fully configured {@link Property} mock to the node while keeping session/node internal collections in sync when possible.
     * <p>For {@code TestNode} instances the property replaces any previous property of same name.</p>
     *
     * @param property prepared property mock
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubProperty(final Property property) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                when(property.getParent()).thenReturn(node);
                if (node instanceof NodeMockUtils.TestNode) {
                    NodeMockUtils.TestNode testNode = (NodeMockUtils.TestNode) node;
                    Session s = testNode.getSession();
                    if (s != null) {
                        stubItem(property).of(s);
                    }
                    PropertyIterator properties = testNode.getProperties();
                    while (properties.hasNext()) {
                        Property p = properties.nextProperty();
                        if (p.getName().equals(property.getName())) {
                            properties.remove();
                        }
                    }
                    testNode.getPropertyCollection().add(property);
                } else {
                    String propertyName = property.getName();
                    doReturn(property).when(node).getProperty(propertyName);
                }
            }
        };
    }

    /**
     * Create and register a child node (by name + optional stubbings) beneath a parent node.
     * <p>Uses {@link NodeMockUtils#mockNode(String, NodeStubbingOperation...)} for child creation.</p>
     *
     * @param name child node name
     * @param stubbings additional node stubbings for the child
     * @return stubbing operation
     * @throws RepositoryException propagated from nested stubbings
     */
    public static NodeStubbingOperation stubNode(final String name, final NodeStubbingOperation... stubbings) throws RepositoryException {
        Node child = NodeMockUtils.mockNode(name, stubbings);
        return stubNode(child);
    }

    /**
     * Register an existing child node beneath a parent; keeps parent/child relation consistent for {@code TestNode}.
     *
     * @param child child node
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubNode(final Node child) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node parent) throws RepositoryException {
                Require.Argument.notNull(parent, "parent must not be null");
                if (parent instanceof NodeMockUtils.TestNode) {
                    Collection<Node> nodes = ((NodeMockUtils.TestNode) parent).getNodeCollection();
                    nodes.add(child);
                    stubParent(parent).of(child);
                } else if (isNotEmpty(child.getName())) {
                    String childName = child.getName();
                    doReturn(child).when(parent).getNode(childName);
                    doReturn(parent).when(child).getParent();
                }
            }
        };
    }

    /**
     * Stub the node name. Blank input defaults to {@link #UNTITLED}. Existing children/paths are not recalculated here; higher-level
     * operations (like {@link #stubParent(Node)}) handle tree consistency.
     *
     * @param value desired node name (may be blank)
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubName(final String value) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                String name = isBlank(value) ? UNTITLED : value;
                doReturn(name).when(node).getName();
            }
        };
    }

    /**
     * Stub UUID/identifier of a node; also updates associated session lookup methods (old UUID mappings cleared, new ones added).
     * Requires a non-blank identifier.
     *
     * @param identifier new identifier (not blank)
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubIdentifier(final String identifier) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                Require.Argument.notEmpty(identifier, "identifier must not be blank");
                if (node.getSession() != null) {
                    if (isNotBlank(node.getUUID())) {
                        when(node.getSession().getNodeByUUID(node.getUUID())).thenReturn(null);
                        when(node.getSession().getNodeByIdentifier(node.getIdentifier())).thenReturn(null);
                    }
                    when(node.getSession().getNodeByUUID(identifier)).thenReturn(node);
                    when(node.getSession().getNodeByIdentifier(identifier)).thenReturn(node);
                }
                when(node.getIdentifier()).thenReturn(identifier);
                when(node.getUUID()).thenReturn(identifier);
            }
        };
    }

    /**
     * Stub parent relation for a node; re-registers the node (and recursively its descendants & properties) in the new session context if present.
     * <p>Also protects against setting a node as its own parent and clears old session lookups.</p>
     *
     * @param parent new parent node
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubParent(final Node parent) {
        return new NodeStubbingOperation() {
            public void of(Node child) throws RepositoryException {
                Require.Argument.notNull(child, "child must not be null");
                Require.Argument.reject(parent::equals, child, "Illegal attempt to set a node as its parent: " + child.getPath());
                Session s = child.getSession();
                if (s != null) {
                    SessionStubbingOperation.stubRemoveItem(child).of(s);
                }
                when(child.getParent()).thenReturn(parent);
                register(child);
            }
        };
    }

    /**
     * Stub node mixin types returned by {@link Node#getMixinNodeTypes()}.
     *
     * @param mixins array of mixin NodeType mocks
     * @return stubbing operation
     */
    public static NodeStubbingOperation stubMixinNodeTypes(final NodeType... mixins) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Require.Argument.notNull(node, "node must not be null");
                doReturn(mixins).when(node).getMixinNodeTypes();
            }
        };
    }

    private static void register(final Node child) throws RepositoryException {
        Session s = child.getSession();
        if (s != null) {
            stubItem(child).of(s);
            NodeIterator nodes = child.getNodes();
            while (nodes.hasNext()) {
                register(nodes.nextNode());
            }
            PropertyIterator properties = child.getProperties();
            while (properties.hasNext()) {
                Property p = properties.nextProperty();
                stubItem(p).of(s);
            }
        }
    }
}
