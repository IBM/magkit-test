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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.Binary;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing PropertyMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-13
 */
public class PropertyMockUtilsTest {

    @Test
    public void testMockPropertyString() throws Exception {
        Property p = PropertyMockUtils.mockProperty("name", "v1", "v2");
        assertNotNull(p);
        assertEquals(PropertyType.STRING, p.getType());
        assertEquals("v1", p.getString());
        assertNotNull(p.getValue());
        assertEquals("v1", p.getValue().getString());
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals("name:v1;v2", p.toString());
    }

    @Test
    public void testMockPropertyBinary() throws Exception {
        Binary bin1 = mock(Binary.class);
        Binary bin2 = mock(Binary.class);
        InputStream bin1Stream = mock(InputStream.class);
        Mockito.when(bin1.getStream()).thenReturn(bin1Stream);
        Property p = PropertyMockUtils.mockProperty("name", bin1, bin2);
        assertNotNull(p);
        assertEquals(PropertyType.BINARY, p.getType());
        assertEquals(bin1, p.getBinary());
        assertEquals(bin1Stream, p.getStream());
        assertNotNull(p.getValue());
        assertEquals(bin1, p.getValue().getBinary());
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals("name:Mock for Binary, hashCode: " + bin1.hashCode() + ";Mock for Binary, hashCode: " + bin2.hashCode(), p.toString());
    }

    @Test
    public void testMockPropertyBoolean() throws Exception {
        Property p = PropertyMockUtils.mockProperty("name", true, false, false);
        assertNotNull(p);
        assertEquals(PropertyType.BOOLEAN, p.getType());
        assertTrue(p.getBoolean());
        assertNotNull(p.getValue());
        assertTrue(p.getValue().getBoolean());
        assertNotNull(p.getValues());
        assertEquals(3, p.getValues().length);
        assertEquals("name:true;false;false", p.toString());
    }

    @Test
    public void testMockPropertyCalendar() throws Exception {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTimeInMillis(0L);
        Calendar cal2 = Calendar.getInstance();
        cal2.add(Calendar.DAY_OF_YEAR, 1);
        Property p = PropertyMockUtils.mockProperty("name", cal1, cal2);
        assertNotNull(p);
        assertEquals(PropertyType.DATE, p.getType());
        assertEquals(cal1, p.getDate());
        assertNotNull(p.getValue());
        assertEquals(cal1, p.getValue().getDate());
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals("name:" + ISO8601.format(cal1) + ";" + ISO8601.format(cal2), p.toString());
    }

    @Test
    public void testMockPropertyDouble() throws Exception {
        Property p = PropertyMockUtils.mockProperty("name", 2D, 3D);
        assertNotNull(p);
        assertEquals(PropertyType.DOUBLE, p.getType());
        assertEquals(2D, p.getDouble(), 0.0);
        assertNotNull(p.getValue());
        assertEquals(2D, p.getValue().getDouble(), 0.0);
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals("name:2.0;3.0", p.toString());
    }

    @Test
    public void testMockPropertyLong() throws Exception {
        Property p = PropertyMockUtils.mockProperty("name", 2L, 3L);
        assertNotNull(p);
        assertEquals(PropertyType.LONG, p.getType());
        assertEquals(2L, p.getLong());
        assertNotNull(p.getValue());
        assertEquals(2L, p.getValue().getLong());
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals("name:2;3", p.toString());
    }

    @Test
    public void testMockPropertyValue() throws Exception {
        Value v1 = ValueMockUtils.mockValue("value1");
        Value v2 = ValueMockUtils.mockValue("value2");
        Property p = PropertyMockUtils.mockProperty("name", v1, v2);
        assertNotNull(p);
        assertEquals(PropertyType.STRING, p.getType());
        assertEquals("value1", p.getString());
        assertEquals(v1, p.getValue());
        assertNotNull(p.getValues());
        assertEquals(2, p.getValues().length);
        assertEquals(v1, p.getValues()[0]);
        assertEquals(v2, p.getValues()[1]);
        assertEquals("name:value1;value2", p.toString());
    }


    @Test
    public void testMockPropertyNode() throws Exception {
        Node value = NodeMockUtils.mockNode("testNode");
        Property p = PropertyMockUtils.mockProperty("name", value, PropertyType.PATH);
        assertNotNull(p);
        assertEquals(PropertyType.PATH, p.getType());
        assertEquals(value, p.getNode());
        assertNotNull(p.getValue());
        assertEquals("/testNode", p.getValue().getString());
        assertNotNull(p.getValues());
        assertEquals(1, p.getValues().length);
        assertEquals("name:/testNode", p.toString());
    }

    @Test
    public void testMockPropertyContent() throws Exception {
        Node content = NodeMockUtils.mockNode("/root/test", stubIdentifier("uuid-1"));
        Property p = PropertyMockUtils.mockProperty("reference", content);
        assertNotNull(p);
        assertEquals(PropertyType.REFERENCE, p.getType());
        assertEquals(content, p.getNode());
        assertEquals("test", p.getNode().getName());
        assertNotNull(p.getValue());
        assertEquals("uuid-1", p.getValue().getString());
        assertNotNull(p.getValues());
        assertEquals(1, p.getValues().length);
        assertEquals("reference:uuid-1", p.toString());
    }

