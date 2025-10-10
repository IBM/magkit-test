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
 * Provides factory methods returning {@link ModuleRegistryStubbingOperation} instances used to configure Mockito mocks of
 * Magnolia's {@link ModuleRegistry}. Each operation focuses on registering or stubbing a single {@link ModuleDefinition}
 * entry, enabling concise and readable test setup when combined with {@link ModuleMockUtils#mockModuleRegistry(ModuleRegistryStubbingOperation...)}.
 * <p>
 * Typical use is to register one or more module definitions so other components under test can resolve them via the registry.
 * Operations can be composed; later stubs for the same name overwrite previous ones if executed in sequence.
 * </p>
 * <h3>Usage Example</h3>
 * <pre>{@code
 * ModuleDefinition def = ModuleMockUtils.mockModuleDefinition(
 *     "shop",
 *     ModuleDefinitionStubbingOperation.stubVersion("2.0.0")
 * );
 * ModuleRegistry registry = ModuleMockUtils.mockModuleRegistry(
 *     ModuleRegistryStubbingOperation.stubModuleDefinition("shop", def)
 * );
 * }</pre>
 * <h3>Null Handling</h3>
 * All parameters must be non-null unless explicitly stated; assertions guard against invalid input to surface test setup errors early.
 * <h3>Thread Safety</h3>
 * Returned operations are stateless and may be reused across tests; concurrent application to the same mock requires external synchronization.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2019-07-22
 */
public abstract class ModuleRegistryStubbingOperation implements StubbingOperation<ModuleRegistry> {

    /**
     * Creates an operation registering (stubbing) a module definition under the specified name.
     * <p>
     * The provided mock {@link ModuleDefinition} will be returned by {@link ModuleRegistry#getDefinition(String)} for the given name.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinition def = ModuleMockUtils.mockModuleDefinition("demo",
     *     ModuleDefinitionStubbingOperation.stubVersion("1.4.1")
     * );
     * ModuleRegistry registry = ModuleMockUtils.mockModuleRegistry(
     *     ModuleRegistryStubbingOperation.stubModuleDefinition("demo", def)
     * );
     * }</pre>
     *
     * @param name module name key used for lookup; must not be null
     * @param md the module definition mock to return; may be null if a test requires missing definition semantics
     * @return operation stubbing registry access for the given module
     */
    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final ModuleDefinition md) {
        return new ModuleRegistryStubbingOperation() {
            @Override
            public void of(ModuleRegistry mr) {
                assertThat(mr, notNullValue());
                doReturn(md).when(mr).getDefinition(name);
            }
        };
    }

    /**
     * Convenience operation creating a module definition using {@link ModuleDefinitionStubbingOperation#stubVersion(String)} for the given version
     * and registering it under the supplied name.
     * <p>
     * Equivalent to {@code stubModuleDefinition(name, stubVersion(version))}.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleRegistry registry = ModuleMockUtils.mockModuleRegistry(
     *     ModuleRegistryStubbingOperation.stubModuleDefinition("core", "1.0.0")
     * );
     * }</pre>
     *
     * @param name module name key; must not be null
     * @param version semantic version string (e.g. "1.0.0"); must not be null
     * @return operation stubbing registry access with versioned module definition
     */
    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final String version) {
        return stubModuleDefinition(name, stubVersion(version));
    }

    /**
     * Convenience operation creating and registering a module definition mock built from the provided stubbing operations.
     * <p>
     * Internally delegates to {@link ModuleMockUtils#mockModuleDefinition(String, ModuleDefinitionStubbingOperation...)} and
     * then registers the result via {@link #stubModuleDefinition(String, ModuleDefinition)}.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleRegistry registry = ModuleMockUtils.mockModuleRegistry(
     *     ModuleRegistryStubbingOperation.stubModuleDefinition(
     *         "analytics",
     *         ModuleDefinitionStubbingOperation.stubVersion("3.2.1"),
     *         ModuleDefinitionStubbingOperation.stubDescription("Analytics features")
     *     )
     * );
     * }</pre>
     *
     * @param name module name key; must not be null
     * @param stubbings zero or more operations configuring the created module definition; must not be null (may be empty)
     * @return operation stubbing registry access for the configured module definition
     */
    public static ModuleRegistryStubbingOperation stubModuleDefinition(final String name, final ModuleDefinitionStubbingOperation... stubbings) {
        ModuleDefinition md = mockModuleDefinition(name, stubbings);
        return stubModuleDefinition(name, md);
    }
}
