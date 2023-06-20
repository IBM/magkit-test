package de.ibmix.magkit.mockito;

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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Comparator;

import static de.ibmix.magkit.mockito.ComponentsMockUtils.clearComponentProvider;
import static de.ibmix.magkit.mockito.ComponentsMockUtils.getComponentProvider;
import static de.ibmix.magkit.mockito.ComponentsMockUtils.getComponentSingleton;
import static de.ibmix.magkit.mockito.ComponentsMockUtils.mockComponentFactory;
import static de.ibmix.magkit.mockito.ComponentsMockUtils.mockComponentInstance;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing ComponentsMockUtils.
 *
 * @author wolf.bubenik
 * @since 07.06.12
 */
public class ComponentsMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        clearComponentProvider();
    }

    @Test
    public void testGetComponentProvider() throws Exception {
        assertThat(getComponentProvider(), notNullValue());
    }

    @Test
    public void testMockComponentInstance() throws Exception {
        // test for concrete class
        Object instance = mockComponentInstance(Object.class);
        assertThat(instance, notNullValue());
        assertThat(instance.getClass().getName().contains("EnhancerByMockito"), is(true));
        Object instance2 = mockComponentInstance(Object.class);
        assertThat(instance.equals(instance2), is(true));

        // test for interface
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        CharSequence interfaceMock = mockComponentInstance(CharSequence.class);
        assertThat(interfaceMock, notNullValue());
        assertThat(interfaceMock.getClass().getName().contains("EnhancerByMockito"), is(true));
        CharSequence interfaceMock2 = mockComponentInstance(CharSequence.class);
        assertThat(interfaceMock.equals(interfaceMock2), is(true));
    }

    @Test
    public void testMockComponentFactory() throws Exception {
        CharSequence instance = Mockito.mock(CharSequence.class);
        mockComponentFactory(CharSequence.class, instance);
        assertThat(getComponentSingleton(CharSequence.class), is(instance));
        assertThat(mockComponentInstance(CharSequence.class), is(instance));
    }

    @Test
    public void testClearComponentProvider() throws Exception {
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        assertThat(getComponentSingleton(Comparator.class), nullValue());

        CharSequence instance1 = mockComponentInstance(CharSequence.class);
        Comparator instance2 = mockComponentInstance(Comparator.class);
        assertThat(getComponentSingleton(CharSequence.class), is(instance1));
        assertThat(getComponentSingleton(Comparator.class), is(instance2));

        clearComponentProvider();
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        assertThat(getComponentSingleton(Comparator.class), nullValue());
    }

    @Test
    public void testClearComponentProviderForClass() throws Exception {
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        assertThat(getComponentSingleton(Comparator.class), nullValue());

        CharSequence instance1 = mockComponentInstance(CharSequence.class);
        Comparator instance2 = mockComponentInstance(Comparator.class);
        assertThat(getComponentSingleton(CharSequence.class), is(instance1));
        assertThat(getComponentSingleton(Comparator.class), is(instance2));

        clearComponentProvider(CharSequence.class);
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        assertThat(getComponentSingleton(Comparator.class), is(instance2));

        clearComponentProvider(Comparator.class);
        assertThat(getComponentSingleton(CharSequence.class), nullValue());
        assertThat(getComponentSingleton(Comparator.class), nullValue());
    }
}
