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

import org.junit.Before;
import org.junit.Test;

import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.io.InputStream;
import java.util.Calendar;

import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.mockito.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.mockito.jcr.PropertyMockUtils.mockProperty;
import static de.ibmix.magkit.mockito.jcr.PropertyStubbingOperation.stubAccept;
import static de.ibmix.magkit.mockito.jcr.PropertyStubbingOperation.stubNode;
import static de.ibmix.magkit.mockito.jcr.PropertyStubbingOperation.stubValues;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing PropertyStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 13.11.12
 */
public class PropertyStubbingOperationTest {

    private Property _property;

    @Before
    public void setUp() throws Exception {
        _property = mockProperty("test");
    }

    @Test
    public void testStubValues() throws Exception {
        stubValues(new Value[0]).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(0));
        assertThat(_property.getValue(), nullValue());
        assertThat(_property.isMultiple(), is(false));

        Value v1 = mock(Value.class);
        Value v2 = mock(Value.class);
        stubValues(v1, v2).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), is(v1));
        assertThat(_property.isMultiple(), is(true));
    }

    @Test
    public void testStubValuesString() throws Exception {
        stubValues("v1", "v2").of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getString(), is("v1"));
        assertThat(_property.getType(), is(PropertyType.STRING));
        assertThat(_property.isMultiple(), is(true));
    }

//    @Test
//    public void testStubUriValues() throws Exception {
//        stubUriValues("uri1", "uri2").of(_property);
//        assertThat(_property.getValues(), notNullValue());
//        assertThat(_property.getValues().length, is(2));
//        assertThat(_property.getValue(), notNullValue());
//        assertThat(_property.getValue().getString(), is("uri1"));
//        assertThat(_property.getType(), is(PropertyType.URI));
//    }

    @Test
    public void testStubValuesLong() throws Exception {
        stubValues(1L, 2L).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getLong(), is(1L));
        assertThat(_property.getType(), is(PropertyType.LONG));
        assertThat(_property.isMultiple(), is(true));
    }

    @Test
    public void testStubValuesDouble() throws Exception {
        stubValues(1D, 2D).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getDouble(), is(1D));
        assertThat(_property.getType(), is(PropertyType.DOUBLE));
        assertThat(_property.isMultiple(), is(true));
    }

    @Test
    public void testStubValuesCalendar() throws Exception {
        Calendar date1 = Calendar.getInstance();
        Calendar date2 = Calendar.getInstance();
        date2.add(Calendar.DAY_OF_YEAR, 1);
        stubValues(date1, date2).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getDate(), is(date1));
        assertThat(_property.getType(), is(PropertyType.DATE));
        assertThat(_property.isMultiple(), is(true));
    }

    @Test
    public void testStubValuesBoolean() throws Exception {
        stubValues(true, true, false).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(3));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getBoolean(), is(true));
        assertThat(_property.getType(), is(PropertyType.BOOLEAN));
        assertThat(_property.isMultiple(), is(true));
    }

//    @Test
//    public void testStubValuesBinary() throws Exception {
//        Binary bin1 = mock(Binary.class);
//        Binary bin2 = mock(Binary.class);
//        stubValues(bin1, bin2).of(_property);
//        assertThat(_property.getValues(), notNullValue());
//        assertThat(_property.getValues().length, is(2));
//        assertThat(_property.getValue(), notNullValue());
//        assertThat(_property.getValue().getBinary(), is(bin1));
//        assertThat(_property.getType(), is(PropertyType.BINARY));
//    }

    @Test
    public void testStubValuesInputStream() throws Exception {
        InputStream bin1 = mock(InputStream.class);
        InputStream bin2 = mock(InputStream.class);
        stubValues(bin1, bin2).of(_property);
        assertThat(_property.getValues(), notNullValue());
        assertThat(_property.getValues().length, is(2));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getStream(), is(bin1));
//        assertThat(_property.getValue().getBinary(), notNullValue());
//        assertThat(_property.getValue().getBinary().getStream(), is(bin1));
        assertThat(_property.getType(), is(PropertyType.BINARY));
        assertThat(_property.isMultiple(), is(true));
    }

    @Test
    public void testStubNode() throws Exception {
        Node node = mockNode("test", stubIdentifier("uuid"));
        stubNode(node, PropertyType.PATH).of(_property);

        assertThat(_property.getNode(), is(node));
        assertThat(_property.getValues().length, is(1));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getString(), is("/test"));
        assertThat(_property.getType(), is(PropertyType.PATH));

        node = mockNode("test2", stubIdentifier("uuid2"));
        stubNode(node, PropertyType.REFERENCE).of(_property);

        assertThat(_property.getNode(), is(node));
        assertThat(_property.getValues().length, is(1));
        assertThat(_property.getValue(), notNullValue());
        assertThat(_property.getValue().getString(), is("uuid2"));
        assertThat(_property.getType(), is(PropertyType.REFERENCE));

//        node = mockNode("test3", stubIdentifier("uuid3"));
//        stubNode(node, PropertyType.WEAKREFERENCE).of(_property);
//
//        assertThat(_property.getNode(), is(node));
//        assertThat(_property.getValues().length, is(1));
//        assertThat(_property.getValue(), notNullValue());
//        assertThat(_property.getValue().getString(), is("uuid3"));
//        assertThat(_property.getType(), is(PropertyType.WEAKREFERENCE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStubNodeForException() throws Exception {
        Node node = mockNode("test", stubIdentifier("uuid"));
        stubNode(node, PropertyType.UNDEFINED).of(_property);
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
