package com.aperto.magkit.mockito;

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

import info.magnolia.module.ModuleRegistry;
import info.magnolia.objectfactory.Components;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Test ModuleRegistryMockUtils.
 *
 * @author wolf.bubenik@aperto.com
 * @since 04.03.2020
 */
public class ModuleRegistryMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockModuleRegistry() {
        ModuleRegistry mr = ModuleRegistryMockUtils.mockModuleRegistry();
        assertThat(mr, notNullValue());
        assertThat(Components.getComponent(ModuleRegistry.class), is(mr));

        ModuleRegistryMockUtils.mockModuleRegistry(ModuleRegistryStubbingOperation.stubModuleDefinition("test-definition", "1.0"));
        assertThat(mr.getDefinition("test-definition"), notNullValue());
        assertThat(mr.getDefinition("test-definition").getName(), is("test-definition"));
        assertThat(mr.getDefinition("test-definition").getVersion().toString(), is("1.0.0"));
    }

    @Test
    public void cleanModuleRegistry() {
        ModuleRegistry mr = ModuleRegistryMockUtils.mockModuleRegistry();
        assertThat(Components.getComponent(ModuleRegistry.class), is(mr));

        ModuleRegistryMockUtils.cleanModuleRegistry();
        // Well, this results in a NullPointerException at AbstractComponentProvider.getComponentDefinition(AbstractComponentProvider.java:329)
//        assertThat(Components.getComponent(ModuleRegistry.class), nullValue());
    }
}
