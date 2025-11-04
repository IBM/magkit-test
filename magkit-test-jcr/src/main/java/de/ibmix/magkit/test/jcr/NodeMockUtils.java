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
import org.mockito.Mockito;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class providing factory methods to create Mockito based {@link Node} mocks and simple mock hierarchies for JCR related unit tests.
 * <p>
 * The helpers intentionally implement only a pragmatic subset of JCR semantics sufficient for most test scenarios: path construction,
 * ancestor navigation, adding child nodes, setting properties and basic type checks. More advanced repository behavior (locking, versioning,
 * access control, transient state handling, save semantics etc.) is <strong>not</strong> emulated.
 * </p>
 * Typical usage:
 * <pre>
 *     Node page = NodeMockUtils.mockNode("/content/site/en/page", stubType("mgnl:page"));
 *     Node asset = NodeMockUtils.mockNode("dam", "/assets/logo.png", stubType("mgnl:asset"));
 *     Node fromXml = NodeMockUtils.mockNodeFromXml("website", inputStream);
 * </pre>
 * All factory methods return fully stubbed nodes whose properties and children can be further refined through {@link NodeStubbingOperation} instances.
 * <p>
 * Thread-safety: instances produced are <em>not</em> thread-safe. Each test should create its own mock graph.
 * </p>
 * <p>
 * Path handling: Paths are always normalized using {@link #sanitizeHandle(String)} (trim + backslash to forward slash conversion).
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-10-31
 */
public final class NodeMockUtils {

    /**
     * Creates a {@link Node} mock named {@code "untitled"} of primary type {@link NodeType#NT_BASE} in the {@code "website"} workspace.
     * Additional stubbing operations can refine name, type, mixins or properties.
     *
     * @param nodeStubbings optional {@link NodeStubbingOperation} instances applied in order. May be {@code null} or empty.
     * @return the created {@link Node} mock instance
     * @throws RepositoryException never thrown unless a provided stubbing explicitly triggers it
     */
    public static Node mockNode(final NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockNode(NodeStubbingOperation.UNTITLED, nodeStubbings);
    }

    /**
     * Creates (or retrieves if already mocked) a {@link Node} mock for the given absolute path inside the default {@code "website"} workspace.
     * Missing intermediate path segments are created automatically using {@link NodeType#NT_BASE}.
     *
     * @param path absolute JCR-like path using forward slashes. If blank defaults to {@link NodeStubbingOperation#UNTITLED_HANDLE}.
     * @param nodeStubbings optional {@link NodeStubbingOperation} instances applied to the terminal node
     * @return the mock for the terminal path segment
     * @throws RepositoryException never thrown unless a provided stubbing explicitly triggers it
     */
    public static Node mockNode(final String path, final NodeStubbingOperation... nodeStubbings) throws RepositoryException {
        return mockNode("website", path, nodeStubbings);
    }

    /**
     * Creates (or retrieves if already mocked) a {@link Node} mock for the given absolute path inside the specified workspace.
     * Missing intermediate path segments are created automatically using {@link NodeType#NT_BASE}.
     *
     * @param repository name of the workspace
     * @param path absolute JCR-like path using forward slashes. If blank defaults to {@link NodeStubbingOperation#UNTITLED_HANDLE}.
     * @param nodeStubbings optional {@link NodeStubbingOperation} instances applied to the terminal node
     * @return the mock for the terminal path segment
     * @throws RepositoryException never thrown unless a provided stubbing explicitly triggers it
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
        // Clear all invocations to avoid confusion when verifying invocations later:
        Mockito.clearInvocations(node);
        return node;
    }

    /**
     * Creates (or retrieves if already mocked) a {@link Node} mock by parsing the given XML input stream.
     * The XML must be encoded in UTF-8 and conform to the JCR XML import format used by Magnolia / Jackrabbit.
     * Implementation details:
     * <ul>
     *   <li>Uses a SAX parser with the default factory settings (no validation, no namespace adjustments).</li>
     *   <li>Delegates element handling to {@code JcrXmlHandler} which creates node mocks and stubs properties accordingly.</li>
     *   <li>The provided input stream is not closed; caller remains responsible for resource cleanup.</li>
     * </ul>
     * <p><strong>Error handling:</strong> Any {@link ParserConfigurationException}, {@link SAXException} or {@link IOException}
     * encountered during parsing is wrapped in a {@link RuntimeException} to simplify test code (unchecked).</p>
     *
     * @param repository name of the workspace
     * @param xmlUtf8 input stream providing the XML content (UTF-8 encoded)
     * @return the root {@link Node} mock representing the parsed XML tree
     * @throws RuntimeException if the XML is invalid or an I/O parsing error occurs
     */
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
     * Creates a new {@link Node} mock for the given path by adding missing segments below the given parent node.
     * The parent node must exist and be a valid JCR node.
     *
     * @param session the current JCR session
     * @param path absolute JCR-like path using forward slashes. Must not be blank.
     * @return the created {@link Node} mock instance
     * @throws RepositoryException if the path is invalid or a repository error occurs
     */
    public static Node mockNewNode(final Session session, final String path) throws RepositoryException {
        String[] pathSegments = StringUtils.split(path, '/');
        return mockNodeTree(session.getRootNode(), pathSegments);
    }

    /**
     * Recursively creates a mock node tree below the given root node, mirroring the structure defined by the given path segments.
     * Intermediate nodes are created as needed using {@link #mockPlainNode(String)}.
     *
     * @param root the root node under which to create the mock tree
     * @param pathSegments the path segments defining the mock structure. Must not be empty.
     * @return the mock node corresponding to the last path segment
     * @throws RepositoryException if a mock node cannot be created
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
     * Sanitizes a JCR handle by trimming whitespace and converting backslashes to forward slashes.
     * If the handle is blank, a default untitled handle is returned.
     *
     * @param handle the handle to sanitize
     * @return the sanitized handle
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
     * Creates a plain {@link Node} mock with no parent or type information.
     * The node will have only a name and a unique identifier.
     *
     * @param name the name of the node
     * @return the created {@link Node} mock instance
     * @throws RepositoryException never thrown
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
        doAnswer(Answers.CALLS_REAL_METHODS).when(result).remove();
        stubType(NodeType.NT_BASE).of(result);
        when(result.isNodeType(anyString())).then(IS_NODE_TYPE_ANSWER);
        doAnswer(TO_STRING_ANSWER).when(result).toString();
        // Clear all invocations to avoid confusion when verifying invocations later:
        Mockito.clearInvocations(result);
        return result;
    }

    /**
     * Returns the absolute JCR path for the given relative path segment, considering the parent's path.
     * If the parent is null, the root path ("/") is returned.
     *
     * @param parent the parent item
     * @param relPath the relative path segment
     * @return the absolute path
     * @throws RepositoryException if an error occurs while constructing the path
     */
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
     * Group of reusable Mockito {@link Answer} instances used to implement lightweight JCR semantics on the created node mocks.
     * <p>
     * They are applied in {@link #mockPlainNode(String)} and focus on path calculation, ancestor traversal, session lookup,
     * child/property iteration and dynamic property setting. Each Answer keeps logic minimal while enabling typical test navigation.
     * </p>
     * <p><strong>Behavior summary:</strong></p>
     * <ul>
     *   <li><code>IS_NODE_TYPE_ANSWER</code>: checks primary type name equality.</li>
     *   <li><code>ACCEPT_ANSWER</code>: invokes {@link ItemVisitor#visit(Node)} on the current mock.</li>
     *   <li><code>ANCESTOR_ANSWER</code>: climbs parents until requested depth; throws {@link ItemNotFoundException} on invalid depth.</li>
     *   <li><code>PATH_ANSWER</code>: builds absolute path from parent path + own name.</li>
     *   <li><code>DEPTH_ANSWER</code>: calculates depth relative to root (root = 0).</li>
     *   <li><code>SESSION_ANSWER</code>: inherits session from parent if available.</li>
     *   <li><code>NODE_ANSWER</code>/<code>PROPERTY_ANSWER</code>: resolves relative path by concatenation and retrieving from session.</li>
     *   <li><code>NODES_ANSWER</code>/<code>PROPERTIES_ANSWER</code>: wrap internal collections in Jackrabbit iterator adapters.</li>
     *   <li><code>HAS_NODES_ANSWER</code>/<code>HAS_PROPERTIES_ANSWER</code>: existence checks for internal collections.</li>
     *   <li><code>ITEM_EXISTS_ANSWER</code>: delegates existence check to session via absolute path.</li>
     *   <li><code>ADD_NODE_ANSWER</code>/<code>ADD_NODE_WITH_TYPE_ANSWER</code>: create child node mocks (optionally stub type) beneath current path.</li>
     *   <li>SET_*_PROPERTY answers: create and attach properties of the respective type via the appropriate {@link NodeStubbingOperation} stubProperty overload.</li>
     *   <li><code>TO_STRING_ANSWER</code>: returns a concise identifier containing path and UUID.</li>
     * </ul>
     */
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
    public static final Answer<String> TO_STRING_ANSWER = invocation -> {
        Node node = (Node) invocation.getMock();
        String result = EMPTY;
        try {
            result = node.getPath() + " id:" + node.getIdentifier();
        } catch (RepositoryException e) {
            // ignore
        }
        return result;
    };

    private NodeMockUtils() {
    }

    /**
     * Internal abstract base for mock nodes used by the utility methods. Maintains collections of child nodes and properties
     * allowing iterator answers to operate on predictable in-memory structures. Removal keeps session/item registrations consistent
     * by delegating to {@link SessionStubbingOperation#stubRemoveItem(javax.jcr.Item)}.
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
