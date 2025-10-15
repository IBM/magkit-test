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

import de.ibmix.magkit.test.ExceptionStubbingOperation;
import org.mockito.stubbing.Answer;

import javax.jcr.Binary;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Factory collection for creating {@link Property}-scoped Mockito stubbing operations.
 * <p>
 * A {@code PropertyStubbingOperation} encapsulates idempotent stubbing logic applied to a given {@link Property} mock via
 * {@link #of(Property)}. Static factory methods cover the most common value types (String, numeric, date, boolean, binary)
 * and convenience cases (node reference, visitor acceptance). Multi-value semantic is supported uniformly: all overloads
 * treat the provided varargs as the exact backing array for {@link Property#getValues()}, while {@link Property#getValue()}
 * and singular getters delegate to the first element (or {@code null} if none present) consistent with JCR expectations.
 * </p>
 * <p><strong>Usage example:</strong>
 * <pre>{@code
 * PropertyStubbingOperation titleStub = PropertyStubbingOperation.stubValues("Hello World");
 * Property title = PropertyMockUtils.mockProperty("title");
 * titleStub.of(title);
 * }</pre>
 * </p>
 * <p><strong>Thread-safety:</strong> Operations are stateless and safe to reuse across tests; only the targeted mock is mutated.</p>
 * <p><strong>Error handling:</strong> Methods assert non-null arguments (property, value arrays) to fail fast in test setups.
 * {@link RepositoryException} is declared for alignment with JCR APIs though not typically thrown by these operations.
 * </p>
 * <p><strong>Extensibility:</strong> For uncommon property types create a custom operation returning an anonymous subclass or wrap
 * {@link ValueMockUtils#mockValue(Object)} generated values.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-05
 * @see ValueMockUtils
 * @see NodeStubbingOperation
 */
public abstract class PropertyStubbingOperation implements ExceptionStubbingOperation<Property, RepositoryException> {

    /**
     * Stub a property with prepared {@link Value} instances (multi or single value).
     * <p>
     * Contract:
     * <ul>
     *   <li>{@link Property#getValue()} returns first element (or {@code null} if array empty).</li>
     *   <li>{@link Property#getValues()} returns the exact provided array (no defensive copy).</li>
     * </ul>
     * Caller ensures the {@link Value} instances match the intended {@link PropertyType} semantics.
     * </p>
     *
     * @param values non-null array of values (may be empty)
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Value... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(property, notNullValue());
                assertThat(values, notNullValue());
                Value v = values.length > 0 ? values[0] : null;
                when(property.getValue()).thenReturn(v);
                when(property.getValues()).thenReturn(values);
            }
        };
    }

    /**
     * Stub a STRING property (single or multi value) using {@link PropertyType#STRING} mapping.
     * Equivalent to {@link #stubValues(int, String...)} with type {@code STRING}.
     *
     * @param values string values (non-null array)
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final String... values) {
        return stubValues(PropertyType.STRING, values);
    }

    /**
     * Stub a typed STRING-based property (e.g. NAME, PATH, URI) by supplying the desired JCR type id and raw string values.
     * Internally each string is converted into a {@link Value} via {@link ValueMockUtils#mockValue(String, int)}.
     *
     * @param type a valid {@link PropertyType} constant representing the JCR type of the supplied values
     * @param values raw lexical string values to wrap
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final int type, final String... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (String value : values) {
                    valueList.add(ValueMockUtils.mockValue(value, type));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a LONG property (multi or single value).
     *
     * @param values long values
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Long... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (Long value : values) {
                    valueList.add(ValueMockUtils.mockValue(value));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a DOUBLE property.
     *
     * @param values double values
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Double... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (Double value : values) {
                    valueList.add(ValueMockUtils.mockValue(value));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a DATE property (Calendar values).
     *
     * @param values calendar values
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Calendar... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (Calendar value : values) {
                    valueList.add(ValueMockUtils.mockValue(value));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a BOOLEAN property.
     *
     * @param values boolean values
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Boolean... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (Boolean value : values) {
                    valueList.add(ValueMockUtils.mockValue(value));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a BINARY property.
     *
     * @param values binary values
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubValues(final Binary... values) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(values, notNullValue());
                List<Value> valueList = new ArrayList<>(values.length);
                for (Binary value : values) {
                    valueList.add(ValueMockUtils.mockValue(value));
                }
                stubValues(valueList.toArray(new Value[0])).of(property);
            }
        };
    }

    /**
     * Stub a property referencing a {@link Node} using either PATH or REFERENCE semantics.
     * <p>Converts the supplied node into the correct lexical representation (path or identifier) and delegates to
     * {@link #stubValues(int, String...)}, additionally stubbing {@link Property#getNode()}.</p>
     *
     * @param value target node (may be {@code null})
     * @param propertyType either {@link PropertyType#PATH} or {@link PropertyType#REFERENCE}
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubNode(final Node value, final int propertyType) {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                assertThat(property, notNullValue());
                when(property.getNode()).thenReturn(value);
                String reference = null;
                if (value != null) {
                    switch (propertyType) {
                        case PropertyType.PATH:
                            reference = value.getPath();
                            break;
                        case PropertyType.REFERENCE:
                            reference = value.getIdentifier();
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported PropertyType for Node references: " + propertyType);

                    }
                }
                stubValues(propertyType, reference).of(property);
            }
        };
    }

    /**
     * Stub {@link Property#accept(javax.jcr.ItemVisitor)} so that the provided visitor immediately visits the property.
     * <p>Implements the visitor pattern contract for tests asserting interaction logic without needing a full repository.</p>
     *
     * @return stubbing operation
     */
    public static PropertyStubbingOperation stubAccept() {
        return new PropertyStubbingOperation() {
            @Override
            public void of(final Property property) throws RepositoryException {
                doAnswer((Answer<Object>) invocation -> {
                    Object[] args = invocation.getArguments();
                    ItemVisitor visitor = (ItemVisitor) args[0];
                    visitor.visit((Property) invocation.getMock());
                    return null;
                }).when(property).accept(any(ItemVisitor.class));
            }
        };
    }
}
