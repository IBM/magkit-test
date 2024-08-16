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

import org.mockito.Answers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.stream.Collectors;

import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubNode;
import static de.ibmix.magkit.test.jcr.PropertyStubbingOperation.stubValues;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Utility for mocking javax.jcr.Property.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-02
 */
public final class PropertyMockUtils {
    private PropertyMockUtils() {
    }

    public static Property mockProperty(final String name, final String... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Binary... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Boolean... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Calendar... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Double... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Long... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Value... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    public static Property mockProperty(final String name, final Node propertyValues) throws RepositoryException {
        return mockProperty(name, propertyValues, PropertyType.REFERENCE);
    }

    public static Property mockProperty(final String name, final Node propertyValue, int propertyType) throws RepositoryException {
        Property property = mockProperty(name);
        stubNode(propertyValue, propertyType).of(property);
        return property;
    }

    public static Property mockProperty(final String name) throws RepositoryException {
        assertThat(name, notNullValue());
        TestProperty property = Mockito.mock(TestProperty.class);
        when(property.getName()).thenReturn(name);
        when(property.getString()).thenAnswer(STRING_ANSWER);
        when(property.getBinary()).thenAnswer(BINARY_ANSWER);
        when(property.getStream()).thenAnswer(STREAM_ANSWER);
        when(property.getBoolean()).thenAnswer(BOOLEAN_ANSWER);
        when(property.getDate()).thenAnswer(CALENDAR_ANSWER);
        when(property.getDouble()).thenAnswer(DOUBLE_ANSWER);
        when(property.getLong()).thenAnswer(LONG_ANSWER);
        when(property.getType()).thenAnswer(TYPE_ANSWER);
        when(property.getPath()).then(NodeMockUtils.PATH_ANSWER);
        when(property.getDepth()).then(NodeMockUtils.DEPTH_ANSWER);
        when(property.getSession()).then(NodeMockUtils.SESSION_ANSWER);
        when(property.getValues()).thenReturn(new Value[0]);
        when(property.isMultiple()).then(IS_MULTIPLE_ANSWER);
        doAnswer(Answers.CALLS_REAL_METHODS).when(property).remove();
        doAnswer(TO_STRING_ANSWER).when(property).toString();
        PropertyStubbingOperation.stubAccept().of(property);
        return property;
    }

    public static final Answer<String> STRING_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        // TODO: return default ?
        return v != null ? v.getString() : null;
    };
    public static final Answer<Binary> BINARY_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        return v != null ? v.getBinary() : null;
    };
    public static final Answer<InputStream> STREAM_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        return v != null && v.getBinary() != null ? v.getBinary().getStream() : null;
    };
    public static final Answer<Boolean> BOOLEAN_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        // TODO: return default ?
        return v != null ? v.getBoolean() : null;
    };
    public static final Answer<Calendar> CALENDAR_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        return v != null ? v.getDate() : null;
    };
    public static final Answer<Double> DOUBLE_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        // TODO: return default ?
        return v != null ? v.getDouble() : null;
    };
    public static final Answer<Long> LONG_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        // TODO: return default ?
        return v != null ? v.getLong() : null;
    };
    public static final Answer<Integer> TYPE_ANSWER = invocation -> {
        Value v = ((Property) invocation.getMock()).getValue();
        return v != null ? v.getType() : PropertyType.UNDEFINED;
    };

    public static final Answer<Boolean> IS_MULTIPLE_ANSWER = invocation -> {
        Value[] v = ((Property) invocation.getMock()).getValues();
        return v != null && v.length > 1;
    };
    public static final Answer<String> TO_STRING_ANSWER = invocation -> {
        Property property = (Property) invocation.getMock();
        return property != null ? property.getName() + ':' + (property.isMultiple() ? Arrays.stream(property.getValues()).map(Value::toString).collect(Collectors.joining(";")) : property.getString()) : "NULL";
    };

    /**
     * Extended Interface to simplify mocking.
     */
    abstract static class TestProperty implements Property {

        @Override
        public void remove() throws RepositoryException {
            NodeMockUtils.TestNode parent = (NodeMockUtils.TestNode) getParent();
            Collection<Property> siblings = parent.getPropertyCollection();
            siblings.remove(this);
            SessionStubbingOperation.stubRemoveItem(this).of(getSession());
        }
    }
}
