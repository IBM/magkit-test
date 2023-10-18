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
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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

    @Before
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void mockNodeTest() throws RepositoryException {
        NodeStubbingOperation op1 = mock(NodeStubbingOperation.class);
        NodeStubbingOperation op2 = mock(NodeStubbingOperation.class);
        Node node = NodeMockUtils.mockNode(op1, op2);
        assertThat(node, notNullValue());
        assertThat(node.getNodes(), notNullValue());
        assertThat(node.getNodes().hasNext(), is(false));
        assertThat(node.hasNodes(), is(false));
        assertThat(node.getProperties(), notNullValue());
        // we have one property "jcr:primaryType"
        assertThat(node.getProperties().hasNext(), is(true));
        assertThat(node.hasProperties(), is(true));
        assertThat(node.isNodeType(NodeType.NT_BASE), is(true));
        assertThat(node.getMixinNodeTypes().length, is(0));
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
        assertThat(sessionRoot.hasNode("section"), is(true));
        assertThat(sessionRoot.getNode("section"), is(section));
        assertThat(sessionRoot.hasNode("page"), is(true));
        assertThat(sessionRoot.getNode("page"), is(page));

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
        assertThat(root, notNullValue());
        assertThat(root.isNode(), is(true));
        assertThat(root.getName(), is("root"));
        assertThat(root.getPath(), is("/root"));
        assertThat(root.getDepth(), is(1));
        assertThat(root.getParent(), is(sessionRoot));
        assertThat(root.getNodes(), notNullValue());
        assertThat(root.getNodes().hasNext(), is(true));
        assertThat((Node) root.getNodes().next(), is(section));
        assertThat(root.getAncestor(0), is(sessionRoot));
        assertThat(root.getAncestor(1), is(root));
        assertThat(root.hasProperty("rootProp"), is(true));
        assertThat(root.getProperty("rootProp").getString(), is("rootValue"));
        assertThat(root.hasNode("section"), is(true));
        assertThat(root.getNode("section"), is(section));
        assertThat(root.hasNode("section/page"), is(true));
        assertThat(root.getNode("section/page"), is(page));
    }

    private void verifyNodeHierarchyForSection(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for section
        assertThat(section, notNullValue());
        assertThat(section.isNode(), is(true));
        assertThat(section.getName(), is("section"));
        assertThat(section.getPath(), is("/root/section"));
        assertThat(section.getDepth(), is(2));
        assertThat(section.getParent(), is(root));
        assertThat(section.getNodes(), notNullValue());
        assertThat(section.getNodes().hasNext(), is(true));
        assertThat((Node) section.getNodes().next(), is(page));
        assertThat(section.getAncestor(0), is(sessionRoot));
        assertThat(section.getAncestor(1), is(root));
        assertThat(section.getAncestor(2), is(section));
        assertThat(section.hasProperty("sectionProp"), is(true));
        assertThat(section.getProperty("sectionProp").getString(), is("sectionValue"));
        assertThat(section.hasNode("page"), is(true));
        assertThat(section.getNode("page"), is(page));
    }

    private void verifyNodeHierarchyForPage(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for page
        assertThat(page, notNullValue());
        assertThat(page.isNode(), is(true));
        assertThat(page.getName(), is("page"));
        assertThat(page.getPath(), is("/root/section/page"));
        assertThat(page.getDepth(), is(3));
        assertThat(page.getParent(), is(section));
        assertThat(page.getNodes(), notNullValue());
        assertThat(page.getNodes().hasNext(), is(false));
        assertThat(page.getAncestor(0), is(sessionRoot));
        assertThat(page.getAncestor(1), is(root));
        assertThat(page.getAncestor(2), is(section));
        assertThat(page.getAncestor(3), is(page));
        assertThat(page.hasProperty("pageProp"), is(true));
        assertThat(page.getProperty("pageProp").getString(), is("pageValue"));
    }

    private void verifyNodeHierarchyForSessionRoot(Node sessionRoot, Node root, Node section, Node page) throws RepositoryException {
        // verify node hierarchy for session root
        assertThat(sessionRoot.hasNode("root"), is(true));
        assertThat(sessionRoot.getNode("root"), is(root));
        assertThat(sessionRoot.hasNode("root/section"), is(true));
        assertThat(sessionRoot.getNode("root/section"), is(section));
        assertThat(sessionRoot.hasNode("root/section/page"), is(true));
        assertThat(sessionRoot.getNode("root/section/page"), is(page));
        assertThat(sessionRoot.hasProperty("root/rootProp"), is(true));
        assertThat(sessionRoot.getProperty("root/rootProp"), is(root.getProperty("rootProp")));
        assertThat(sessionRoot.hasProperty("root/section/sectionProp"), is(true));
        assertThat(sessionRoot.getProperty("root/section/sectionProp"), is(section.getProperty("sectionProp")));
        assertThat(sessionRoot.hasProperty("root/section/page/pageProp"), is(true));
        assertThat(sessionRoot.getProperty("root/section/page/pageProp"), is(page.getProperty("pageProp")));
    }

    private void verifyNoLeftoversOfFlatStructure(Node sessionRoot, Node root) throws RepositoryException {
        // verify that no leftovers of flat structure exist
        assertThat(sessionRoot.hasNode("section"), is(false));
        assertThat(sessionRoot.getNode("section"), nullValue());
        assertThat(sessionRoot.hasNode("page"), is(false));
        assertThat(sessionRoot.getNode("page"), nullValue());
        assertThat(sessionRoot.hasNode("section/page"), is(false));
        assertThat(sessionRoot.getNode("section/page"), nullValue());

        assertThat(root.hasNode("page"), is(false));
        assertThat(root.getNode("page"), nullValue());
    }


    @Test
    public void mockNodeTestSession() throws RepositoryException {
        Session session = SessionMockUtils.mockSession("website");
        assertThat(session.getRootNode(), notNullValue());
        // for now we don't need the workspace
        assertThat(session.getWorkspace(), notNullValue());
        assertThat(session.getWorkspace().getName(), is("website"));
        assertThat(session.getItem("/root"), nullValue());
        assertThat(session.getItem("/root/section"), nullValue());
        assertThat(session.getItem("/root/section/page"), nullValue());
        assertThat(session.itemExists("/root"), is(false));
        assertThat(session.itemExists("/root/section"), is(false));
        assertThat(session.itemExists("/root/section/page"), is(false));

        Item root = NodeMockUtils.mockNode("root/");
        assertThat(root.getSession(), is(session));
        assertThat(session.getItem("/root"), is(root));
        assertThat(session.getItem("/root/section"), nullValue());
        assertThat(session.getItem("/root/section/page"), nullValue());
        assertThat(session.itemExists("/root"), is(true));
        assertThat(session.itemExists("/root/section"), is(false));
        assertThat(session.itemExists("/root/section/page"), is(false));

        Item section = NodeMockUtils.mockNode("root/section");
        assertThat(section.getSession(), is(session));
        assertThat(session.getItem("/root"), is(root));
        assertThat(session.getItem("/root/section"), is(section));
        assertThat(session.getItem("/root/section/page"), nullValue());
        assertThat(session.itemExists("/root"), is(true));
        assertThat(session.itemExists("/root/section"), is(true));
        assertThat(session.itemExists("/root/section/page"), is(false));

        Item page = NodeMockUtils.mockNode("root/section/page");
        assertThat(page.getSession(), is(session));
        assertThat(session.getItem("/root"), is(root));
        assertThat(session.getItem("/root/section"), is(section));
        assertThat(session.getItem("/root/section/page"), is(page));
        assertThat(session.itemExists("/root"), is(true));
        assertThat(session.itemExists("/root/section"), is(true));
        assertThat(session.itemExists("/root/section/page"), is(true));
    }

    @Test
    public void testRelativePathNode() throws Exception {
        Node node1 = NodeMockUtils.mockNode("root/level1/level2/level3");
        Node node2 = NodeMockUtils.mockNode("level4", NodeStubbingOperation.stubParent(node1));
        Node node3 = NodeMockUtils.mockNode("level5", NodeStubbingOperation.stubParent(node2));

        assertThat(node3.getPath(), is("/root/level1/level2/level3/level4/level5"));
        assertThat(node1.getNode("level4/level5"), is(node3));

        Session session = SessionMockUtils.mockSession("website");
        Node root = session.getNode("/root");

        assertThat(root.getPath(), is("/root"));
        assertThat(root, notNullValue());
        assertThat(root.getNode("level1/level2/level3/level4/level5"), is(node3));

    }

    @Test
    public void testNestedNodesWithSameName() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("0/0/0/0");
        assertThat(node.getDepth(), is(4));
        assertThat(node.getPath(), is("/0/0/0/0"));
    }

    @Test
    public void mockDeepHierarchy() throws RepositoryException {
        NodeMockUtils.mockNode("/1/2/3/4/5/6/7/8/9/10/11/12");
    }

    @Test
    public void mockNodeFromXmlTest() throws RepositoryException {
        Node node = NodeMockUtils.mockNodeFromXml("testRepo", getClass().getResourceAsStream("website.aha.de.rezeptseiten.streusselkuchen.xml"));
        assertThat(node, notNullValue());

        assertThat(node.getSession(), notNullValue());
        assertThat(node.getSession().getWorkspace().getName(), is("testRepo"));
        MatcherAssert.assertThat(SessionMockUtils.mockSession("testRepo").getNode("/aprikosen-streuselkuchen"), is(node));

        assertThat(node.getName(), is("aprikosen-streuselkuchen"));
        assertThat(node.getPath(), is("/aprikosen-streuselkuchen"));
        assertThat(node.getIdentifier(), is("5a7c0215-fdbe-4ef7-8cc6-dbffded7ae2b"));

        assertThat(node.getProperty("jcr:primaryType").getType(), is(PropertyType.NAME));
        assertThat(node.getProperty("jcr:primaryType").getString(), is("mgnl:page"));

        assertThat(node.getProperty("contentCategory").getType(), is(PropertyType.STRING));
        assertThat(node.getProperty("contentCategory").getString(), is("recipe"));
        assertThat(node.getProperty("contentCategory").getValue().getString(), is("recipe"));
        assertThat(node.getProperty("contentCategory").getValues().length, is(1));

        assertThat(node.getProperty("inheritContext").getType(), is(PropertyType.BOOLEAN));
        assertThat(node.getProperty("inheritContext").getString(), is("true"));
        assertThat(node.getProperty("inheritContext").getBoolean(), is(true));
        assertThat(node.getProperty("inheritContext").getValue().getString(), is("true"));
        assertThat(node.getProperty("contentCategory").getValues().length, is(1));

        assertThat(node.getProperty("mgnl:created").getType(), is(PropertyType.DATE));
        assertThat(node.getProperty("mgnl:created").getString(), is("2014-11-21T16:50:16.228+01:00"));
        assertThat(node.getProperty("mgnl:created").getDate(), is(ISO8601.parse("2014-11-21T16:50:16.228+01:00")));
        assertThat(node.getProperty("mgnl:created").getValue().getString(), is("2014-11-21T16:50:16.228+01:00"));
        assertThat(node.getProperty("contentCategory").getValues().length, is(1));

        Node nested = node.getNode("content/01/layouts/0/slides/0");
        assertThat(nested, notNullValue());

        assertThat(nested.getSession(), notNullValue());
        assertThat(nested.getSession().getWorkspace().getName(), is("testRepo"));
        MatcherAssert.assertThat(SessionMockUtils.mockSession("testRepo").getNode("/aprikosen-streuselkuchen/content/01/layouts/0/slides/0"), is(nested));

        assertThat(nested.getName(), is("0"));
        assertThat(nested.getPath(), is("/aprikosen-streuselkuchen/content/01/layouts/0/slides/0"));
        assertThat(nested.getDepth(), is(7));
        assertThat(nested.getAncestor(1).getName(), is("aprikosen-streuselkuchen"));
        assertThat(nested.getProperty("jcr:primaryType").getType(), is(PropertyType.NAME));
        assertThat(nested.getProperty("jcr:primaryType").getString(), is("mgnl:component"));
        assertThat(nested.getIdentifier(), is("f344504f-2636-4894-974c-9f44f44fcfe0"));
        assertThat(nested.getProperty("mgnl:template").getType(), is(PropertyType.STRING));
        assertThat(nested.getProperty("mgnl:template").getString(), is("m5-tk-campaign:components/contents/sliderTeaser"));
    }

    @Test
    public void addNodeTest() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertThat(node.hasNodes(), is(false));

        Node grandChild = node.addNode("grand/child");
        assertThat(node.hasNodes(), is(true));
        assertThat(node.getNode("grand"), notNullValue());
        assertThat(node.getNode("grand").hasNodes(), is(true));
        assertThat(node.getNode("grand").getPath(), is("/some/node/grand"));
        assertThat(node.getNode("grand").getNode("child"), notNullValue());

        assertThat(node.getNode("grand/child"), is(grandChild));
        assertThat(node.getNode("grand/child").getPath(), is("/some/node/grand/child"));
    }

    @Test
    public void addNodeWithType() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertThat(node.hasNodes(), is(false));

        Node grandChild = node.addNode("grand/child", "test:Type");
        assertThat(grandChild.getPrimaryNodeType().getName(), is("test:Type"));
        assertThat(node.hasNodes(), is(true));
        assertThat(node.getNode("grand"), notNullValue());
        assertThat(node.getNode("grand").hasNodes(), is(true));
        assertThat(node.getNode("grand").getPath(), is("/some/node/grand"));
        assertThat(node.getNode("grand").getNode("child"), is(grandChild));

        assertThat(node.getNode("grand/child"), is(grandChild));
        assertThat(node.getNode("grand/child").getPath(), is("/some/node/grand/child"));
        assertThat(node.getNode("grand/child").getPrimaryNodeType().getName(), is("test:Type"));
    }

    @Test
    public void removeTest() throws RepositoryException {
        Node child = NodeMockUtils.mockNode("some/node/child");
        Session session = child.getSession();
        assertThat(session.getNode("/some"), notNullValue());
        assertThat(session.getNode("/some").hasNodes(), is(true));
        assertThat(session.getNode("/some/node"), notNullValue());
        assertThat(session.getNode("/some/node/child"), notNullValue());

        Node node = child.getParent();
        node.remove();
        assertThat(session.getNode("/some"), notNullValue());
        assertThat(session.getNode("/some").hasNodes(), is(false));
        assertThat(session.getNode("/some").getNode("node"), nullValue());
        assertThat(session.getNode("/some/node"), nullValue());
        assertThat(session.getNode("/some/node/child"), nullValue());
    }

    @Test
    public void setPropertyTestBinary() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("binary"), nullValue());
        Binary bin = mock(Binary.class);
        Property p = node.setProperty("binary", bin);
        assertThat(p.getType(), is(PropertyType.BINARY));
        assertThat(node.getProperty("binary"), is(p));
        assertThat(node.getProperty("binary").getBinary(), is(bin));
    }

    @Test
    public void setPropertyTestBoolean() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("boolean"), nullValue());
        Property p = node.setProperty("boolean", true);
        assertThat(p.getType(), is(PropertyType.BOOLEAN));
        assertThat(node.getProperty("boolean"), is(p));
        assertThat(node.getProperty("boolean").getBoolean(), is(true));
    }

    @Test
    public void setPropertyTestCalendar() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("calendar"), nullValue());
        Calendar now = Calendar.getInstance();
        Property p = node.setProperty("calendar", now);
        assertThat(p.getType(), is(PropertyType.DATE));
        assertThat(node.getProperty("calendar"), is(p));
        assertThat(node.getProperty("calendar").getDate(), is(now));
    }

    @Test
    public void setPropertyTestDouble() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("double"), nullValue());
        Property p = node.setProperty("double", 1.2D);
        assertThat(p.getType(), is(PropertyType.DOUBLE));
        assertThat(node.getProperty("double"), is(p));
        assertThat(node.getProperty("double").getDouble(), is(1.2D));
    }

    @Test
    public void setPropertyTestLong() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("long"), nullValue());
        Property p = node.setProperty("long", 1L);
        assertThat(p.getType(), is(PropertyType.LONG));
        assertThat(node.getProperty("long"), is(p));
        assertThat(node.getProperty("long").getLong(), is(1L));
    }

    @Test
    public void setPropertyTestString() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("string"), nullValue());
        Property p = node.setProperty("string", "value");
        assertThat(p.getType(), is(PropertyType.STRING));
        assertThat(node.getProperty("string"), is(p));
        assertThat(node.getProperty("string").getString(), is("value"));
    }

    @Test
    public void setPropertyTestStrings() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("string"), nullValue());
        Property p = node.setProperty("string", new String[]{"value1", "value2", "value3"});
        assertThat(p.getType(), is(PropertyType.STRING));
        assertThat(node.getProperty("string"), is(p));
        assertThat(node.getProperty("string").getString(), is("value1"));
        assertThat(node.getProperty("string").getValues().length, is(3));
        assertThat(node.getProperty("string").getValues()[2].getString(), is("value3"));
    }

    @Test
    public void setPropertyTestNode() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        Node child = NodeMockUtils.mockNode("other");
        assertThat(node.getProperty("link"), nullValue());
        Property p = node.setProperty("link", child);
        assertThat(p.getType(), is(PropertyType.REFERENCE));
        assertThat(node.getProperty("link"), is(p));
        assertThat(node.getProperty("link").getString(), is(child.getIdentifier()));
    }

    @Test
    public void setPropertyTestValue() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("value"), nullValue());

        Value value = mock(Value.class);
        Property p = node.setProperty("value", value);
        assertThat(node.getProperty("value"), is(p));
        assertThat(node.getProperty("value").getValue(), is(value));
        assertThat(node.getProperty("value").getType(), is(PropertyType.UNDEFINED));
    }

    @Test
    public void setPropertyTestValues() throws RepositoryException {
        Node node = NodeMockUtils.mockNode();
        assertThat(node.getProperty("values"), nullValue());

        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        Property p = node.setProperty("values", new Value[] {v1, v2});
        assertThat(node.getProperty("values"), is(p));
        assertThat(node.getProperty("values").getValues().length, is(2));
        assertThat(node.getProperty("values").getValues()[0], is(v1));
        assertThat(node.getProperty("values").getValues()[1], is(v2));
        assertThat(node.getProperty("values").getType(), is(PropertyType.UNDEFINED));
    }

    @Test
    public void sanitizeHandle() {
        assertThat(NodeMockUtils.sanitizeHandle(null), is("/untitled"));
        assertThat(NodeMockUtils.sanitizeHandle(""), is("/untitled"));
        assertThat(NodeMockUtils.sanitizeHandle("  \n \t "), is("/untitled"));
        assertThat(NodeMockUtils.sanitizeHandle("  \n handle \t "), is("handle"));
        assertThat(NodeMockUtils.sanitizeHandle("  \\some\\path "), is("/some/path"));
    }

    @Test(expected = ItemNotFoundException.class)
    public void getAncestorForNegativeIndex() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        node.getAncestor(-1);
    }

    @Test(expected = ItemNotFoundException.class)
    public void getAncestorForLargeIndex() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("some/node");
        assertThat(node.getAncestor(0).getPath(), is("/"));
        assertThat(node.getAncestor(1).getPath(), is("/some"));
        assertThat(node.getAncestor(2).getPath(), is("/some/node"));
        // expect ItemNotFoundException
        node.getAncestor(3);
    }
}
