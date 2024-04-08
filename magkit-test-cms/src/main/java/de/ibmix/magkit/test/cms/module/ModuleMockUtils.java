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

import javax.jcr.RepositoryException;
import java.util.Arrays;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.clearComponentProvider;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Utility class to create Mockito mocks of magnolia module classes.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public abstract class ModuleMockUtils {

    /**
     * Util method for creating InstallContext mocks.
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2012-07-25
     * @param stubbings operations
     * @return install context
     */
    public static InstallContext mockInstallContext(final InstallContextStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        InstallContext result = mockComponentInstance(InstallContext.class);
        Arrays.stream(stubbings).forEach(stubbing -> {
            try {
                stubbing.of(result);
            } catch (RepositoryException e) {
                // ignore, not relevant while stubbing
            }
        });
        return result;
    }

    public static ModuleRegistry mockModuleRegistry(final ModuleRegistryStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        ModuleRegistry registry = mockComponentInstance(ModuleRegistry.class);
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(registry));
        return registry;
    }

    /**
     * Creates a ModuleDefinition mock with default name ("test") and provided behaviour that is registered at a ModuleRegistry.
     *
     * @param stubbings the StubbingOperations to stub properties of the module
     * @return a Mockito mock of a ModuleDefinition
     */
    public static ModuleDefinition mockModuleDefinition(final ModuleDefinitionStubbingOperation... stubbings) {
        return mockModuleDefinition("test", stubbings);

    }

    /**
     * Creates a ModuleDefinition mock with the given name and behaviour that is registered at a ModuleRegistry.
     *
     * @param name the name of the module
     * @param stubbings the StubbingOperations to stub properties of the module
     * @return a Mockito mock of a ModuleDefinition
     */
    public static ModuleDefinition mockModuleDefinition(final String name, final ModuleDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        ModuleDefinition result = mockModuleRegistry().getDefinition(name);
        if (result == null) {
            result = mock(ModuleDefinition.class);
            ModuleDefinitionStubbingOperation.stubName(name).of(result);
            mockModuleRegistry(ModuleRegistryStubbingOperation.stubModuleDefinition(name, result));
        }
        ModuleDefinition finalResult = result;
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(finalResult));
        return result;
    }

    public static ServletDefinition mockServletDefinition(final String name, final ServletDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        ServletDefinition result = mock(ServletDefinition.class);
        doReturn(name).when(result).getName();
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(result));
        return result;
    }

    public static void cleanModuleRegistry() {
        clearComponentProvider(ModuleRegistry.class);
    }

}
