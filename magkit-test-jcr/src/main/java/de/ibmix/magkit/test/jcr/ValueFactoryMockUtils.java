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
 * Util class for mocking javax.jcr.ValueFactory.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public final class ValueFactoryMockUtils {

    private static final Answer<Value> MOCK_STRING_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, String.class));
    private static final Answer<Value> MOCK_BOOLEAN_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Boolean.class));
    private static final Answer<Value> MOCK_DOUBLE_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Double.class));
    private static final Answer<Value> MOCK_LONG_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Long.class));
    private static final Answer<Value> MOCK_CALENDAR_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Calendar.class));
    private static final Answer<Value> MOCK_BINARY_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Binary.class));
    private static final Answer<Value> MOCK_NODE_VALUE = invocationOnMock -> mockValue(invocationOnMock.getArgument(0, Node.class));

    private ValueFactoryMockUtils() {
    }

    /**
     * Create a ValueFactory mock with Answers for the create methods tha return a Value mock for the given value object.
     *
     * @param stubbings  the ValueFactoryStubbingOperation to specify the behaviour of the mock
     * @return  a new mock instance of ValueFactory
     * @throws RepositoryException never
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
}
