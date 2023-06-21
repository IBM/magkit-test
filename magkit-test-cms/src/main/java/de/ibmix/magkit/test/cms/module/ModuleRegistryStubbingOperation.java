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

import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;

import static de.ibmix.magkit.test.cms.module.ModuleDefinitionMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubVersion;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Providing operations for stubbing a magnolia ModuleRegistry - registering ModuleDefinition mocks.
 *
 * @author wolf.bubenik
 * @since 22.07.19.
 */
public abstract class ModuleRegistryStubbingOperation {

    public abstract void of(ModuleRegistry mr);

    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final ModuleDefinition md) {
        return new ModuleRegistryStubbingOperation() {
            @Override
            public void of(ModuleRegistry mr) {
                assertThat(mr, notNullValue());
                doReturn(md).when(mr).getDefinition(name);
            }
        };
    }

    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final String version) {
        return stubModuleDefinition(name, stubVersion(version));
    }

    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final ModuleDefinitionStubbingOperation... stubbings) {
        ModuleDefinition md = mockModuleDefinition(name, stubbings);
        return stubModuleDefinition(name, md);
    }

    private ModuleRegistryStubbingOperation() {}
}
