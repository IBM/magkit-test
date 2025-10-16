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

import org.junit.jupiter.api.Test;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;

import java.util.Calendar;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing ValueFactoryMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class ValueFactoryMockUtilsTest {

    /**
     * Test of mockValueFactory method, of class ValueFactoryMockUtils.
     */
    @Test
    public void testMockValueFactory() throws RepositoryException {
        ValueFactoryStubbingOperation op1 = mock(ValueFactoryStubbingOperation.class);
        ValueFactoryStubbingOperation op2 = mock(ValueFactoryStubbingOperation.class);

        ValueFactory factory = ValueFactoryMockUtils.mockValueFactory(op1, op2);
        assertNotNull(factory);
        verify(op1, times(1)).of(factory);
        verify(op2, times(1)).of(factory);

        assertEquals("test", factory.createValue("test").getString());
        assertEquals(2.3D, factory.createValue(2.3D).getDouble(), 0.0);
        assertEquals(123, factory.createValue(123L).getLong());
        assertTrue(factory.createValue(true).getBoolean());
        Calendar now = Calendar.getInstance();
        assertEquals(now, factory.createValue(now).getDate());
        Binary binary = mock(Binary.class);
        assertEquals(binary, factory.createValue(binary).getBinary());
        Node node = mockNode("test");
        assertEquals(node.getIdentifier(), factory.createValue(node).getString());
    }
}