    @Test
    public void testMockProperty() throws Exception {
        Property p = PropertyMockUtils.mockProperty("name");
        assertNotNull(p);
        assertEquals("name", p.getName());
        assertFalse(p.getBoolean());
        assertNull(p.getDate());
        assertNull(p.getString());
        assertNull(p.getBinary());
        assertNull(p.getDecimal());
        assertEquals(0.0, p.getDouble(), 0.0);
        assertEquals(0L, p.getLong());
        assertNotNull(p.getValues());
        assertEquals(0, p.getValues().length);
        assertEquals(PropertyType.UNDEFINED, p.getType());

        ItemVisitor visitor = mock(ItemVisitor.class);
        p.accept(visitor);
        verify(visitor, times(1)).visit(p);
    }

    @Test
    public void testMockPropertyParent() throws Exception {
        Property property = PropertyMockUtils.mockProperty("property", "test");
        Node node = mockNode("node", stubProperty(property));
        assertEquals(node, property.getParent());
    }

    @Test
    public void testMockPropertyDepthAndPath() throws Exception {
        Property property = PropertyMockUtils.mockProperty("property", "test");
        Node parent = NodeMockUtils.mockNode("parent");
        Node node = mockNode("node", stubProperty(property), NodeStubbingOperation.stubParent(parent));

        assertEquals(3, property.getDepth());
        assertEquals("/parent/node/property", property.getPath());

        Session session = SessionMockUtils.mockSession("website");
        assertEquals(node, (Node) session.getItem("/parent/node"));
        assertEquals(property, (Property) session.getItem("/parent/node/property"));
    }

    @Test
    public void testGetProperty() throws Exception {
        Session session = SessionMockUtils.mockSession("website");
        Node node1 = NodeMockUtils.mockNode("root/level1/level2/level3/level4");
        Property prop1 = PropertyMockUtils.mockProperty("prop1", "12345");
        Property prop2 = PropertyMockUtils.mockProperty("prop2", "67890");

        stubProperty(prop1).of(node1);
        stubProperty(prop2).of(node1);

        assertEquals("/root/level1/level2/level3/level4/prop1", prop1.getPath());

        assertEquals(prop1, session.getProperty("/root/level1/level2/level3/level4/prop1"));
        assertEquals(prop2, session.getProperty("/root/level1/level2/level3/level4/prop2"));

        Node node2 = session.getNode("/root/level1/level2");

        assertEquals(prop1, node2.getProperty("level3/level4/prop1"));
        assertEquals(prop2, node2.getProperty("level3/level4/prop2"));
    }

    @Test
    public void removeTest() throws RepositoryException {
        Node node3 = NodeMockUtils.mockNode("root/level1/level2/level3");
        Property prop1 = PropertyMockUtils.mockProperty("prop1", "12345");
        Property prop2 = PropertyMockUtils.mockProperty("prop2", "67890");

        stubProperty(prop1).of(node3);
        stubProperty(prop2).of(node3);

        assertEquals(prop1, node3.getProperty("prop1"));
        assertEquals(prop2, node3.getProperty("prop2"));
        assertTrue(node3.hasProperty("prop1"));
        assertTrue(node3.hasProperty("prop2"));
        assertEquals(3L, node3.getProperties().getSize());

        Session session = node3.getSession();
        assertEquals(prop1, session.getProperty("/root/level1/level2/level3/prop1"));
        assertEquals(prop2, session.getProperty("/root/level1/level2/level3/prop2"));
        assertEquals(prop1, (Property) session.getItem("/root/level1/level2/level3/prop1"));
        assertEquals(prop2, (Property) session.getItem("/root/level1/level2/level3/prop2"));

        assertTrue(session.itemExists("/root/level1/level2/level3/prop1"));
        assertTrue(session.itemExists("/root/level1/level2/level3/prop2"));

        // now remove one property:
        prop1.remove();

        assertNull(node3.getProperty("prop1"));
        assertEquals(prop2, node3.getProperty("prop2"));
        assertFalse(node3.hasProperty("prop1"));
        assertTrue(node3.hasProperty("prop2"));
        assertEquals(2L, node3.getProperties().getSize());

        assertNull(session.getProperty("/root/level1/level2/level3/prop1"));
        assertEquals(prop2, session.getProperty("/root/level1/level2/level3/prop2"));
        assertNull(session.getItem("/root/level1/level2/level3/prop1"));
        assertEquals(prop2, session.getItem("/root/level1/level2/level3/prop2"));

        assertFalse(session.itemExists("/root/level1/level2/level3/prop1"));
        assertTrue(session.itemExists("/root/level1/level2/level3/prop2"));
    }
}
