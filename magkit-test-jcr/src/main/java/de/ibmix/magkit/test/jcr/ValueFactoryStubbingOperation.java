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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import java.util.Calendar;

import static org.mockito.Mockito.when;

/**
 * Fluent stubbing operation for enriching a mocked {@link ValueFactory} with deterministic return values.
 * <p>
 * Instances are typically created through the static {@code stubCreateValue(..)} factory methods and supplied to
 * {@link ValueFactoryMockUtils#mockValueFactory(ValueFactoryStubbingOperation...)}. Each operation will configure the
 * passed {@link ValueFactory} mock so that the respective {@code createValue(..)} overload returns a {@link Value}
 * created by the corresponding method in {@link ValueMockUtils}. Additional stubbing (e.g. for repeated calls) can be
 * layered afterwards directly on the {@link ValueFactory} mock if required.
 * <p>
 * Contract:
 * <ul>
 *   <li>{@link #of(ValueFactory)} asserts the provided factory is non-null.</li>
 *   <li>Every factory method captures its argument and ensures future calls with the same primitive/value instance
 *       return the configured {@link Value}.</li>
 *   <li>Returned {@link Value} mocks implement rich conversion semantics as documented in {@link ValueMockUtils}.</li>
 * </ul>
 * Thread safety: Individual operations are immutable and can be reused for multiple factories if desired.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public abstract class ValueFactoryStubbingOperation implements ExceptionStubbingOperation<ValueFactory, RepositoryException> {

    private ValueFactoryStubbingOperation() {
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(String)} to return a mock produced by
     * {@link ValueMockUtils#mockValue(String)} for the supplied {@code value}.
     *
     * @param value source string; may be {@code null} (null-safe mock is still created)
     * @return stubbing operation for use with {@link ValueFactoryMockUtils#mockValueFactory(ValueFactoryStubbingOperation...)}
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final String value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(boolean)} to return a mock value.
     *
     * @param value primitive boolean input
     * @return stubbing operation
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final boolean value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(Calendar)} to return a date mock value.
     *
     * @param value calendar instance (may be {@code null})
     * @return stubbing operation
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final Calendar value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(double)} to return a numeric mock value.
     *
     * @param value double input
     * @return stubbing operation
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final double value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(long)} to return a numeric mock value.
     *
     * @param value long input
     * @return stubbing operation
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final long value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }

    /**
     * Create a stubbing operation configuring {@link ValueFactory#createValue(Binary)} to return a binary mock value.
     *
     * @param value binary instance (may be {@code null})
     * @return stubbing operation
     */
    public static ValueFactoryStubbingOperation stubCreateValue(final Binary value) {
        return new ValueFactoryStubbingOperation() {
            @Override
            public void of(final ValueFactory valueFactory) throws RepositoryException {
                Require.Argument.notNull(valueFactory, "valueFactory must not be null");
                Value valueObj = ValueMockUtils.mockValue(value);
                when(valueFactory.createValue(value)).thenReturn(valueObj);
            }
        };
    }
}
