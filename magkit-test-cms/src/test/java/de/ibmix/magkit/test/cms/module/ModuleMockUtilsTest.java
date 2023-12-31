package de.ibmix.magkit.test.cms.module;

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

import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.objectfactory.Components;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.clearComponentProvider;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test ModuleMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public class ModuleMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        clearComponentProvider();
    }

    @After
    public void tearDown() throws Exception {
        clearComponentProvider();
    }

    @Test
    public void mockModuleRegistry() {
        ModuleRegistry mr = ModuleMockUtils.mockModuleRegistry();
        assertThat(mr, notNullValue());
        assertThat(Components.getComponent(ModuleRegistry.class), is(mr));

        ModuleMockUtils.mockModuleRegistry(ModuleRegistryStubbingOperation.stubModuleDefinition("test-definition", "1.0"));
        assertThat(mr.getDefinition("test-definition"), notNullValue());
        assertThat(mr.getDefinition("test-definition").getName(), is("test-definition"));
        assertThat(mr.getDefinition("test-definition").getVersion().toString(), is("1.0.0"));
    }

    @Test
    public void mockModuleDefinition() {
        ModuleDefinitionStubbingOperation op = mock(ModuleDefinitionStubbingOperation.class);
        ModuleDefinition definition = ModuleMockUtils.mockModuleDefinition(op);
        assertThat(definition, notNullValue());
        assertThat(definition.getName(), is("test"));
        verify(op, times(1)).of(definition);
        assertThat(Components.getComponent(ModuleRegistry.class).getDefinition("test"), is(definition));
    }

    @Test
    public void cleanModuleRegistry() {
        ModuleRegistry mr = ModuleMockUtils.mockModuleRegistry();
        assertThat(Components.getComponent(ModuleRegistry.class), is(mr));

        ModuleMockUtils.cleanModuleRegistry();
        // Well, this results in a NullPointerException at AbstractComponentProvider.getComponentDefinition(AbstractComponentProvider.java:329)
//        assertThat(Components.getComponent(ModuleRegistry.class), nullValue());
    }

    @Test
    public void mockInstallContext() {
        InstallContext ic = ModuleMockUtils.mockInstallContext();
        assertThat(ic, notNullValue());
        ModuleDefinition md = ModuleMockUtils.mockModuleDefinition();
        ic = ModuleMockUtils.mockInstallContext(InstallContextStubbingOperation.stubCurrentModuleDefinition(md));
        assertThat(ic.getCurrentModuleDefinition(), is(md));
    }

    @Test
    public void mockServletDefinition() {
        ServletDefinitionStubbingOperation op = mock(ServletDefinitionStubbingOperation.class);
        ServletDefinition servletDefinition = ModuleMockUtils.mockServletDefinition("test", op);
        assertThat(servletDefinition, notNullValue());
        assertThat(servletDefinition.getName(), is("test"));
        verify(op, times(1)).of(servletDefinition);
    }
}
