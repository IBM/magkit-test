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


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.PropertyIteratorAdapter;
import org.mockito.Answers;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubMixinNodeTypes;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubName;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubType;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utils for mocking JCR nodes.
 *
 * @author wolf.bubenik@aperto.com
 * @since 09.10.2012
 */
public final class NodeMockUtils {

    public static final Answer<Boolean> IS_NODE_TYPE_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String type = (String) invocation.getArguments()[0];
        return StringUtils.equals(node.getPrimaryNodeType().getName(), type);
    };
    public static final Answer<Object> ACCEPT_ANSWER = invocation -> {
        Object[] args = invocation.getArguments();
        ItemVisitor visitor = (ItemVisitor) args[0];
        visitor.visit((Node) invocation.getMock());
        return null;
    };
    public static final Answer<Item> ANCESTOR_ANSWER = invocation -> {
        Object[] args = invocation.getArguments();
        int depth = (Integer) args[0];
        Item node = (Item) invocation.getMock();
        if (depth < 0 || node.getDepth() < depth) {
            throw new ItemNotFoundException("No ancestor with depth " + depth);
        }
        Item result = node;
        while (result.getDepth() > depth) {
            result = result.getParent();
        }
        return result;
    };
    public static final Answer<String> PATH_ANSWER = invocation -> {
        Item node = (Item) invocation.getMock();
        Node parent = node.getParent();
        // note that we may have a property namespace withing the path. This is very likely not correct
        // but allows using the item path for mocking session.getItem(absPath)
        return getPathForParent(parent, node.getName());
    };
    public static final Answer<Integer> DEPTH_ANSWER = invocation -> {
        Item node = (Item) invocation.getMock();
        Node parent = node.getParent();
        return parent == null ? 0 : parent.getDepth() + 1;
    };
    public static final Answer<Session> SESSION_ANSWER = invocation -> {
        Item node = (Item) invocation.getMock();
        Item parent = node.getParent();
        return parent == null ? null : parent.getSession();
    };
    public static final Answer<Node> NODE_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String relPath = (String) invocation.getArguments()[0];
        String absPath = getPathForParent(node, relPath);
        Session s = node.getSession();
        return s == null ? null : (Node) s.getItem(absPath);
    };
    public static final Answer<Property> PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String relPath = (String) invocation.getArguments()[0];
        String absPath = getPathForParent(node, relPath);
        Session s = node.getSession();
        return s == null ? null : (Property) s.getItem(absPath);
    };
    public static final Answer<NodeIteratorAdapter> NODES_ANSWER = invocation -> {
        TestNode node = (TestNode) invocation.getMock();
        return new NodeIteratorAdapter(node.getNodeCollection());
    };
    public static final Answer<Boolean> HAS_NODES_ANSWER = invocation -> {
        TestNode node = (TestNode) invocation.getMock();
        return node.getNodeCollection() != null && !node.getNodeCollection().isEmpty();
    };
    public static final Answer<PropertyIteratorAdapter> PROPERTIES_ANSWER = invocation -> {
        TestNode node = (TestNode) invocation.getMock();
        return new PropertyIteratorAdapter(node.getPropertyCollection());
    };
    public static final Answer<Boolean> HAS_PROPERTIES_ANSWER = invocation -> {
        TestNode node = (TestNode) invocation.getMock();
        return node.getPropertyCollection() != null && !node.getPropertyCollection().isEmpty();
    };
    public static final Answer<Boolean> ITEM_EXISTS_ANSWER = invocation -> {
        Item node = (Item) invocation.getMock();
        String relPath = (String) invocation.getArguments()[0];
        String absPath = getPathForParent(node, relPath);
        Session s = node.getSession();
        return s != null && s.itemExists(absPath);
    };
    public static final Answer<Node> ADD_NODE_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String relPath = (String) invocation.getArguments()[0];
        Node result = null;
        if (isNotBlank(relPath)) {
            String separator = node.getPath().endsWith("/") ? EMPTY : "/";
            String absPath = node.getPath() + separator + relPath;
            result = mockNode(node.getSession().getWorkspace().getName(), absPath);
        }
        return result;
    };
    public static final Answer<Node> ADD_NODE_WITH_TYPE_ANSWER = invocation -> {
        String type = (String) invocation.getArguments()[1];
        Node result = ADD_NODE_ANSWER.answer(invocation);
        if (result != null && isNotBlank(type)) {
            stubType(type).of(result);
        }
        return result;
    };
    public static final Answer<Property> SET_BOOLEAN_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Boolean value = (Boolean) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_BINARY_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Binary value = (Binary) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };

    public static final Answer<Property> SET_CALENDAR_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Calendar value = (Calendar) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_DOUBLE_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Double value = (Double) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_LONG_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Long value = (Long) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_NODE_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Node value = (Node) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_STRING_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        String value = (String) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_STRINGS_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        String[] value = (String[]) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_VALUE_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Value value = (Value) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };
    public static final Answer<Property> SET_VALUES_PROPERTY_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String name = (String) invocation.getArguments()[0];
        Value[] value = (Value[]) invocation.getArguments()[1];
        stubProperty(name, value).of(node);
        return node.getProperty(name);
    };

    private NodeMockUtils() {
    }

    /**
     * Creates a Node mock with name "untitled" in the "website" workspace.
     *
     * @return a Mockito mock for a javax.jcr.Node
     * @throws RepositoryException declared exception from jcr api but not thrown if not explicitly mocked
     */
    public static Node mockNode(final NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockNode(NodeStubbingOperation.UNTITLED, nodeStubbings);
    }

    /**
     * Creates a Node mock with given path in the "website" workspace.
     *
     * @return a Mockito mock for a javax.jcr.Node
     * @throws RepositoryException declared exception from jcr api but not thrown if not explicitly mocked
     */
    public static Node mockNode(final String name, final NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockNode("website", name, nodeStubbings);
    }

    /**
     * Mocks the node hierarchy for the given path in workspace with the given name.
     *
     * @param nodeStubbings for stub behaviour
     * @return mocked node
     */
    public static Node mockNode(final String repository, final String path, final NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        Session session = SessionMockUtils.mockSession(repository);
        String cleanPath = sanitizeHandle(path);
        Node node = session.itemExists(cleanPath) ? (Node) session.getItem(cleanPath) : mockNewNode(session, cleanPath);
        if (nodeStubbings != null) {
            for (NodeStubbingOperation nodeStubbing : nodeStubbings) {
                nodeStubbing.of(node);
            }
        }
        return node;
    }

    public static Node mockNodeFromXml(final String repository, InputStream xmlUtf8) {
        try {
            JcrXmlHandler jcrXmlHandler = new JcrXmlHandler(repository);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(xmlUtf8, jcrXmlHandler);
            return jcrXmlHandler.getResult();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mocks the node hierarchy for the given node path in the given session.
     */
    public static Node mockNewNode(final Session session, final String path) throws RepositoryException {
        String[] pathSegments = StringUtils.split(path, '/');
        return mockNodeTree(session.getRootNode(), pathSegments);
    }

    /**
     * Adds the node hierarchy for the given names to the given root node. New nodes will be of type NodeType.NT_BASE.
     */
    public static Node mockNodeTree(final Node root, final String... pathSegments) throws RepositoryException {
        String name = pathSegments[0];
        String path = getPathForParent(root, name);
        Node result = (Node) root.getSession().getItem(path);
        if (result == null) {
            result = mockPlainNode(name);
            NodeStubbingOperation.stubNode(result).of(root);
        }
        if (pathSegments.length > 1) {
            String[] successors = ArrayUtils.remove(pathSegments, 0);
            result = mockNodeTree(result, successors);
        }
        return result;
    }

    /**
     * Replaces backslash by forward slash and removes leading and tailing white space characters.
     *
     * @param handle the node path to be sanitised
     * @return the sanitised String
     */
    public static String sanitizeHandle(final String handle) {
        String cleanHandle = handle;
        if (isBlank(cleanHandle)) {
            cleanHandle = NodeStubbingOperation.UNTITLED_HANDLE;
        }
        cleanHandle = cleanHandle.trim().replace('\\', '/');
        return cleanHandle;
    }

    /**
     * Creates a Mockito Mock for an javax.jcr.Node of type NodeType.NT_BASE and with the given name.
     *
     * @param name the name for the Node mock.
     * @return a Mockito mock for javax.jcr.Node
     * @throws RepositoryException declared exception from node api but never thrown.
     */
    public static Node mockPlainNode(final String name) throws RepositoryException {
        TestNode result = mock(TestNode.class);
        stubName(name).of(result);
        stubIdentifier(UUID.randomUUID().toString()).of(result);
        stubMixinNodeTypes().of(result);
        doAnswer(ACCEPT_ANSWER).when(result).accept(any(ItemVisitor.class));
        when(result.isNode()).thenReturn(true);
        when(result.getPath()).then(PATH_ANSWER);
        when(result.getDepth()).then(DEPTH_ANSWER);
        when(result.getSession()).then(SESSION_ANSWER);
        when(result.getNode(anyString())).then(NODE_ANSWER);
        when(result.getProperty(anyString())).then(PROPERTY_ANSWER);
        when(result.hasNode(anyString())).then(ITEM_EXISTS_ANSWER);
        when(result.hasProperty(anyString())).then(ITEM_EXISTS_ANSWER);
        when(result.getNodeCollection()).thenReturn(new ArrayList<>());
        when(result.getPropertyCollection()).thenReturn(new ArrayList<>());
        when(result.getNodes()).then(NODES_ANSWER);
        when(result.getNodes(anyString())).then(NODES_ANSWER);
        when(result.getNodes(any(String[].class))).then(NODES_ANSWER);
        when(result.hasNodes()).then(HAS_NODES_ANSWER);
        when(result.getProperties()).then(PROPERTIES_ANSWER);
        when(result.getProperties(anyString())).then(PROPERTIES_ANSWER);
        when(result.getProperties(any(String[].class))).then(PROPERTIES_ANSWER);
        when(result.hasProperties()).then(HAS_PROPERTIES_ANSWER);
        when(result.getAncestor(anyInt())).thenAnswer(ANCESTOR_ANSWER);
        when(result.addNode(anyString())).thenAnswer(ADD_NODE_ANSWER);
        when(result.addNode(anyString(), anyString())).thenAnswer(ADD_NODE_WITH_TYPE_ANSWER);
        when(result.setProperty(anyString(), any(Binary.class))).thenAnswer(SET_BINARY_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), anyBoolean())).thenAnswer(SET_BOOLEAN_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Calendar.class))).thenAnswer(SET_CALENDAR_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), anyDouble())).thenAnswer(SET_DOUBLE_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), anyLong())).thenAnswer(SET_LONG_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Node.class))).thenAnswer(SET_NODE_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), anyString())).thenAnswer(SET_STRING_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), anyString(), anyInt())).thenAnswer(SET_STRING_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(String[].class))).thenAnswer(SET_STRINGS_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(String[].class), anyInt())).thenAnswer(SET_STRINGS_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Value.class))).thenAnswer(SET_VALUE_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Value.class), anyInt())).thenAnswer(SET_VALUE_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Value[].class))).thenAnswer(SET_VALUES_PROPERTY_ANSWER);
        when(result.setProperty(anyString(), any(Value[].class), anyInt())).thenAnswer(SET_VALUES_PROPERTY_ANSWER);
        doAnswer(Answers.CALLS_REAL_METHODS.get()).when(result).remove();
        // check if this is the correct default value
        stubType(NodeType.NT_BASE).of(result);
        when(result.isNodeType(anyString())).then(IS_NODE_TYPE_ANSWER);
        return result;
    }

    static String getPathForParent(Item parent, String relPath) throws RepositoryException {
        String result;
        if (parent == null) {
            result = "/";
        } else {
            String parentPath = parent.getPath();
            if (parentPath.endsWith("/")) {
                result = parentPath + relPath;
            } else {
                result = parentPath + "/" + relPath;
            }
        }
        return result;
    }

    /**
     * Extended Interface to simplify mocking.
     */
    abstract static class TestNode implements Node {
        abstract Collection<Node> getNodeCollection();

        abstract Collection<Property> getPropertyCollection();

        @Override
        public void remove() throws RepositoryException {
            NodeMockUtils.TestNode parent = (NodeMockUtils.TestNode) getParent();
            Collection<Node> siblings = parent.getNodeCollection();
            siblings.remove(this);
            Session s = getSession();
            if (s != null) {
                SessionStubbingOperation.stubRemoveItem(this).of(s);
            }
        }
    }
}
