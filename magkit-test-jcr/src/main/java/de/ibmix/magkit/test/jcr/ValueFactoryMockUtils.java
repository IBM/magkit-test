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
 * #L% */

import org.mockito.stubbing.Answer;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.ValueMockUtils.mockValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Utility for creating Mockito based {@link ValueFactory} test doubles with realistic {@link Value} creation behaviour.
 * <p>
 * The produced factory delegates every {@code createValue(..)} call to the corresponding factory method in
 * {@link ValueMockUtils} so that returned {@link Value} instances mirror JCR conversion rules (numeric/date parsing,
 * boolean handling, binary stream access, etc.). This eliminates repetitive boilerplate in repository centric unit tests.
 * <p>
 * Customisation: Additional behaviour can be layered by supplying one or more {@link ValueFactoryStubbingOperation}
 * instances via the varargs parameter in {@link #mockValueFactory(ValueFactoryStubbingOperation...)}.
 * Each supplied operation is executed in order and may override previously defined stubbings (last one wins).
 * <p>
 * Thread safety: Returned mocks are fully stubbed and stateless after creation and can be safely reused in parallel test execution.
 * <p>
 * Example usage (conceptual):
 * <pre>
 * ValueFactory vf = ValueFactoryMockUtils.mockValueFactory(
 *     ValueFactoryStubbingOperation.stubCreateValue("fixed")
 * );
 * Value v = vf.createValue("fixed"); // returns mock with numeric/date conversions if applicable
 * </pre>
 * Note: All methods declare {@link RepositoryException} for API symmetry; current implementation never throws it.
 */
public final class ValueFactoryMockUtils {

    /**
     * Create a {@link ValueFactory} mock where each {@code createValue(..)} overload returns a {@link Value}
     * produced by the corresponding {@link ValueMockUtils#mockValue(String)} (or related overload) factory. Optional
     * {@link ValueFactoryStubbingOperation}s can further adapt or override behaviour (e.g. alternate return values,
     * exception simulation).
     * <p>
     * Behaviour per overload:
     * <ul>
     *   <li>{@code createValue(String)} -> {@link ValueMockUtils#mockValue(String)}</li>
     *   <li>{@code createValue(boolean)} -> {@link ValueMockUtils#mockValue(boolean)}</li>
     *   <li>{@code createValue(double)} -> {@link ValueMockUtils#mockValue(double)}</li>
     *   <li>{@code createValue(long)} -> {@link ValueMockUtils#mockValue(long)}</li>
     *   <li>{@code createValue(Calendar)} -> {@link ValueMockUtils#mockValue(Calendar)}</li>
     *   <li>{@code createValue(Binary)} -> {@link ValueMockUtils#mockValue(Binary)}</li>
     *   <li>{@code createValue(Node)} -> {@link ValueMockUtils#mockValue(Node)}</li>
     * </ul>
     * Each supplied stubbing operation receives the freshly created mock allowing additional when/thenReturn clauses.
     *
     * @param stubbings optional ordered customisation operations; may be empty
     * @return configured {@link ValueFactory} Mockito mock (never {@code null})
     * @throws RepositoryException declared for symmetry; not thrown
     */
    public static ValueFactory mockValueFactory(ValueFactoryStubbingOperation... stubbings) throws RepositoryException {
        ValueFactory result = mock(ValueFactory.class);
        doAnswer(MOCK_STRING_VALUE).when(result).createValue(anyString());
        doAnswer(MOCK_BOOLEAN_VALUE).when(result).createValue(anyBoolean());
        doAnswer(MOCK_DOUBLE_VALUE).when(result).createValue(anyDouble());
        doAnswer(MOCK_LONG_VALUE).when(result).createValue(anyLong());
        doAnswer(MOCK_CALENDAR_VALUE).when(result).createValue(any(Calendar.class));
        doAnswer(MOCK_BINARY_VALUE).when(result).createValue(any(Binary.class));
        doAnswer(MOCK_NODE_VALUE).when(result).createValue(any(Node.class));
        for (ValueFactoryStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /** Answer returning a {@link Value} for String inputs using {@link ValueMockUtils#mockValue(String)}. */
    private static final Answer<Value> MOCK_STRING_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, String.class));
    /** Answer returning a {@link Value} for boolean inputs using {@link ValueMockUtils#mockValue(boolean)}. */
    private static final Answer<Value> MOCK_BOOLEAN_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Boolean.class));
    /** Answer returning a {@link Value} for double inputs using {@link ValueMockUtils#mockValue(double)}. */
    private static final Answer<Value> MOCK_DOUBLE_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Double.class));
    /** Answer returning a {@link Value} for long inputs using {@link ValueMockUtils#mockValue(long)}. */
    private static final Answer<Value> MOCK_LONG_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Long.class));
    /** Answer returning a {@link Value} for calendar inputs using {@link ValueMockUtils#mockValue(Calendar)}. */
    private static final Answer<Value> MOCK_CALENDAR_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Calendar.class));
    /** Answer returning a {@link Value} for binary inputs using {@link ValueMockUtils#mockValue(Binary)}. */
    private static final Answer<Value> MOCK_BINARY_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Binary.class));
    /** Answer returning a {@link Value} for node inputs using {@link ValueMockUtils#mockValue(Node)}. */
    private static final Answer<Value> MOCK_NODE_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Node.class));

    private ValueFactoryMockUtils() {
    }
}
