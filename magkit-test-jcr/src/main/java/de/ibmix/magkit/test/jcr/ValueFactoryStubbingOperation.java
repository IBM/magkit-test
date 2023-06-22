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

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.Calendar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.ValueFactory.
 *
 * @author wolf.bubenik
 * @since 03.08.12
 */
public abstract class ValueFactoryStubbingOperation implements ExceptionStubbingOperation<ValueFactory, RepositoryException> {

    private ValueFactoryStubbingOperation() {
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final String value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final boolean value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final Calendar value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final double value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final long value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    public static ValueFactoryStubbingOperation stubCreateValue(final Binary value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                assertThat(valueFactory, notNullValue());
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }
}
