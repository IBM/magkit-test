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

import org.apache.jackrabbit.JcrConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubMixinNodeTypes;
import static org.apache.commons.collections4.IteratorUtils.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing NodeStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-20
 */
public class NodeStubbingOperationTest {

    private Node _node;

    @BeforeEach
    public void setUp() throws Exception {
        SessionMockUtils.cleanSession();
        _node = NodeMockUtils.mockNode();
    }

    @Test
    public void testStubJcrSession() throws Exception {
        Session session = mock(Session.class);
        NodeStubbingOperation.stubJcrSession(session).of(_node);
        assertEquals(session, _node.getSession());
        assertEquals(_node, session.getNode("/untitled"));
    }

    @Test
    public void testStubType() throws Exception {
        NodeStubbingOperation.stubType("testType").of(_node);
        assertNotNull(_node.getPrimaryNodeType());
        assertEquals("testType", _node.getPrimaryNodeType().getName());
        assertTrue(_node.getPrimaryNodeType().isNodeType("testType"));
        assertFalse(_node.getPrimaryNodeType().isNodeType("other"));
        assertNotNull(_node.getProperty(JcrConstants.JCR_PRIMARYTYPE));
        assertEquals("testType", _node.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString());
    }

    @Test
    public void testStubTitle() throws Exception {
        NodeStubbingOperation.stubTitle("testTitle").of(_node);
        assertNotNull(_node.getProperty(NodeStubbingOperation.PROPNAME_TITLE));
        assertEquals("testTitle", _node.getProperty(NodeStubbingOperation.PROPNAME_TITLE).getString());
    }

    @Test
    public void testStubProperty() throws Exception {
        // each node always has a nodeType property:
        assertTrue(_node.hasProperties());
        assertEquals(1, toList(_node.getProperties()).size());
        assertEquals("{http://www.jcp.org/jcr/nt/1.0}base", _node.getProperties().nextProperty().getString());
        assertFalse(_node.hasProperty("string"));

        NodeStubbingOperation.stubProperty("string", "Hallo Wolf").of(_node);

        assertTrue(_node.hasProperty("string"));
        assertNotNull(_node.getProperty("string"));
        assertEquals(PropertyType.STRING, _node.getProperty("string").getType());
        assertEquals("Hallo Wolf", _node.getProperty("string").getString());
        assertTrue(_node.hasProperties());
        assertNotNull(_node.getProperties());
        assertEquals(2, toList(_node.getProperties()).size());
        //        assert that we get a not empty Iterator in subsequent calls of getProperties:
        assertEquals(2, toList(_node.getProperties()).size());
        assertEquals("Hallo Wolf", ((Property) toList(_node.getProperties()).get(1)).getString());
    }

    @Test
    public void stubSameNamePropertiesTest() throws RepositoryException {
        // each node always has a nodeType property:
        assertTrue(_node.hasProperties());
        assertEquals(1, toList(_node.getProperties()).size());

        NodeStubbingOperation.stubProperty("string", "Hallo Wolf").of(_node);
        assertEquals(2, toList(_node.getProperties()).size());
        assertEquals("Hallo Wolf", _node.getProperty("string").getString());

        NodeStubbingOperation.stubProperty("string", "Hallo Test").of(_node);
        // same name properties should not accumulate in properties list of node mock:
        assertEquals(2, toList(_node.getProperties()).size());
        assertEquals("Hallo Test", _node.getProperty("string").getString());
        assertEquals("{http://www.jcp.org/jcr/nt/1.0}base", _node.getProperties().nextProperty().getString());
    }

    @Test
    public void testStubLong() throws Exception {
        NodeStubbingOperation.stubProperty("long", 1L).of(_node);
        assertEquals(PropertyType.LONG, _node.getProperty("long").getType());
        assertEquals(1L, _node.getProperty("long").getLong());
        assertEquals(2, toList(_node.getProperties()).size());
    }

    @Test
    public void testStubBoolean() throws Exception {
        NodeStubbingOperation.stubProperty("boolean", Boolean.TRUE).of(_node);
        assertEquals(PropertyType.BOOLEAN, _node.getProperty("boolean").getType());
        assertTrue(_node.getProperty("boolean").getBoolean());

    }

    @Test
    public void testStubDouble() throws Exception {
        NodeStubbingOperation.stubProperty("double", 12345D).of(_node);
        assertEquals(PropertyType.DOUBLE, _node.getProperty("double").getType());
        assertEquals(12345D, _node.getProperty("double").getDouble());
    }

    @Test
    public void testStubCalendar() throws Exception {
        Calendar now = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("calendar", now).of(_node);
        assertEquals(PropertyType.DATE, _node.getProperty("calendar").getType());
        assertEquals(now, _node.getProperty("calendar").getDate());
    }

    @Test
    public void testStubBinary() throws Exception {
        Binary binary = mock(Binary.class);
        NodeStubbingOperation.stubProperty("binary", binary).of(_node);
        assertEquals(PropertyType.BINARY, _node.getProperty("binary").getType());
        assertEquals(binary, _node.getProperty("binary").getBinary());
    }

