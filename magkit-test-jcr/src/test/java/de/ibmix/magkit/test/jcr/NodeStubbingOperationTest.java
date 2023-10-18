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

import org.junit.Before;
import org.junit.Test;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
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

    @Before
    public void setUp() throws Exception {
        SessionMockUtils.cleanSession();
        _node = NodeMockUtils.mockNode();
    }

    @Test
    public void testStubJcrSession() throws Exception {
        Session session = mock(Session.class);
        NodeStubbingOperation.stubJcrSession(session).of(_node);
        assertThat(_node.getSession(), is(session));
    }

    @Test
    public void testStubType() throws Exception {
        NodeStubbingOperation.stubType("testType").of(_node);
        assertThat(_node.getPrimaryNodeType(), notNullValue());
        assertThat(_node.getPrimaryNodeType().getName(), is("testType"));
        assertThat(_node.getPrimaryNodeType().isNodeType("testType"), is(true));
        assertThat(_node.getPrimaryNodeType().isNodeType("other"), is(false));
    }

    @Test
    public void testStubTitle() throws Exception {
        NodeStubbingOperation.stubTitle("testTitle").of(_node);
        assertThat(_node.getProperty(NodeStubbingOperation.PROPNAME_TITLE), notNullValue());
        assertThat(_node.getProperty(NodeStubbingOperation.PROPNAME_TITLE).getString(), is("testTitle"));
    }

    @Test
    public void testStubProperty() throws Exception {
        // each node always has a nodeType property:
        assertThat(_node.hasProperties(), is(true));
        assertThat(toList(_node.getProperties()).size(), is(1));
        assertThat(_node.getProperties().nextProperty().getString(), is("{http://www.jcp.org/jcr/nt/1.0}base"));
        assertThat(_node.hasProperty("string"), is(false));

        NodeStubbingOperation.stubProperty("string", "Hallo Wolf").of(_node);

        assertThat(_node.hasProperty("string"), is(true));
        assertThat(_node.getProperty("string"), notNullValue());
        assertThat(_node.getProperty("string").getType(), is(PropertyType.STRING));
        assertThat(_node.getProperty("string").getString(), is("Hallo Wolf"));
        assertThat(_node.hasProperties(), is(true));
        assertThat(_node.getProperties(), notNullValue());
        assertThat(toList(_node.getProperties()).size(), is(2));
//        assert that we get a not empty Iterator in subsequent calls of getProperties:
        assertThat(toList(_node.getProperties()).size(), is(2));
        assertThat(((Property) toList(_node.getProperties()).get(1)).getString(), is("Hallo Wolf"));
    }

    @Test
    public void stubSameNamePropertiesTest() throws RepositoryException {
        // each node always has a nodeType property:
        assertThat(_node.hasProperties(), is(true));
        assertThat(toList(_node.getProperties()).size(), is(1));

        NodeStubbingOperation.stubProperty("string", "Hallo Wolf").of(_node);
        assertThat(toList(_node.getProperties()).size(), is(2));
        assertThat(_node.getProperty("string").getString(), is("Hallo Wolf"));

        NodeStubbingOperation.stubProperty("string", "Hallo Test").of(_node);
        // same name properties should not accumulate in properties list of node mock:
        assertThat(toList(_node.getProperties()).size(), is(2));
        assertThat(_node.getProperty("string").getString(), is("Hallo Test"));
        assertThat(_node.getProperties().nextProperty().getString(), is("{http://www.jcp.org/jcr/nt/1.0}base"));
    }

    @Test
    public void testStubLong() throws Exception {
        NodeStubbingOperation.stubProperty("long", 1L).of(_node);
        assertThat(_node.getProperty("long").getType(), is(PropertyType.LONG));
        assertThat(_node.getProperty("long").getLong(), is(1L));
        assertThat(toList(_node.getProperties()).size(), is(2));
    }

    @Test
    public void testStubBoolean() throws Exception {
        NodeStubbingOperation.stubProperty("boolean", Boolean.TRUE).of(_node);
        assertThat(_node.getProperty("boolean").getType(), is(PropertyType.BOOLEAN));
        assertThat(_node.getProperty("boolean").getBoolean(), is(Boolean.TRUE));

    }

    @Test
    public void testStubDouble() throws Exception {
        NodeStubbingOperation.stubProperty("double", 12345D).of(_node);
        assertThat(_node.getProperty("double").getType(), is(PropertyType.DOUBLE));
        assertThat(_node.getProperty("double").getDouble(), is(12345D));
    }

    @Test
    public void testStubCalendar() throws Exception {
        Calendar now = Calendar.getInstance();
        NodeStubbingOperation.stubProperty("calendar", now).of(_node);
        assertThat(_node.getProperty("calendar").getType(), is(PropertyType.DATE));
        assertThat(_node.getProperty("calendar").getDate(), is(now));
    }

    @Test
    public void testStubBinary() throws Exception {
        Binary binary = mock(Binary.class);
        NodeStubbingOperation.stubProperty("binary", binary).of(_node);
        assertThat(_node.getProperty("binary").getType(), is(PropertyType.BINARY));
        assertThat(_node.getProperty("binary").getBinary(), is(binary));
    }

    @Test
    public void testStubReference() throws Exception {
        Node ref = mock(Node.class);
        NodeStubbingOperation.stubIdentifier("uuid-1").of(ref);
        NodeStubbingOperation.stubProperty("ref", ref).of(_node);
        assertThat(_node.getProperty("ref").getType(), is(PropertyType.REFERENCE));
        assertThat(_node.getProperty("ref").getNode(), is(ref));
        assertThat(toList(_node.getProperties()).size(), is(2));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testStubIdentifier() throws Exception {
        NodeStubbingOperation.stubIdentifier("uuid-1").of(_node);
        assertThat(_node.getIdentifier(), is("uuid-1"));
        assertThat(_node.getUUID(), is("uuid-1"));

        Session session = mock(Session.class);
        NodeStubbingOperation.stubJcrSession(session).of(_node);
        NodeStubbingOperation.stubIdentifier("uuid-2").of(_node);
        assertThat(_node.getUUID(), is("uuid-2"));
        assertThat(session.getNodeByUUID("uuid-1"), nullValue());
        assertThat(session.getNodeByUUID("uuid-2"), is(_node));

        NodeStubbingOperation.stubIdentifier("uuid-3").of(_node);
        assertThat(_node.getUUID(), is("uuid-3"));
        assertThat(session.getNodeByUUID("uuid-2"), nullValue());
        assertThat(session.getNodeByUUID("uuid-3"), is(_node));
    }

    @Test
    public void testStubParent() throws Exception {
        Node parent = NodeMockUtils.mockNode("Hans");
        NodeStubbingOperation.stubParent(parent).of(_node);
        assertThat(_node.getName(), is("untitled"));
        assertThat(_node.getPath(), is("/Hans/untitled"));
        assertThat(_node.getDepth(), is(2));
    }

    @Test
    public void testStubNode() throws RepositoryException {
        assertThat(_node.getNode("child"), nullValue());
        assertThat(_node.hasNode("child"), is(false));
        assertThat(_node.hasNodes(), is(false));

        Node child = NodeMockUtils.mockPlainNode("child");
        NodeStubbingOperation.stubNode(child).of(_node);
        assertThat(_node.getNode("child"), is(child));
        assertThat(child.getParent(), is(_node));
        assertThat(_node.hasNode("child"), is(true));
        assertThat(_node.hasNodes(), is(true));
    }

    @Test
    public void stubNodeWithName() throws RepositoryException {
        assertThat(_node.getNode("child"), nullValue());

        NodeStubbingOperation op = mock(NodeStubbingOperation.class);
        NodeStubbingOperation.stubNode("child", op).of(_node);
        assertThat(_node.hasNode("child"), is(true));
        assertThat(_node.hasNodes(), is(true));
        assertThat(_node.getNode("child"), notNullValue());
        assertThat(_node.getNode("child").getName(), is("child"));
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

        assertThat((Node) session.getItem("/untitled"), is(_node));
        assertThat(session.getNode("/untitled"), is(_node));
        assertThat((Property) session.getItem("/untitled/property"), is(property));
        assertThat(session.getProperty("/untitled/property"), is(property));

        Node parent = NodeMockUtils.mockNode("Hans");
        NodeStubbingOperation.stubParent(parent).of(_node);
        assertThat(_node.getName(), is("untitled"));
        assertThat(_node.getPath(), is("/Hans/untitled"));
        assertThat(_node.getDepth(), is(2));

        assertThat(session.getItem("/untitled"), is(nullValue()));
        assertThat(session.getNode("/untitled"), is(nullValue()));
        assertThat(session.getItem("/untitled/property"), is(nullValue()));
        assertThat(session.getProperty("/untitled/property"), is(nullValue()));

        assertThat((Node) session.getItem("/Hans/untitled"), is(_node));
        assertThat(session.getNode("/Hans/untitled"), is(_node));
        assertThat((Property) session.getItem("/Hans/untitled/property"), is(property));
        assertThat(session.getProperty("/Hans/untitled/property"), is(property));
    }

    @Test
    public void testStubMixinNodeTypes() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("node");
        assertThat(node.getMixinNodeTypes(), notNullValue());
        assertThat(node.getMixinNodeTypes().length, is(0));

        stubMixinNodeTypes(mock(NodeType.class), mock(NodeType.class)).of(node);
        assertThat(node.getMixinNodeTypes().length, is(2));

        stubMixinNodeTypes().of(node);
        assertThat(node.getMixinNodeTypes().length, is(0));
    }
}
