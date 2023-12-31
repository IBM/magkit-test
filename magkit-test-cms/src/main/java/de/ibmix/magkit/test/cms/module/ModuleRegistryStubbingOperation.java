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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;

import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubVersion;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;

/**
 * Providing operations for stubbing a magnolia ModuleRegistry - registering ModuleDefinition mocks.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2019-07-22
 */
public abstract class ModuleRegistryStubbingOperation implements StubbingOperation<ModuleRegistry> {

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
}
