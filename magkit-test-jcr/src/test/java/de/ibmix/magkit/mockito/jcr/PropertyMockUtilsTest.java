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

import org.junit.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.mockito.jcr.PropertyMockUtils.mockProperty;
import static de.ibmix.magkit.mockito.jcr.ValueMockUtils.mockValue;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing PropertyMockUtils.
 *
 * @author wolf.bubenik
 * @since 13.11.12
 */
public class PropertyMockUtilsTest {

    @Test
    public void testMockPropertyString() throws Exception {
        Property p = mockProperty("name", "v1", "v2");
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.STRING));
        assertThat(p.getString(), is("v1"));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getString(), is("v1"));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyBinary() throws Exception {
        Binary bin1 = mock(Binary.class);
        Binary bin2 = mock(Binary.class);
        InputStream bin1Stream = mock(InputStream.class);
        Mockito.when(bin1.getStream()).thenReturn(bin1Stream);
        Property p = mockProperty("name", bin1, bin2);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.BINARY));
        assertThat(p.getBinary(), is(bin1));
        assertThat(p.getStream(), is(bin1Stream));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getBinary(), is(bin1));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyInputStream() throws Exception {
        InputStream bin1 = mock(InputStream.class);
        InputStream bin2 = mock(InputStream.class);
        Property p = mockProperty("name", bin1, bin2);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.BINARY));
        assertThat(p.getStream(), is(bin1));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getStream(), is(bin1));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyBoolean() throws Exception {
        Property p = mockProperty("name", true, false, false);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.BOOLEAN));
        assertThat(p.getBoolean(), is(true));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getBoolean(), is(true));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(3));
    }

    @Test
    public void testMockPropertyCalendar() throws Exception {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        Property p = mockProperty("name", cal1, cal2);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.DATE));
        assertThat(p.getDate(), is(cal1));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getDate(), is(cal1));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyDouble() throws Exception {
        Property p = mockProperty("name", 2D, 3D);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.DOUBLE));
        assertThat(p.getDouble(), is(2D));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getDouble(), is(2D));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyLong() throws Exception {
        Property p = mockProperty("name", 2L, 3L);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.LONG));
        assertThat(p.getLong(), is(2L));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getLong(), is(2L));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
    }

    @Test
    public void testMockPropertyValue() throws Exception {
        Value v1 = mockValue("value1");
        Value v2 = mockValue("value2");
        Property p = mockProperty("name", v1, v2);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.STRING));
        assertThat(p.getString(), is("value1"));
        assertThat(p.getValue(), is(v1));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(2));
        assertThat(p.getValues()[0], is(v1));
        assertThat(p.getValues()[1], is(v2));
    }


    @Test
    public void testMockPropertyNode() throws Exception {
        Node value = mockNode("testNode");
        Property p = mockProperty("name", value, PropertyType.PATH);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.PATH));
        assertThat(p.getNode(), is(value));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getString(), is("/testNode"));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(1));
    }

    @Test
    public void testMockPropertyContent() throws Exception {
        Node content = NodeMockUtils.mockNode("/root/test", stubIdentifier("uuid-1"));
        Property p = mockProperty("reference", content);
        assertThat(p, notNullValue());
        assertThat(p.getType(), is(PropertyType.REFERENCE));
        assertThat(p.getNode(), is(content));
        assertThat(p.getNode().getName(), is("test"));
        assertThat(p.getValue(), notNullValue());
        assertThat(p.getValue().getString(), is("uuid-1"));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(1));
    }

    @Test
    public void testMockProperty() throws Exception {
        Property p = mockProperty("name");
        assertThat(p, notNullValue());
        assertThat(p.getName(), is("name"));
        assertThat(p.getBoolean(), is(false));
        assertThat(p.getDate(), nullValue());
        assertThat(p.getString(), nullValue());
        assertThat(p.getBinary(), nullValue());
        assertThat(p.getDecimal(), nullValue());
        assertThat(p.getDouble(), is(0.0));
        assertThat(p.getLong(), is(0L));
        assertThat(p.getValues(), notNullValue());
        assertThat(p.getValues().length, is(0));
        assertThat(p.getType(), is(PropertyType.UNDEFINED));

        ItemVisitor visitor = mock(ItemVisitor.class);
        p.accept(visitor);
        verify(visitor, times(1)).visit(p);
    }

    @Test
    public void testMockPropertyParent() throws Exception {
        Property property = mockProperty("property", "test");
        Node node = mockNode("node", stubProperty(property));
        assertThat(property.getParent(), is(node));
    }

    @Test
    public void testMockPropertyDepthAndPath() throws Exception {
        Property property = mockProperty("property", "test");
        Node parent = mockNode("parent");
        Node node = mockNode("node", stubProperty(property), NodeStubbingOperation.stubParent(parent));

        assertThat(property.getDepth(), is(3));
        assertThat(property.getPath(), is("/parent/node/property"));

        Session session = SessionMockUtils.mockSession("website");
        assertThat((Node) session.getItem("/parent/node"), is(node));
        assertThat((Property) session.getItem("/parent/node/property"), is(property));
    }

    @Test
    public void testGetProperty() throws Exception {
        Session session = SessionMockUtils.mockSession("website");
        Node node1 = mockNode("root/level1/level2/level3/level4");
        Property prop1 = mockProperty("prop1", "12345");
        Property prop2 = mockProperty("prop2", "67890");

        stubProperty(prop1).of(node1);
        stubProperty(prop2).of(node1);

        assertThat(prop1.getPath(), is("/root/level1/level2/level3/level4/prop1"));

        assertThat(session.getProperty("/root/level1/level2/level3/level4/prop1"), is(prop1));
        assertThat(session.getProperty("/root/level1/level2/level3/level4/prop2"), is(prop2));

        Node node2 = session.getNode("/root/level1/level2");

        assertThat(node2.getProperty("level3/level4/prop1"), is(prop1));
        assertThat(node2.getProperty("level3/level4/prop2"), is(prop2));
    }

    @Test
    public void removeTest() throws RepositoryException {
        Node node3 = mockNode("root/level1/level2/level3");
        Property prop1 = mockProperty("prop1", "12345");
        Property prop2 = mockProperty("prop2", "67890");

        stubProperty(prop1).of(node3);
        stubProperty(prop2).of(node3);

        assertThat(node3.getProperty("prop1"), is(prop1));
        assertThat(node3.getProperty("prop2"), is(prop2));
        assertThat(node3.hasProperty("prop1"), is(true));
        assertThat(node3.hasProperty("prop2"), is(true));
        // we always have an primary type property
        assertThat(node3.getProperties().getSize(), is(3L));

        Session session = node3.getSession();
        assertThat(session.getProperty("/root/level1/level2/level3/prop1"), is(prop1));
        assertThat(session.getProperty("/root/level1/level2/level3/prop2"), is(prop2));
        assertThat(session.getItem("/root/level1/level2/level3/prop1"), is((Item) prop1));
        assertThat(session.getItem("/root/level1/level2/level3/prop2"), is((Item) prop2));

        assertThat(session.itemExists("/root/level1/level2/level3/prop1"), is(true));
        assertThat(session.itemExists("/root/level1/level2/level3/prop2"), is(true));

        // now remove one property:
        prop1.remove();

        assertThat(node3.getProperty("prop1"), nullValue());
        assertThat(node3.getProperty("prop2"), is(prop2));
        assertThat(node3.hasProperty("prop1"), is(false));
        assertThat(node3.hasProperty("prop2"), is(true));
        // we always have an primary type property
        assertThat(node3.getProperties().getSize(), is(2L));

        assertThat(session.getProperty("/root/level1/level2/level3/prop1"), nullValue());
        assertThat(session.getProperty("/root/level1/level2/level3/prop2"), is(prop2));
        assertThat(session.getItem("/root/level1/level2/level3/prop1"), nullValue());
        assertThat(session.getItem("/root/level1/level2/level3/prop2"), is((Item) prop2));

        assertThat(session.itemExists("/root/level1/level2/level3/prop1"), is(false));
        assertThat(session.itemExists("/root/level1/level2/level3/prop2"), is(true));
    }
}
