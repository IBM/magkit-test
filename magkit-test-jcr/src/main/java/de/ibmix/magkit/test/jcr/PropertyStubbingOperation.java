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
 * Utility class for stubbing mocks of javax.jcr.Property.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-11-05
 */
public abstract class PropertyStubbingOperation implements ExceptionStubbingOperation<Property, RepositoryException> {

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

    public static PropertyStubbingOperation stubValues(final String... values) {
        return stubValues(PropertyType.STRING, values);
    }

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
