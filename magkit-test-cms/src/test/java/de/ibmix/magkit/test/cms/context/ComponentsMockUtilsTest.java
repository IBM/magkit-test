package de.ibmix.magkit.test.cms.context;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Comparator;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.clearComponentProvider;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.getComponentProvider;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.getComponentSingleton;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentFactory;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.internal.util.MockUtil.isMock;

/**
 * Testing ComponentsMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-06-07
 */
public class ComponentsMockUtilsTest {

    @BeforeEach
    public void setUp() {
        clearComponentProvider();
    }

    @Test
    public void testGetComponentProvider() {
        assertNotNull(getComponentProvider());
    }

    @Test
    public void testMockComponentInstance() {
        Object instance = mockComponentInstance(Object.class);
        assertNotNull(instance);
        assertTrue(isMock(instance));
        Object instance2 = mockComponentInstance(Object.class);
        assertTrue(instance.equals(instance2));
        assertNull(getComponentSingleton(CharSequence.class));
        CharSequence interfaceMock = mockComponentInstance(CharSequence.class);
        assertNotNull(interfaceMock);
        assertTrue(isMock(interfaceMock));
        CharSequence interfaceMock2 = mockComponentInstance(CharSequence.class);
        assertTrue(interfaceMock.equals(interfaceMock2));
    }

    @Test
    public void testMockComponentFactory() {
        CharSequence instance = Mockito.mock(CharSequence.class);
        mockComponentFactory(CharSequence.class, instance);
        assertEquals(instance, getComponentSingleton(CharSequence.class));
        assertEquals(instance, mockComponentInstance(CharSequence.class));
    }

    @Test
    public void testClearComponentProvider() {
        assertNull(getComponentSingleton(CharSequence.class));
        assertNull(getComponentSingleton(Comparator.class));
        CharSequence instance1 = mockComponentInstance(CharSequence.class);
        Comparator<?> instance2 = mockComponentInstance(Comparator.class);
        assertEquals(instance1, getComponentSingleton(CharSequence.class));
        assertEquals(instance2, getComponentSingleton(Comparator.class));
        clearComponentProvider();
        assertNull(getComponentSingleton(CharSequence.class));
        assertNull(getComponentSingleton(Comparator.class));
    }

    @Test
    public void testClearComponentProviderForClass() {
        assertNull(getComponentSingleton(CharSequence.class));
        assertNull(getComponentSingleton(Comparator.class));
        CharSequence instance1 = mockComponentInstance(CharSequence.class);
        Comparator<?> instance2 = mockComponentInstance(Comparator.class);
        assertEquals(instance1, getComponentSingleton(CharSequence.class));
        assertEquals(instance2, getComponentSingleton(Comparator.class));
        clearComponentProvider(CharSequence.class);
        assertNull(getComponentSingleton(CharSequence.class));
        assertEquals(instance2, getComponentSingleton(Comparator.class));
        clearComponentProvider(Comparator.class);
        assertNull(getComponentSingleton(CharSequence.class));
        assertNull(getComponentSingleton(Comparator.class));
    }
}
