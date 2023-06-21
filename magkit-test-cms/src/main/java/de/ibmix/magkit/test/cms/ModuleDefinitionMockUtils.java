package de.ibmix.magkit.test.cms;

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

import info.magnolia.module.model.ModuleDefinition;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.mockito.Mockito.mock;

/**
 * Utility class to create Mockito mocks of a magnolia ModuleDefinition.
 *
 * @author wolf.bubenik
 */
public final class ModuleDefinitionMockUtils {

    /**
     * Creates a plain ModuleDefinition mock that is not registered at an ModuleRegistry.
     *
     * @return a ModuleDefinition mock, never null.
     */
    public static ModuleDefinition mockModuleDefinition() {
        return mock(ModuleDefinition.class);
    }

    /**
     * Creates a ModuleDefinition mock wit provided behaviour that is not registered at an ModuleRegistry.
     *
     * @param stubbings the StubbingOperations to stub properties of the module
     * @return a Mockito mock of a ModuleDefinition
     */
    public static ModuleDefinition mockModuleDefinition(ModuleDefinitionStubbingOperation... stubbings) {
        ModuleDefinition result = mockModuleDefinition();
        for (ModuleDefinitionStubbingOperation stub : stubbings) {
            stub.of(result);
        }
        return result;

    }

    /**
     * Creates a ModuleDefinition mock with the given name and a ModuleRegistry mock that returns this module.
     *
     * @param name the name of the module
     * @param stubbings the StubbingOperations to stub properties of the module
     * @return a Mockito mock of a ModuleDefinition
     */
    public static ModuleDefinition mockModuleDefinition(String name, ModuleDefinitionStubbingOperation... stubbings) {
        ModuleDefinition result = mockModuleDefinition(stubbings);
        if (isNotEmpty(name)) {
            ModuleDefinitionStubbingOperation.stubName(name).of(result);
        }
        return result;
    }

    private ModuleDefinitionMockUtils() {
    }

}
