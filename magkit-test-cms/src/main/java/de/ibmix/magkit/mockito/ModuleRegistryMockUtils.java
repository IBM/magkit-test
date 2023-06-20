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

import info.magnolia.module.ModuleRegistry;

/**
 * Utility class to create Mockito mocks of a magnolia ModuleRegistry.
 *
 * @author wolf.bubenik
 * @since 22.07.19.
 */
public final class ModuleRegistryMockUtils extends ComponentsMockUtils {

    public static ModuleRegistry mockModuleRegistry(ModuleRegistryStubbingOperation... stubbings) {
        ModuleRegistry config = mockModuleRegistry();
        for (ModuleRegistryStubbingOperation stubbing : stubbings) {
            stubbing.of(config);
        }
        return config;
    }

    private static ModuleRegistry mockModuleRegistry() {
        return mockComponentInstance(ModuleRegistry.class);
    }

    public static void cleanModuleRegistry() {
        clearComponentProvider(ModuleRegistry.class);
    }

    private ModuleRegistryMockUtils() {}
}
