package de.ibmix.magkit.mockito.jcr;

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
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;

import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.mockito.jcr.SessionStubbingOperation.stubItem;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for NodeStubbingOperation.
 * Stubbing operations to be used as parameters in NodeMockUtils.mock...(...).
 *
 * @author wolf.bubenik
 * @since 09.10.2012
 */
public abstract class NodeStubbingOperation {
    public static final String PROPNAME_TITLE = "title";
    public static final String UNTITLED = "untitled";
    public static final String UNTITLED_HANDLE = "/" + UNTITLED;

    public abstract void of(Node node) throws RepositoryException;

    /**
     * Creates NodeStubbingOperation that stubs method {@link javax.jcr.Node#getSession()} to return the provided session.
     *
     * @param session the session to return
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubJcrSession(final Session session) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                doReturn(session).when(node).getSession();
            }
        };
    }

    /**
     * Creates NodeStubbingOperation that stubs method node.getPrimaryNodeType() to return a NodeType by provided name.
     *
     * @param typeName the name of the NodeType to return
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubType(final String typeName) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                // TODO: check default NodeType (null or NT_BASE?)
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
     * Creates NodeStubbingOperation that stubs method node.getTitle() to return the provided value.
     *
     * @param value the String to return
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubTitle(final String value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(PROPNAME_TITLE, value).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final String... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Binary... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final InputStream... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Calendar... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Boolean... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Double... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Long... values) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                Property property = PropertyMockUtils.mockProperty(name, values);
                stubProperty(property).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Node value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                Property p = PropertyMockUtils.mockProperty(name, value, PropertyType.REFERENCE);
                stubProperty(p).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final String name, final Value... value) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                Property p = PropertyMockUtils.mockProperty(name, value);
                stubProperty(p).of(node);
            }
        };
    }

    public static NodeStubbingOperation stubProperty(final Property property) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                NodeMockUtils.TestNode testNode = (NodeMockUtils.TestNode) node;
                when(property.getParent()).thenReturn(node);
                Session s = node.getSession();
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

            }
        };
    }

    public static NodeStubbingOperation stubNode(final String name, final NodeStubbingOperation... stubbings) throws RepositoryException {
        Node child = mockNode(name, stubbings);
        return stubNode(child);
    }

    public static NodeStubbingOperation stubNode(final Node child) {
        return new NodeStubbingOperation() {
            @Override
            public void of(final Node parent) throws RepositoryException {
                assertThat(parent, notNullValue());
                NodeMockUtils.TestNode testNode = (NodeMockUtils.TestNode) parent;
                Collection<Node> nodes = testNode.getNodeCollection();
                nodes.add(child);
                stubParent(parent).of(child);
            }
        };
    }

    /**
     * Creates NodeStubbingOperation that stubs a Node for the provided path.
     * If the provided String is null, empty or blank, "/untitled" will be used as default.
     * "/" will be added at the beginning of the path if missing.
     * Two properties of the Node will be stubbed:
     * - node.getHandle() to return the provided value
     * - node.getName() to return the provided value
     *
     * @param value the String to return
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubName(final String value) {
        return new NodeStubbingOperation() {
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                String name = isBlank(value) ? UNTITLED : value;
                doReturn(name).when(node).getName();
            }
        };
    }

    /**
     * Creates NodeStubbingOperation that stubs node.getUUID() to return the provided value.
     * Following properties will be stubbed:
     * - hierarchyManager.getNodeByUuid(oldUuid) to return NULL
     * - hierarchyManager.getNodeByUuid(uuid) to return node
     * - node.getIdentifier() to return the provided value
     * - node.getUUID() to return the provided value
     *
     * @param identifier the String to return
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubIdentifier(final String identifier) {
        return new NodeStubbingOperation() {

            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                assertThat(isNotBlank(identifier), is(true));
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
     * Creates NodeStubbingOperation that stubs node.getParent() to return the provided value.
     * Executes following stubbingOperations:
     * - stubLevel.of(child)
     * - stubAncestors().of(child)
     * - stubName(child.getName()).of(child) to trigger unregistering of child with previous path and stubbing of new path
     * stubParent(child).of(grandchild) is recursively executed for all children of child to update their level, ancestors and paths.
     *
     * @param parent the parent node to be stubbed
     * @return NodeStubbingOperation instance
     */
    public static NodeStubbingOperation stubParent(final Node parent) {
        return new NodeStubbingOperation() {
            public void of(Node child) throws RepositoryException {
                assertThat(child, notNullValue());
                assertThat("Illegal attempt to set a node as its parent: " + child.getPath(), child, not(is(parent)));
                Session s = child.getSession();
                if (s != null) {
                    SessionStubbingOperation.stubRemoveItem(child).of(s);
                }
                when(child.getParent()).thenReturn(parent);
                register(child);
            }
        };
    }

    public static NodeStubbingOperation stubMixinNodeTypes(final NodeType... mixins) {
        return new NodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
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