    @Test
    public void testStubReference() throws Exception {
        Node ref = mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-1").of(ref);
        NodeStubbingOperation.stubProperty("ref", ref).of(_node);
        assertEquals(PropertyType.REFERENCE, _node.getProperty("ref").getType());
        assertEquals(ref, _node.getProperty("ref").getNode());
        assertEquals(2, toList(_node.getProperties()).size());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testStubIdentifier() throws Exception {
        NodeStubbingOperation.stubIdentifier("uuid-1").of(_node);
        assertEquals("uuid-1", _node.getIdentifier());
        assertEquals("uuid-1", _node.getUUID());

        Session session = mock(Session.class);
        NodeStubbingOperation.stubJcrSession(session).of(_node);
        NodeStubbingOperation.stubIdentifier("uuid-2").of(_node);
        assertEquals("uuid-2", _node.getUUID());
        assertNull(session.getNodeByUUID("uuid-1"));
        assertEquals(_node, session.getNodeByUUID("uuid-2"));

        NodeStubbingOperation.stubIdentifier("uuid-3").of(_node);
        assertEquals("uuid-3", _node.getUUID());
        assertNull(session.getNodeByUUID("uuid-2"));
        assertEquals(_node, session.getNodeByUUID("uuid-3"));
    }

    @Test
    public void testStubParent() throws Exception {
        Node parent = NodeMockUtils.mockNode("Hans");
        NodeStubbingOperation.stubParent(parent).of(_node);
        assertEquals("untitled", _node.getName());
        assertEquals("/Hans/untitled", _node.getPath());
        assertEquals(2, _node.getDepth());
    }

    @Test
    public void testStubNode() throws RepositoryException {
        assertNull(_node.getNode("child"));
        assertFalse(_node.hasNode("child"));
        assertFalse(_node.hasNodes());

        Node child = NodeMockUtils.mockPlainNode("child");
        NodeStubbingOperation.stubNode(child).of(_node);
        assertEquals(child, _node.getNode("child"));
        assertEquals(_node, child.getParent());
        assertTrue(_node.hasNode("child"));
        assertTrue(_node.hasNodes());
    }

    @Test
    public void stubNodeWithName() throws RepositoryException {
        assertNull(_node.getNode("child"));

        NodeStubbingOperation op = mock(NodeStubbingOperation.class);
        NodeStubbingOperation.stubNode("child", op).of(_node);
        assertTrue(_node.hasNode("child"));
        assertTrue(_node.hasNodes());
        assertNotNull(_node.getNode("child"));
        assertEquals("child", _node.getNode("child").getName());
        verify(op, times(1)).of(_node.getNode("child"));
    }

    /**
     * Tests if a certain property or a node is still available under the old path after
     * stubbing a new parent node. It should not.
     */
    @Test
    public void testParentOldPath() throws RepositoryException {
        Session session = SessionMockUtils.mockSession("website");
        Property property = PropertyMockUtils.mockProperty("property");

        NodeStubbingOperation.stubProperty(property).of(_node);

        assertEquals(_node, (Node) session.getItem("/untitled"));
        assertEquals(_node, session.getNode("/untitled"));
        assertEquals(property, (Property) session.getItem("/untitled/property"));
        assertEquals(property, session.getProperty("/untitled/property"));

        Node parent = NodeMockUtils.mockNode("Hans");
        NodeStubbingOperation.stubParent(parent).of(_node);
        assertEquals("untitled", _node.getName());
        assertEquals("/Hans/untitled", _node.getPath());
        assertEquals(2, _node.getDepth());

        assertNull(session.getItem("/untitled"));
        assertNull(session.getNode("/untitled"));
        assertNull(session.getItem("/untitled/property"));
        assertNull(session.getProperty("/untitled/property"));

        assertEquals(_node, (Node) session.getItem("/Hans/untitled"));
        assertEquals(_node, session.getNode("/Hans/untitled"));
        assertEquals(property, (Property) session.getItem("/Hans/untitled/property"));
        assertEquals(property, session.getProperty("/Hans/untitled/property"));
    }

    @Test
    public void testStubMixinNodeTypes() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("node");
        assertNotNull(node.getMixinNodeTypes());
        assertEquals(0, node.getMixinNodeTypes().length);

        stubMixinNodeTypes(mock(NodeType.class), mock(NodeType.class)).of(node);
        assertEquals(2, node.getMixinNodeTypes().length);

        stubMixinNodeTypes().of(node);
        assertEquals(0, node.getMixinNodeTypes().length);
    }

    @Test
    public void testPropertyStubbingOfPlainNode() throws RepositoryException {
        Node node  = mock(Node.class);
        assertNull(node.getProperty("test"));
        NodeStubbingOperation.stubProperty("test", "value").of(node);
        assertNotNull(node.getProperty("test"));
    }

    @Test
    public void stubChildNodeOfPlainNode() throws RepositoryException {
        Node child  = mock(Node.class);
        doReturn("child").when(child).getName();
        Node parent  = mock(Node.class);
        assertNull(parent.getNode("child"));
        assertNull(child.getParent());

        NodeStubbingOperation.stubNode(child).of(parent);
        assertEquals(child, parent.getNode("child"));
        assertEquals(parent, child.getParent());
    }
}
