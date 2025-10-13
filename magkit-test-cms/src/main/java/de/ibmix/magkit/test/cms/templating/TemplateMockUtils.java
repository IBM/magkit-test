package de.ibmix.magkit.test.cms.templating;

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

import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubId;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility methods for creating and registering Mockito based mocks of Magnolia {@link TemplateDefinition},
 * {@link AreaDefinition} and their {@link TemplateDefinitionRegistry}. The helpers centralize repetitive test
 * setup logic so individual tests stay concise and intention revealing.
 * <ul>
 *     <li>Provides a lazily created singleton mock of {@link TemplateDefinitionRegistry} backed by the Magnolia component provider.</li>
 *     <li>Creates (or reuses) mocked template and area definitions identified by an id and optionally applies fluent stubbing operations.</li>
 *     <li>Registers mocked definition providers updating the registry's returned collections accordingly.</li>
 *     <li>Supplies convenience factory methods for {@link DefinitionProvider} instances with controllable validity and timestamp.</li>
 * </ul>
 * Typical usage:
 * <pre>{@code
 * TemplateDefinition home = TemplateMockUtils.mockTemplateDefinition("my-module:pages/home",
 *     TemplateDefinitionStubbingOperation.stubTitle("Home"));
 * AreaDefinition header = TemplateMockUtils.mockAreaDefinition("my-module:areas/header");
 * // The registry mock now returns the created definitions.
 * TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
 * assertSame(home, registry.getTemplateDefinition("my-module:pages/home"));
 * }</pre>
 * Thread safety: Implementation is backed by ThreadLocal. The helpers are intended for multi threaded unit test execution.
 * Side effects: Methods that register definitions mutate the state (stubbing) of the shared registry mock.
 * Clean up: Call {@link #cleanTemplateManager()} between tests if test isolation is required.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-08-27
 */
public final class TemplateMockUtils extends ComponentsMockUtils {

    /**
     * Obtain the shared mock of {@link TemplateDefinitionRegistry}. If a mock has already been created via previous
     * calls it is returned unchanged. Otherwise a new mock is instantiated, registered with the Magnolia component
     * provider (through {@link ComponentsMockUtils#mockComponentInstance(Class)}) and initialized so that
     * {@link TemplateDefinitionRegistry#getAllDefinitions()} and {@link TemplateDefinitionRegistry#getTemplateDefinitions()} both
     * return the same mutable backing list. Subsequent calls to the various register helper methods replace those
     * stubbings to reflect newly added definitions.
     *
     * @return the shared mocked {@link TemplateDefinitionRegistry} instance (never {@code null})
     */
    @SuppressWarnings("deprecation")
    public static TemplateDefinitionRegistry mockTemplateDefinitionRegistry() {
        TemplateDefinitionRegistry result = mockComponentInstance(TemplateDefinitionRegistry.class);
        if (result.getAllDefinitions() == null) {
            List<TemplateDefinition> allDefinitions = new ArrayList<>();
            when(result.getAllDefinitions()).thenReturn(allDefinitions);
            when(result.getTemplateDefinitions()).thenReturn(allDefinitions);
        }
        return result;
    }

    /**
     * Create or reuse a mocked {@link ConfiguredTemplateDefinition} for the given id and apply the provided
     * stubbing operations. If a provider for the id already exists in the registry and yields a
     * {@link ConfiguredTemplateDefinition}, that existing mock is reused to allow additive stubbing across test code.
     * Otherwise a new mock is created, its id is stubbed (using {@link TemplateDefinitionStubbingOperation#stubId(String)})
     * and it is registered in the registry.
     * <p>
     * The method is deprecated in favor of {@link #mockTemplateDefinition(String, TemplateDefinitionStubbingOperation...)}
     * because most tests do not need to rely on the concrete configured implementation type.
     *
     * @param id        unique template id (e.g. {@code my-module:pages/home}); may be blank for sub templates (won't be registered)
     * @param stubbings ordered, non-null array of stubbing operations to apply; pass an empty array for none
     * @return the existing or newly created mock {@link ConfiguredTemplateDefinition}
     * @deprecated Prefer {@link #mockTemplateDefinition(String, TemplateDefinitionStubbingOperation...)} which returns the interface type.
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public static ConfiguredTemplateDefinition mockConfiguredTemplateDefinition(String id, TemplateDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        ConfiguredTemplateDefinition result;
        DefinitionProvider<TemplateDefinition> existingProvider = registry.getProvider(id);
        if (existingProvider != null && existingProvider.get() instanceof ConfiguredTemplateDefinition) {
            result = (ConfiguredTemplateDefinition) existingProvider.get();
        } else {
            result = mock(ConfiguredTemplateDefinition.class);
            stubId(id).of(result);
            register(id, result);
        }
        for (TemplateDefinitionStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Create or reuse a mocked generic {@link TemplateDefinition}. If a provider for the supplied id is already
     * registered its existing definition is reused, allowing multiple test helpers to augment the same mock.
     * Otherwise a new mock is created, its id stubbed and then registered so the shared registry will resolve it via
     * {@link TemplateDefinitionRegistry#getTemplateDefinition(String)} and include it in returned collections.
     *
     * @param id        unique template id (e.g. {@code my-module:pages/home}); if blank the definition is created but not registered
     * @param stubbings ordered, non-null array of stubbing operations to apply; pass an empty array for none
     * @return the existing or newly created mock {@link TemplateDefinition}
     */
    @SuppressWarnings("deprecation")
    public static TemplateDefinition mockTemplateDefinition(String id, TemplateDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        TemplateDefinition result;
        DefinitionProvider<TemplateDefinition> provider = registry.getProvider(id);
        if (provider == null) {
            result = mock(TemplateDefinition.class);
            stubId(id).of(result);
            register(id, result);
        } else {
            result = provider.get();
        }
        for (TemplateDefinitionStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Create or reuse a mocked {@link AreaDefinition}. If a definition with the id already exists and is an
     * {@link AreaDefinition} it is reused so that additional stubbing extends previous expectations. Otherwise a new
     * mock is created, its id stubbed and the definition registered in the shared registry mock.
     *
     * @param id        unique area definition id; if blank the mock is not registered
     * @param stubbings ordered, non-null array of stubbing operations to apply; pass an empty array for none
     * @return the existing or newly created mock {@link AreaDefinition}
     */
    public static AreaDefinition mockAreaDefinition(String id, AreaDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        AreaDefinition result;
        DefinitionProvider<TemplateDefinition> provider = registry.getProvider(id);
        if (provider != null && provider.get() instanceof AreaDefinition) {
            result = (AreaDefinition) provider.get();
        } else {
            result = mock(AreaDefinition.class);
            stubId(id).of(result);
            register(id, result);
        }
        for (AreaDefinitionStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Convenience overload registering a raw {@link TemplateDefinition} by wrapping it inside an automatically
     * generated {@link DefinitionProvider}. Delegates to {@link #register(String, DefinitionProvider)}.
     *
     * @param id        unique template id; if blank the definition is not registered
     * @param template  the mock or concrete {@link TemplateDefinition} instance to expose via the registry
     */
    public static void register(String id, TemplateDefinition template) {
        register(id, mockDefinitionProvider(template));
    }

    /**
     * Register (stub) the provided {@link DefinitionProvider} into the shared {@link TemplateDefinitionRegistry} mock
     * so that future calls to {@link TemplateDefinitionRegistry#getProvider(String)} and
     * {@link TemplateDefinitionRegistry#getTemplateDefinition(String)} return it. Additionally updates the previously
     * stubbed collections returned by {@link TemplateDefinitionRegistry#getAllDefinitions()} and
     * {@link TemplateDefinitionRegistry#getTemplateDefinitions()} to include the new definition. Blank ids are ignored
     * (intended for sub templates without their own id).
     *
     * @param id        unique template id; empty or {@code null} values skip registration
     * @param provider  provider whose {@link DefinitionProvider#get()} result must be a {@link TemplateDefinition}
     * @param <T>       concrete definition type extending {@link RenderableDefinition}
     */
    @SuppressWarnings("deprecation")
    public static <T extends RenderableDefinition> void register(String id, DefinitionProvider<T> provider) {
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        if (isNotBlank(id)) {
            try {
                doReturn(provider).when(registry).getProvider(id);
                TemplateDefinition templateDefinition = (TemplateDefinition) provider.get();
                when(registry.getTemplateDefinition(id)).thenReturn(templateDefinition);
                List<TemplateDefinition> newDefinitions = new ArrayList<>(registry.getAllDefinitions());
                newDefinitions.add(templateDefinition);
                when(registry.getTemplateDefinitions()).thenReturn(newDefinitions);
                when(registry.getAllDefinitions()).thenReturn(newDefinitions);
            } catch (RegistrationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Create a mocked {@link DefinitionProvider} for the supplied definition marking it as valid and using the
     * current system time as its last modified timestamp.
     *
     * @param definition definition instance to wrap
     * @param <T>        concrete definition type extending {@link RenderableDefinition}
     * @return mocked provider returning the given definition
     */
    public static <T extends RenderableDefinition> DefinitionProvider<T> mockDefinitionProvider(T definition) {
        return mockDefinitionProvider(definition, true, System.currentTimeMillis());
    }

    /**
     * Create a mocked {@link DefinitionProvider} for the supplied definition allowing explicit control over the
     * validity flag and last modified timestamp. Useful for simulating reload scenarios or invalid configuration states.
     *
     * @param definition definition instance to wrap
     * @param isValid    value returned by {@link DefinitionProvider#isValid()}
     * @param timestamp  value returned by {@link DefinitionProvider#getLastModified()} (epoch millis)
     * @param <T>        concrete definition type extending {@link RenderableDefinition}
     * @return mocked provider with configured behavior
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends RenderableDefinition> DefinitionProvider<T> mockDefinitionProvider(T definition, boolean isValid, long timestamp) {
        DefinitionProvider result = mock(DefinitionProvider.class);
        doReturn(definition).when(result).get();
        doReturn(isValid).when(result).isValid();
        doReturn(timestamp).when(result).getLastModified();
        return result;
    }

    /**
     * Remove the mocked {@link TemplateDefinitionRegistry} from the Magnolia component provider so a fresh mock can
     * be created on the next call to {@link #mockTemplateDefinitionRegistry()}. Intended for test isolation across
     * suites when static mocking state would otherwise leak.
     */
    public static void cleanTemplateManager() {
        clearComponentProvider(TemplateDefinitionRegistry.class);
    }

    /**
     * Not instantiable.
     */
    private TemplateMockUtils() {
    }
}
