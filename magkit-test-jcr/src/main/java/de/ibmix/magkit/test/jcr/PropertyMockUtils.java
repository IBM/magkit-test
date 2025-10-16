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

import de.ibmix.magkit.assertations.Require;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Utility for creating Mockito based {@link Property} test doubles with rich, type-aware default answers.
 * <p>
 * The static {@code mockProperty(...)} factory methods in this class allow you to quickly create a JCR
 * {@link Property} mock that behaves consistently with typical repository semantics:
 * </p>
 * <ul>
 *     <li>If you pass more than one value the property is treated as multi-valued ( {@link Property#isMultiple()} returns {@code true} ).</li>
 *     <li>All canonical accessor methods ({@code getString()}, {@code getLong()}, {@code getDate()}, ... ) delegate to
 *     the currently configured {@link Value} instance(s) so that conversions you stub on {@link Value} are respected.</li>
 *     <li>{@code getType()} dynamically reflects the underlying (first) {@link Value#getType()} and falls back to {@link PropertyType#UNDEFINED}.</li>
 *     <li>{@code toString()} yields a concise debug representation: {@code <name>:<value>} (single) or joined with ';' (multi-valued).</li>
 *     <li>{@code remove()} modifies the parent mock node's internal property collection (see {@link NodeMockUtils}).</li>
 * </ul>
 * <p>
 * Overloads let you provide initial values of different Java types (String, Boolean, Long, etc.), pre-built {@link Value}s,
 * {@link Binary} content, or node references. The dedicated node overload allows specifying the JCR property type
 * (e.g. {@link PropertyType#REFERENCE} or {@link PropertyType#WEAKREFERENCE}).
 * </p>
 * <p>
 * Typical usage:
 * </p>
 * <pre>{@code
 * Property title = PropertyMockUtils.mockProperty("title", "Hello World");
 * assertEquals("Hello World", title.getString());
 *
 * Property flags = PropertyMockUtils.mockProperty("flags", "a", "b", "c");
 * assertTrue(flags.isMultiple());
 *
 * Property ref = PropertyMockUtils.mockProperty("myRef", someNode); // defaults to REFERENCE
 * }
 * </pre>
 * <p>
 * For advanced manipulation you can further stub additional interactions on the returned mock using the standard Mockito API.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-02
 */
public final class PropertyMockUtils {
    private PropertyMockUtils() {
    }

    /**
     * Create a {@link Property} mock with one or more {@link String} values.
     * <p>If multiple values are supplied the property is marked multi-valued.</p>
     *
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more string values to be wrapped as {@link Value}s
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final String... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more {@link Binary} values.
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more binary values
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Binary... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more {@link Boolean} values.
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more boolean values
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Boolean... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more {@link Calendar} date values.
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more calendar values
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Calendar... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more {@link Double} values.
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more double values
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Double... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more {@link Long} values.
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more long values
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Long... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a {@link Property} mock with one or more pre-built {@link Value} instances.
     * <p>This is the most flexible overload when you want to control value conversion behavior explicitly.</p>
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues zero or more {@link Value} objects
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Value... propertyValues) throws RepositoryException {
        Property property = mockProperty(name);
        stubValues(propertyValues).of(property);
        return property;
    }

    /**
     * Create a single-valued node reference {@link Property} (defaults to {@link PropertyType#REFERENCE}).
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValues referenced node
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Node propertyValues) throws RepositoryException {
        return mockProperty(name, propertyValues, PropertyType.REFERENCE);
    }

    /**
     * Create a single-valued node reference {@link Property} with an explicit JCR property type.
     * <p>Use this overload for WEAKREFERENCE or similar reference types.</p>
     * @param name the JCR property name, must not be {@code null}
     * @param propertyValue referenced node
     * @param propertyType the JCR property type (e.g. {@link PropertyType#REFERENCE}, {@link PropertyType#WEAKREFERENCE})
     * @return configured {@link Property} mock
     * @throws RepositoryException if value stubbing fails
     */
    public static Property mockProperty(final String name, final Node propertyValue, int propertyType) throws RepositoryException {
        Property property = mockProperty(name);
        stubNode(propertyValue, propertyType).of(property);
        return property;
    }

    /**
     * Create a bare {@link Property} mock without initial values.
     * <p>The returned mock has dynamic answers for the standard accessor methods that delegate to the current
     * (first) {@link Value} when set later via the provided stubbing operations.</p>
     * <p>Initially the property is single-valued and returns an empty {@code Value[]} for {@link Property#getValues()}.</p>
     *
     * @param name the JCR property name, must not be {@code null}
     * @return base {@link Property} mock ready for further stubbing
     * @throws RepositoryException if internal stubbing fails
     */
    public static Property mockProperty(final String name) throws RepositoryException {
        Require.Argument.notNull(name, "property name must not be null");
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
