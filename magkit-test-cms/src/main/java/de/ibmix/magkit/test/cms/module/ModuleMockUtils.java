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

import de.ibmix.magkit.assertions.Require;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;

import javax.jcr.RepositoryException;
import java.util.Arrays;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.clearComponentProvider;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Utility class providing factory methods to create Mockito based test doubles (mocks) for Magnolia module related types.
 * <p>
 * The methods centralize repetitive mock setup for {@link InstallContext}, {@link ModuleRegistry}, {@link ModuleDefinition} and
 * {@link ServletDefinition}. Each factory accepts one or more "stubbing operations" allowing callers to configure
 * behaviour (return values, state) of the mock in a fluent way. This reduces duplication and keeps test code concise and
 * readable. All mocks are created using the shared component provider (via {@link de.ibmix.magkit.test.cms.context.ComponentsMockUtils})
 * to integrate with other test utilities.
 * </p>
 * <h3>Thread Safety</h3>
 * The helper relies on a static component provider that is backed by ThreadLocal. It is intended for concurrent use across parallel test execution.
 * Call {@link #cleanModuleRegistry()} between tests if isolation is required.
 * <h3>Usage Example</h3>
 * <pre>{@code
 * ModuleDefinition module = ModuleMockUtils.mockModuleDefinition(
 *     ModuleDefinitionStubbingOperation.stubName("my-module"),
 *     ModuleDefinitionStubbingOperation.stubVersion("1.0")
 * );
 * }
 * </pre>
 * <h3>Error Handling</h3>
 * Any {@link RepositoryException} thrown while applying stubbing logic for an {@link InstallContext} is caught and ignored
 * because repository interaction is not relevant for pure mocking; tests expecting repository failures must simulate them explicitly.
 * <p><b>Thread safety:</b> Implementation is backed by ComponentProvider that uses ThreadLocal and is thread-safe; intended for multithreaded test initialization code.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public abstract class ModuleMockUtils {

    /**
     * Creates and returns a mock of {@link InstallContext} and applies the given stubbing operations.
     * <p>
     * Provided {@code stubbings} allow configuration of the mock. Any {@link RepositoryException} optionally thrown by
     * a stubbing implementation is suppressed because the mock creation phase should not fail for repository access.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * InstallContext ctx = ModuleMockUtils.mockInstallContext(
     *     InstallContextStubbingOperation.stubCurrentModule(moduleDef),
     *     InstallContextStubbingOperation.stubAttribute("key", "value")
     * );
     * }</pre>
     *
     * @author wolf.bubenik@ibmix.de
     * @since 2012-07-25
     * @param stubbings one or more operations configuring the mock; must not be {@code null} (may be empty)
     * @return configured mock instance of {@link InstallContext}
     * @see InstallContextStubbingOperation
     */
    public static InstallContext mockInstallContext(final InstallContextStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Creates and returns a mock of {@link ModuleRegistry} applying each supplied stubbing operation.
     * <p>
     * Typical usage registers module definitions for lookup inside other tests.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleRegistry registry = ModuleMockUtils.mockModuleRegistry(
     *     ModuleRegistryStubbingOperation.stubModuleDefinition("my-module", moduleDef)
     * );
     * }</pre>
     *
     * @param stubbings one or more operations configuring the registry mock; must not be {@code null} (may be empty)
     * @return configured mock instance of {@link ModuleRegistry}
     * @see ModuleRegistryStubbingOperation
     */
    public static ModuleRegistry mockModuleRegistry(final ModuleRegistryStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        ModuleRegistry registry = mockComponentInstance(ModuleRegistry.class);
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(registry));
        return registry;
    }

    /**
     * Convenience overload creating a {@link ModuleDefinition} mock with the default name {@code "test"}.
     * <p>
     * Internally delegates to {@link #mockModuleDefinition(String, ModuleDefinitionStubbingOperation...)}.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinition module = ModuleMockUtils.mockModuleDefinition(
     *     ModuleDefinitionStubbingOperation.stubVersion("1.2.3")
     * );
     * }</pre>
     *
     * @param stubbings one or more operations configuring the module definition; must not be {@code null} (may be empty)
     * @return configured mock instance of {@link ModuleDefinition}
     * @see ModuleDefinitionStubbingOperation
     */
    public static ModuleDefinition mockModuleDefinition(final ModuleDefinitionStubbingOperation... stubbings) {
        return mockModuleDefinition("test", stubbings);

    }

    /**
     * Creates a {@link ModuleDefinition} mock with the given name and applies all provided stubbing operations.
     * <p>
     * If a module with the given {@code name} is not yet registered in a mocked {@link ModuleRegistry}, a new mock is created
     * and registered automatically. Subsequent calls for the same name will reuse the existing mock allowing additive stubbing.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinition module = ModuleMockUtils.mockModuleDefinition(
     *     "shop",
     *     ModuleDefinitionStubbingOperation.stubVersion("2.0"),
     *     ModuleDefinitionStubbingOperation.stubName("shop")
     * );
     * }</pre>
     *
     * @param name unique module name used for registry lookup and stubbing
     * @param stubbings one or more operations configuring the module definition; must not be {@code null} (may be empty)
     * @return existing or newly created configured mock instance of {@link ModuleDefinition}
     * @see ModuleDefinitionStubbingOperation
     * @see ModuleRegistryStubbingOperation
     */
    public static ModuleDefinition mockModuleDefinition(final String name, final ModuleDefinitionStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
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

    /**
     * Creates a {@link ServletDefinition} mock with the given servlet name and applies provided stubbing operations.
     * <p>
     * The servlet name is stubbed directly; other attributes (e.g. class, parameters) can be configured via the supplied operations.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * ServletDefinition servlet = ModuleMockUtils.mockServletDefinition(
     *     "myServlet",
     *     ServletDefinitionStubbingOperation.stubClass(MyServlet.class)
     * );
     * }</pre>
     *
     * @param name servlet name used in Magnolia configuration
     * @param stubbings one or more operations configuring the servlet definition; must not be {@code null} (may be empty)
     * @return configured mock instance of {@link ServletDefinition}
     * @see ServletDefinitionStubbingOperation
     */
    public static ServletDefinition mockServletDefinition(final String name, final ServletDefinitionStubbingOperation... stubbings) {
        Require.Argument.notNull(stubbings, "stubbings should not be null");
        ServletDefinition result = mock(ServletDefinition.class);
        doReturn(name).when(result).getName();
        Arrays.stream(stubbings).forEach(stubbing -> stubbing.of(result));
        return result;
    }

    /**
     * Removes any mocked {@link ModuleRegistry} instance from the shared component provider to ensure a clean test state.
     * <p>
     * Invoke after tests that manipulate the module registry to avoid cross-test interference.
     * </p>
     */
    public static void cleanModuleRegistry() {
        clearComponentProvider(ModuleRegistry.class);
    }

    private ModuleMockUtils() {}

}
