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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubAccept;
import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing PropertyStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-13
 */
public class PropertyStubbingOperationTest {

    private Property _property;

    @BeforeEach
    public void setUp() throws Exception {
        _property = PropertyMockUtils.mockProperty("test");
    }

    @Test
    public void testStubValues() throws Exception {
        stubValues(new Value[0]).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(0, _property.getValues().length);
        assertNull(_property.getValue());
        assertFalse(_property.isMultiple());

        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        stubValues(v1, v2).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertEquals(v1, _property.getValue());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesString() throws Exception {
        stubValues("v1", "v2").of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals("v1", _property.getValue().getString());
        assertEquals(PropertyType.STRING, _property.getType());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesLong() throws Exception {
        stubValues(1L, 2L).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals(1L, _property.getValue().getLong());
        assertEquals(PropertyType.LONG, _property.getType());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesDouble() throws Exception {
        stubValues(1D, 2D).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals(1D, _property.getValue().getDouble());
        assertEquals(PropertyType.DOUBLE, _property.getType());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesCalendar() throws Exception {
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.DAY_OF_YEAR, 1);
        stubValues(date1, date2).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals(date1, _property.getValue().getDate());
        assertEquals(PropertyType.DATE, _property.getType());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesBoolean() throws Exception {
        stubValues(true, true, false).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(3, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertTrue(_property.getValue().getBoolean());
        assertEquals(PropertyType.BOOLEAN, _property.getType());
        assertTrue(_property.isMultiple());
    }

    @Test
    public void testStubValuesBinary() throws Exception {
        Binary bin1 = mock(Binary.class);
        Binary bin2 = mock(Binary.class);
        stubValues(bin1, bin2).of(_property);
        assertNotNull(_property.getValues());
        assertEquals(2, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals(bin1, _property.getValue().getBinary());
        assertEquals(PropertyType.BINARY, _property.getType());
    }

    @Test
    public void testStubNode() throws Exception {
        Node node = mockNode("test", stubIdentifier("uuid"));
        stubNode(node, PropertyType.PATH).of(_property);

        assertEquals(node, _property.getNode());
        assertEquals(1, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals("/test", _property.getValue().getString());
        assertEquals(PropertyType.PATH, _property.getType());

        node = mockNode("test2", stubIdentifier("uuid2"));
        stubNode(node, PropertyType.REFERENCE).of(_property);

        assertEquals(node, _property.getNode());
        assertEquals(1, _property.getValues().length);
        assertNotNull(_property.getValue());
        assertEquals("uuid2", _property.getValue().getString());
        assertEquals(PropertyType.REFERENCE, _property.getType());
    }

    @Test
    public void testStubNodeForException() throws Exception {
        Node node = mockNode("test", stubIdentifier("uuid"));
        assertThrows(IllegalArgumentException.class, () -> stubNode(node, PropertyType.UNDEFINED).of(_property));
    }

    @Test
    public void testStubAccept() throws Exception {
        ItemVisitor visitor = mock(ItemVisitor.class);
        verify(visitor, times(0)).visit(_property);
        stubAccept().of(_property);
        _property.accept(visitor);
        verify(visitor, times(1)).visit(_property);
    }
}
