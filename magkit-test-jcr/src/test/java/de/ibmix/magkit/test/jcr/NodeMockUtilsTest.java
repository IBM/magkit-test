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

import org.apache.jackrabbit.util.ISO8601;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test the NodeMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-10-31
 */
public class NodeMockUtilsTest {

    @BeforeEach
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void mockNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = mock(NodeStubbingOperation.class);
        NodeStubbingOperation op2 = mock(NodeStubbingOperation.class);
        Node node = NodeMockUtils.mockNode(op1, op2);
        assertNotNull(node);
        assertNotNull(node.getNodes());
        assertFalse(node.getNodes().hasNext());
        assertFalse(node.hasNodes());
        assertNotNull(node.getProperties());
        // we have one property "jcr:primaryType"
        assertTrue(node.getProperties().hasNext());
        assertTrue(node.hasProperties());
        assertTrue(node.isNodeType(NodeType.NT_BASE));
        assertEquals(0, node.getMixinNodeTypes().length);
        assertEquals("/untitled id:" + node.getIdentifier(), node.toString());
        verify(op1, times(1)).of(node);
        verify(op2, times(1)).of(node);

        ItemVisitor visitor = mock(ItemVisitor.class);
        node.accept(visitor);
        verify(visitor, times(1)).visit(node);
    }

    @Test
    public void mockNodeTestHierarchy() throws RepositoryException {
        // build nested node hierarchy
        Node sessionRoot = SessionMockUtils.mockSession("website").getRootNode();
        Node root = NodeMockUtils.mockNode("root/", NodeStubbingOperation.stubProperty("rootProp", "rootValue"));
        Node section = NodeMockUtils.mockNode("root/section", NodeStubbingOperation.stubProperty("sectionProp", "sectionValue"));
        Node page = NodeMockUtils.mockNode("root/section/page", NodeStubbingOperation.stubProperty("pageProp", "pageValue"));

        verifyNodeHierarchyForRoot(sessionRoot, root, section, page);
        verifyNodeHierarchyForSection(sessionRoot, root, section, page);
        verifyNodeHierarchyForPage(sessionRoot, root, section, page);
        verifyNodeHierarchyForSessionRoot(sessionRoot, root, section, page);
        verifyNoLeftoversOfFlatStructure(sessionRoot, root);
    }

    @Test
    public void mockNodeTestHierarchy2() throws RepositoryException {
        Node sessionRoot = SessionMockUtils.mockSession("website").getRootNode();
        // mock a flat node hierarchy
        Node root = NodeMockUtils.mockNode("root/", NodeStubbingOperation.stubProperty("rootProp", "rootValue"));
        Node section = NodeMockUtils.mockNode("section", NodeStubbingOperation.stubProperty("sectionProp", "sectionValue"));
        Node page = NodeMockUtils.mockNode("page", NodeStubbingOperation.stubProperty("pageProp", "pageValue"));
        // assert that hierarchy is flat -> all node are child of session root
        assertTrue(sessionRoot.hasNode("section"));
        assertEquals(section, sessionRoot.getNode("section"));
        assertTrue(sessionRoot.hasNode("page"));
        assertEquals(page, sessionRoot.getNode("page"));

        // build nested node hierarchy
        NodeStubbingOperation.stubNode(page).of(section);
        NodeStubbingOperation.stubNode(section).of(root);

        verifyNodeHierarchyForRoot(sessionRoot, root, section, page);
        verifyNodeHierarchyForSection(sessionRoot, root, section, page);
        verifyNodeHierarchyForPage(sessionRoot, root, section, page);
        verifyNodeHierarchyForSessionRoot(sessionRoot, root, section, page);
        verifyNoLeftoversOfFlatStructure(sessionRoot, root);
    }

    private void verifyNodeHierarchyForRoot(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for root
        assertNotNull(root);
        assertTrue(root.isNode());
        assertEquals("root", root.getName());
        assertEquals("/root", root.getPath());
        assertEquals(1, root.getDepth());
        assertEquals(sessionRoot, root.getParent());
        assertNotNull(root.getNodes());
        assertTrue(root.getNodes().hasNext());
        assertEquals(section, (Node) root.getNodes().next());
        assertEquals(sessionRoot, root.getAncestor(0));
        assertEquals(root, root.getAncestor(1));
        assertTrue(root.hasProperty("rootProp"));
        assertEquals("rootValue", root.getProperty("rootProp").getString());
        assertTrue(root.hasNode("section"));
        assertEquals(section, root.getNode("section"));
        assertTrue(root.hasNode("section/page"));
        assertEquals(page, root.getNode("section/page"));
    }

    private void verifyNodeHierarchyForSection(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for section
        assertNotNull(section);
        assertTrue(section.isNode());
        assertEquals("section", section.getName());
        assertEquals("/root/section", section.getPath());
        assertEquals(2, section.getDepth());
        assertEquals(root, section.getParent());
        assertNotNull(section.getNodes());
        assertTrue(section.getNodes().hasNext());
        assertEquals(page, (Node) section.getNodes().next());
        assertEquals(sessionRoot, section.getAncestor(0));
        assertEquals(root, section.getAncestor(1));
        assertEquals(section, section.getAncestor(2));
        assertTrue(section.hasProperty("sectionProp"));
        assertEquals("sectionValue", section.getProperty("sectionProp").getString());
        assertTrue(section.hasNode("page"));
        assertEquals(page, section.getNode("page"));
    }

    private void verifyNodeHierarchyForPage(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for page
        assertNotNull(page);
        assertTrue(page.isNode());
        assertEquals("page", page.getName());
        assertEquals("/root/section/page", page.getPath());
        assertEquals(3, page.getDepth());
        assertEquals(section, page.getParent());
        assertNotNull(page.getNodes());
        assertFalse(page.getNodes().hasNext());
        assertEquals(sessionRoot, page.getAncestor(0));
        assertEquals(root, page.getAncestor(1));
        assertEquals(section, page.getAncestor(2));
        assertEquals(page, page.getAncestor(3));
        assertTrue(page.hasProperty("pageProp"));
        assertEquals("pageValue", page.getProperty("pageProp").getString());
    }

    private void verifyNodeHierarchyForSessionRoot(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for session root
        assertTrue(sessionRoot.hasNode("root"));
        assertEquals(root, sessionRoot.getNode("root"));
        assertTrue(sessionRoot.hasNode("root/section"));
        assertEquals(section, sessionRoot.getNode("root/section"));
        assertTrue(sessionRoot.hasNode("root/section/page"));
        assertEquals(page, sessionRoot.getNode("root/section/page"));
        assertTrue(sessionRoot.hasProperty("root/rootProp"));
        assertEquals(root.getProperty("rootProp"), sessionRoot.getProperty("root/rootProp"));
        assertTrue(sessionRoot.hasProperty("root/section/sectionProp"));
        assertEquals(section.getProperty("sectionProp"), sessionRoot.getProperty("root/section/sectionProp"));
        assertTrue(sessionRoot.hasProperty("root/section/page/pageProp"));
        assertEquals(page.getProperty("pageProp"), sessionRoot.getProperty("root/section/page/pageProp"));
    }

    private void verifyNoLeftoversOfFlatStructure(Node sessionRoot, Node root) throws RepositoryException {
        // verify that no leftovers of flat structure exist
        assertFalse(sessionRoot.hasNode("section"));
        assertNull(sessionRoot.getNode("section"));
        assertFalse(sessionRoot.hasNode("page"));
        assertNull(sessionRoot.getNode("page"));
        assertFalse(sessionRoot.hasNode("section/page"));
        assertNull(sessionRoot.getNode("section/page"));

        assertFalse(root.hasNode("page"));
        assertNull(root.getNode("page"));
    }


    @Test
    public void mockNodeTestSession() throws RepositoryException {
        Session session = SessionMockUtils.mockSession("website");
        assertNotNull(session.getRootNode());
        // for now we don't need the workspace
        assertNotNull(session.getWorkspace());
        assertEquals("website", session.getWorkspace().getName());
        assertNull(session.getItem("/root"));
        assertNull(session.getItem("/root/section"));
        assertNull(session.getItem("/root/section/page"));
        assertFalse(session.itemExists("/root"));
        assertFalse(session.itemExists("/root/section"));
        assertFalse(session.itemExists("/root/section/page"));

        Item root = NodeMockUtils.mockNode("root/");
        assertEquals(session, root.getSession());
        assertEquals(root, session.getItem("/root"));
        assertNull(session.getItem("/root/section"));
        assertNull(session.getItem("/root/section/page"));
        assertTrue(session.itemExists("/root"));
        assertFalse(session.itemExists("/root/section"));
        assertFalse(session.itemExists("/root/section/page"));

        Item section = NodeMockUtils.mockNode("root/section");
        assertEquals(session, section.getSession());
        assertEquals(root, session.getItem("/root"));
        assertEquals(section, session.getItem("/root/section"));
        assertNull(session.getItem("/root/section/page"));
        assertTrue(session.itemExists("/root"));
        assertTrue(session.itemExists("/root/section"));
        assertFalse(session.itemExists("/root/section/page"));

        Item page = NodeMockUtils.mockNode("root/section/page");
        assertEquals(session, page.getSession());
        assertEquals(root, session.getItem("/root"));
        assertEquals(section, session.getItem("/root/section"));
        assertEquals(page, session.getItem("/root/section/page"));
        assertTrue(session.itemExists("/root"));
        assertTrue(session.itemExists("/root/section"));
        assertTrue(session.itemExists("/root/section/page"));
    }

    @Test
    public void testRelativePathNode() throws Exception {
        Node node1 = NodeMockUtils.mockNode("root/level1/level2/level3");
        Node node2 = NodeMockUtils.mockNode("level4", NodeStubbingOperation.stubParent(node1));
        Node node3 = NodeMockUtils.mockNode("level5", NodeStubbingOperation.stubParent(node2));

        assertEquals("/root/level1/level2/level3/level4/level5", node3.getPath());
        assertEquals(node3, node1.getNode("level4/level5"));

        Session session = SessionMockUtils.mockSession("website");
        Node root = session.getNode("/root");

        assertEquals("/root", root.getPath());
        assertNotNull(root);
        assertEquals(node3, root.getNode("level1/level2/level3/level4/level5"));

    }

    @Test
    public void testNestedNodesWithSameName() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("0/0/0/0");
        assertEquals(4, node.getDepth());
        assertEquals("/0/0/0/0", node.getPath());
    }

    @Test
    public void mockDeepHierarchy() throws RepositoryException {
        NodeMockUtils.mockNode("/1/2/3/4/5/6/7/8/9/10/11/12");
    }

    @Test
    public void mockNodeFromXmlTest() throws RepositoryException {
        Node node = NodeMockUtils.mockNodeFromXml("testRepo", getClass().getResourceAsStream("website.aha.de.rezeptseiten.streusselkuchen.xml"));
        assertNotNull(node);

        assertNotNull(node.getSession());
        assertEquals("testRepo", node.getSession().getWorkspace().getName());
        assertEquals(node, SessionMockUtils.mockSession("testRepo").getNode("/aprikosen-streuselkuchen"));

        assertEquals("aprikosen-streuselkuchen", node.getName());
        assertEquals("/aprikosen-streuselkuchen", node.getPath());
        assertEquals("5a7c0215-fdbe-4ef7-8cc6-dbffded7ae2b", node.getIdentifier());

        assertEquals(PropertyType.NAME, node.getProperty("jcr:primaryType").getType());
        assertEquals("mgnl:page", node.getProperty("jcr:primaryType").getString());

        assertEquals(PropertyType.STRING, node.getProperty("contentCategory").getType());
        assertEquals("recipe", node.getProperty("contentCategory").getString());
        assertEquals("recipe", node.getProperty("contentCategory").getValue().getString());
        assertEquals(1, node.getProperty("contentCategory").getValues().length);

        assertEquals(PropertyType.BOOLEAN, node.getProperty("inheritContext").getType());
        assertEquals("true", node.getProperty("inheritContext").getString());
        assertTrue(node.getProperty("inheritContext").getBoolean());
        assertEquals("true", node.getProperty("inheritContext").getValue().getString());

        assertEquals(PropertyType.DATE, node.getProperty("mgnl:created").getType());
        assertEquals("2014-11-21T16:50:16.228+01:00", node.getProperty("mgnl:created").getString());
        assertEquals(ISO8601.parse("2014-11-21T16:50:16.228+01:00"), node.getProperty("mgnl:created").getDate());
        assertEquals("2014-11-21T16:50:16.228+01:00", node.getProperty("mgnl:created").getValue().getString());

        Node nested = node.getNode("content/01/layouts/0/slides/0");
        assertNotNull(nested);

        assertNotNull(nested.getSession());
        assertEquals("testRepo", nested.getSession().getWorkspace().getName());
        assertEquals(nested, SessionMockUtils.mockSession("testRepo").getNode("/aprikosen-streuselkuchen/content/01/layouts/0/slides/0"));

        assertEquals("0", nested.getName());
        assertEquals("/aprikosen-streuselkuchen/content/01/layouts/0/slides/0", nested.getPath());
        assertEquals(7, nested.getDepth());
        assertEquals("aprikosen-streuselkuchen", nested.getAncestor(1).getName());
        assertEquals(PropertyType.NAME, nested.getProperty("jcr:primaryType").getType());
        assertEquals("mgnl:component", nested.getProperty("jcr:primaryType").getString());
        assertEquals("f344504f-2636-4894-974c-9f44f44fcfe0", nested.getIdentifier());
        assertEquals(PropertyType.STRING, nested.getProperty("mgnl:template").getType());
        assertEquals("m5-tk-campaign:components/contents/sliderTeaser", nested.getProperty("mgnl:template").getString());
    }

    @Test
    public void addNodeTest() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertFalse(node.hasNodes());

        Node grandChild = node.addNode("grand/child");
        assertTrue(node.hasNodes());
        assertNotNull(node.getNode("grand"));
        assertTrue(node.getNode("grand").hasNodes());
        assertEquals("/some/node/grand", node.getNode("grand").getPath());
        assertNotNull(node.getNode("grand").getNode("child"));

        assertEquals(grandChild, node.getNode("grand/child"));
        assertEquals("/some/node/grand/child", node.getNode("grand/child").getPath());
    }

    @Test
    public void addNodeWithType() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertFalse(node.hasNodes());

        Node grandChild = node.addNode("grand/child", "test:Type");
        assertEquals("test:Type", grandChild.getPrimaryNodeType().getName());
        assertTrue(node.hasNodes());
        assertNotNull(node.getNode("grand"));
        assertTrue(node.getNode("grand").hasNodes());
        assertEquals("/some/node/grand", node.getNode("grand").getPath());
        assertEquals(grandChild, node.getNode("grand").getNode("child"));

        assertEquals(grandChild, node.getNode("grand/child"));
        assertEquals("/some/node/grand/child", node.getNode("grand/child").getPath());
        assertEquals("test:Type", node.getNode("grand/child").getPrimaryNodeType().getName());
    }

    @Test
    public void removeTest() throws RepositoryException {
        Node child = NodeMockUtils.mockNode("some/node/child");
        Session session = child.getSession();
        assertNotNull(session.getNode("/some"));
        assertTrue(session.getNode("/some").hasNodes());
        assertNotNull(session.getNode("/some/node"));
        assertNotNull(session.getNode("/some/node/child"));

        Node node = child.getParent();
        node.remove();
        assertNotNull(session.getNode("/some"));
        assertFalse(session.getNode("/some").hasNodes());
        assertNull(session.getNode("/some").getNode("node"));
        assertNull(session.getNode("/some/node"));
        assertNull(session.getNode("/some/node/child"));
    }

    @Test
    public void setPropertyTestBinary() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("binary"));
        Binary bin = mock(Binary.class);
        Property p = node.setProperty("binary", bin);
        assertPropertyBasics(node, "binary", p, PropertyType.BINARY);
        assertEquals(bin, p.getBinary());
    }

    @Test
    public void setPropertyTestBoolean() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("boolean"));
        Property p = node.setProperty("boolean", true);
        assertPropertyBasics(node, "boolean", p, PropertyType.BOOLEAN);
        assertTrue(p.getBoolean());
    }

    @Test
    public void setPropertyTestCalendar() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("calendar"));
        Calendar now = Calendar.getInstance();
        Property p = node.setProperty("calendar", now);
        assertPropertyBasics(node, "calendar", p, PropertyType.DATE);
        assertEquals(now, p.getDate());
    }

    @Test
    public void setPropertyTestDouble() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("double"));
        Property p = node.setProperty("double", 1.2D);
        assertPropertyBasics(node, "double", p, PropertyType.DOUBLE);
        assertEquals(1.2D, p.getDouble());
    }

    @Test
    public void setPropertyTestLong() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("long"));
        Property p = node.setProperty("long", 1L);
        assertPropertyBasics(node, "long", p, PropertyType.LONG);
        assertEquals(1L, p.getLong());
    }

    @Test
    public void setPropertyTestString() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("string"));
        Property p = node.setProperty("string", "value");
        assertPropertyBasics(node, "string", p, PropertyType.STRING);
        assertEquals("value", p.getString());
    }

    @Test
    public void setPropertyTestStrings() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("string"));
        Property p = node.setProperty("string", new String[]{"value1", "value2", "value3"});
        assertPropertyBasics(node, "string", p, PropertyType.STRING);
        assertEquals("value1", p.getString());
        assertEquals(3, p.getValues().length);
        assertEquals("value3", p.getValues()[2].getString());
    }

    @Test
    public void setPropertyTestNode() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        Node child = NodeMockUtils.mockNode("other");
        assertNull(node.getProperty("link"));
        Property p = node.setProperty("link", child);
        assertPropertyBasics(node, "link", p, PropertyType.REFERENCE);
        assertEquals(child.getIdentifier(), p.getString());
    }

    @Test
    public void setPropertyTestValue() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("value"));
        Value value = mock(Value.class);
        Property p = node.setProperty("value", value);
        assertPropertyBasics(node, "value", p, PropertyType.UNDEFINED);
        assertEquals(value, p.getValue());
    }

    @Test
    public void setPropertyTestValues() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertNull(node.getProperty("values"));
        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        Property p = node.setProperty("values", new Value[] {v1, v2});
        assertPropertyBasics(node, "values", p, PropertyType.UNDEFINED);
        assertEquals(2, p.getValues().length);
        assertEquals(v1, p.getValues()[0]);
        assertEquals(v2, p.getValues()[1]);
    }

    @Test
    public void sanitizeHandle() {
        assertEquals("/untitled", NodeMockUtils.sanitizeHandle(null));
        assertEquals("/untitled", NodeMockUtils.sanitizeHandle(""));
        assertEquals("/untitled", NodeMockUtils.sanitizeHandle("  \n \t "));
        assertEquals("handle", NodeMockUtils.sanitizeHandle("  \n handle \t "));
        assertEquals("/some/path", NodeMockUtils.sanitizeHandle("  \\some\\path "));
    }

    @Test
    public void getAncestorForNegativeIndex() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertThrows(ItemNotFoundException.class, () -> node.getAncestor(-1));
    }

    @Test
    public void getAncestorForLargeIndex() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertEquals("/", node.getAncestor(0).getPath());
        assertEquals("/some", node.getAncestor(1).getPath());
        assertEquals("/some/node", node.getAncestor(2).getPath());
        assertThrows(ItemNotFoundException.class, () -> node.getAncestor(3));
    }

    @Test
    public void mockNodeBlankPathDefaultsUntitled() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("");
        assertEquals("untitled", node.getName());
        assertEquals("/untitled", node.getPath());
    }

    /**
     * Verify that calling mockNode twice for the same repository/path returns the same instance and applies additional stubbings.
     */
    @Test
    public void mockNodeRepositoryExistingNodeReused() throws RepositoryException {
        Node first = NodeMockUtils.mockNode("repoA", "/reuse/path", NodeStubbingOperation.stubProperty("p1", "v1"));
        Node second = NodeMockUtils.mockNode("repoA", "/reuse/path", NodeStubbingOperation.stubProperty("p2", "v2"));
        assertTrue(first == second);
        assertEquals("v1", second.getProperty("p1").getString());
        assertEquals("v2", second.getProperty("p2").getString());
    }

    /**
     * addNode with blank name should return null and not modify children.
     */
    @Test
    public void addNodeBlankNameReturnsNull() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("some/node");
        assertFalse(parent.hasNodes());
        Node added = parent.addNode("");
        assertNull(added);
        assertFalse(parent.hasNodes());
    }

    /**
     * addNode with blank type should keep default NT_BASE primary type.
     */
    @Test
    public void addNodeBlankTypeKeepsDefaultPrimaryType() throws RepositoryException {
        Node parent = NodeMockUtils.mockNode("type/test");
        Node child = parent.addNode("child", "");
        assertEquals(NodeType.NT_BASE, child.getPrimaryNodeType().getName());
    }

    /**
     * Test overloaded setProperty variants with explicit type parameter.
     */
    @Test
    public void setPropertyVariantsWithTypeParameter() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        Property p1 = node.setProperty("s1", "value", PropertyType.STRING);
        assertEquals(PropertyType.STRING, p1.getType());
        assertEquals("value", node.getProperty("s1").getString());

        Property p2 = node.setProperty("s2", new String[]{"a", "b"}, PropertyType.STRING);
        assertEquals(PropertyType.STRING, p2.getType());
        assertEquals(2, node.getProperty("s2").getValues().length);
        assertEquals("b", node.getProperty("s2").getValues()[1].getString());

        Value v = mock(Value.class);
        Property p3 = node.setProperty("s3", v, PropertyType.UNDEFINED);
        assertEquals(PropertyType.UNDEFINED, p3.getType());
        assertEquals(v, node.getProperty("s3").getValue());

        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        Property p4 = node.setProperty("s4", new Value[]{v1, v2}, PropertyType.UNDEFINED);
        assertEquals(2, p4.getValues().length);
        assertEquals(v1, node.getProperty("s4").getValues()[0]);
        assertEquals(v2, node.getProperty("s4").getValues()[1]);
        assertEquals(PropertyType.UNDEFINED, p4.getType());
    }

    /**
     * Re-stub the identifier and ensure session mappings for old id are cleared.
     */
    @Test
    public void stubIdentifierReassignmentUpdatesSession() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("id/test");
        Session session = node.getSession();
        String oldId = node.getIdentifier();
        assertEquals(node, session.getNodeByIdentifier(oldId));
        NodeStubbingOperation.stubIdentifier("new-identifier-123").of(node);
        assertEquals("new-identifier-123", node.getIdentifier());
        assertEquals(node, session.getNodeByIdentifier("new-identifier-123"));
        assertNull(session.getNodeByIdentifier(oldId));
    }

    /**
     * Invalid XML should throw a RuntimeException in mockNodeFromXml.
     */
    @Test
    public void mockNodeFromXmlInvalidInputThrows() {
        java.io.InputStream is = new java.io.ByteArrayInputStream("<not><valid>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertThrows(RuntimeException.class, () -> NodeMockUtils.mockNodeFromXml("badRepo", is));
    }

    /**
     * Direct test of getPathForParent(null, relPath) returning root path.
     */
    @Test
    public void getPathForParentNullReturnsRoot() throws RepositoryException {
        assertEquals("/", NodeMockUtils.getPathForParent(null, "anything"));
    }

    private void assertPropertyBasics(Node node, String name, Property property, int type) throws RepositoryException {
        assertEquals(type, property.getType());
        assertEquals(property, node.getProperty(name));
    }
}
