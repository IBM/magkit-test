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

import info.magnolia.config.registry.DefinitionProvider;
import info.magnolia.config.registry.Registry;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubId;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubTitle;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing TemplateMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-04-08
 */
public class TemplateMockUtilsTest {

    @Before
    public void reset() {
        TemplateMockUtils.cleanTemplateManager();
    }

    /**
     * When a generic TemplateDefinition exists for an id, creating an AreaDefinition with same id must create a new instance.
     */
    @Test
    public void shouldCreateNewAreaIfExistingGenericTemplate() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("mod:shared/id", stubTitle("T"));
        List<TemplateDefinition> beforeList = new ArrayList<>(registry.getAllDefinitions());
        TemplateDefinition generic = beforeList.get(0);
        int beforeSize = beforeList.size();
        AreaDefinition area = TemplateMockUtils.mockAreaDefinition("mod:shared/id", AreaDefinitionStubbingOperation.stubEnabled(true));
        List<TemplateDefinition> afterList = new ArrayList<>(registry.getAllDefinitions());
        assertThat(afterList, hasSize(beforeSize + 1));
        assertThat(area, not(sameInstance(generic)));
        assertThat(area.getEnabled(), is(Boolean.TRUE));
    }

    /**
     * Creating an AreaDefinition twice for same id reuses the first area instance.
     */
    @Test
    public void shouldReuseAreaForSameId() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        AreaDefinition first = TemplateMockUtils.mockAreaDefinition("mod:areas/body", AreaDefinitionStubbingOperation.stubEnabled(true));
        int before = registry.getAllDefinitions().size();
        AreaDefinition second = TemplateMockUtils.mockAreaDefinition("mod:areas/body", AreaDefinitionStubbingOperation.stubMaxComponents(3));
        assertThat(second, sameInstance(first));
        assertThat(second.getEnabled(), is(Boolean.TRUE));
        assertThat(second.getMaxComponents(), is(3));
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    /**
     * Provider for blank id must not have get() invoked; no registration side effects.
     */
    @Test
    public void shouldNotInvokeProviderGetForBlankId() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        @SuppressWarnings("unchecked") DefinitionProvider<TemplateDefinition> provider = mock(DefinitionProvider.class);
        TemplateMockUtils.register("", provider);
        verify(provider, never()).get();
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    /**
     * Blank id registration via template overload must not add definition.
     */
    @Test
    public void shouldNotRegisterTemplateWithBlankId() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition def = mock(TemplateDefinition.class);
        stubId("").of(def);
        TemplateMockUtils.register("", def);
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    /**
     * Both generic and area with same id are present after creation, order agnostic.
     */
    @Test
    public void shouldContainBothGenericAndAreaAfterUpgrade() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition generic = TemplateMockUtils.mockTemplateDefinition("mod:dual/id", stubTitle("G"));
        AreaDefinition area = TemplateMockUtils.mockAreaDefinition("mod:dual/id", AreaDefinitionStubbingOperation.stubEnabled(true));
        assertThat(registry.getAllDefinitions(), hasSize(2));
        assertThat(registry.getAllDefinitions(), containsInAnyOrder(generic, area));
    }

    @Test
    public void shouldInitRegistryAndAddDefinition() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        Collection<TemplateDefinition> initial = registry.getAllDefinitions();
        assertThat(initial.isEmpty(), is(true));
        TemplateDefinition def = TemplateMockUtils.mockTemplateDefinition("m:pages/a", stubTitle("A"));
        assertThat(def.getTitle(), is("A"));
        assertThat(registry.getAllDefinitions().size(), is(1));
        assertThat(registry.getAllDefinitions().contains(def), is(true));
    }

    @Test
    public void shouldReuseTemplateAndOverride() {
        TemplateDefinition first = TemplateMockUtils.mockTemplateDefinition("m:pages/b", stubTitle("T1"));
        TemplateDefinition second = TemplateMockUtils.mockTemplateDefinition("m:pages/b", stubDescription("Desc"), stubTitle("T2"));
        assertThat(first, sameInstance(second));
        assertThat(second.getTitle(), is("T2"));
        assertThat(second.getDescription(), is("Desc"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldReuseConfiguredTemplate() {
        ConfiguredTemplateDefinition a = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/c", stubTitle("X"));
        ConfiguredTemplateDefinition b = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/c", stubTitle("Y"));
        assertThat(a, sameInstance(b));
        assertThat(b.getTitle(), is("Y"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldUpgradeGenericToConfigured() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition generic = TemplateMockUtils.mockTemplateDefinition("m:pages/d", stubTitle("G"));
        int before = registry.getAllDefinitions().size();
        ConfiguredTemplateDefinition configured = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/d", stubTitle("C"));
        assertThat(configured, instanceOf(ConfiguredTemplateDefinition.class));
        assertThat(configured.getTitle(), is("C"));
        Collection<TemplateDefinition> defs = registry.getAllDefinitions();
        assertThat(defs.size(), is(before + 1));
        assertThat(defs, hasItems(generic, configured));
    }

    @Test
    public void shouldReuseArea() {
        AreaDefinition a = TemplateMockUtils.mockAreaDefinition("m:areas/header", AreaDefinitionStubbingOperation.stubEnabled(true));
        AreaDefinition b = TemplateMockUtils.mockAreaDefinition("m:areas/header", AreaDefinitionStubbingOperation.stubMaxComponents(9));
        assertThat(a, sameInstance(b));
        assertThat(b.getEnabled(), is(Boolean.TRUE));
        assertThat(b.getMaxComponents(), is(9));
    }

    @Test
    public void shouldNotRegisterBlankTemplateId() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition t = TemplateMockUtils.mockTemplateDefinition("", stubTitle("NoReg"));
        assertThat(t.getTitle(), is("NoReg"));
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    @Test
    public void shouldRegisterProvidersAndAccumulate() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/e", stubTitle("One"));
        TemplateDefinition two = mock(TemplateDefinition.class);
        when(two.getId()).thenReturn("m:pages/f");
        DefinitionProvider<TemplateDefinition> provider = TemplateMockUtils.mockDefinitionProvider(two, true, 99L);
        TemplateMockUtils.register("m:pages/f", provider);
        assertThat(registry.getAllDefinitions().contains(two), is(true));
        assertThat(registry.getAllDefinitions().size(), is(2));
    }

    @Test
    public void shouldCreateProviderDefaults() {
        TemplateDefinition def = mock(TemplateDefinition.class);
        when(def.getId()).thenReturn("m:pages/g");
        long start = System.currentTimeMillis();
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def);
        assertThat(p.get(), is(def));
        assertThat(p.isValid(), is(true));
        assertThat(p.getLastModified(), greaterThanOrEqualTo(start));
        assertThat(p.getLastModified(), lessThanOrEqualTo(System.currentTimeMillis() + 5));
    }

    @Test
    public void shouldCreateProviderExplicit() {
        TemplateDefinition def = mock(TemplateDefinition.class);
        when(def.getId()).thenReturn("m:pages/h");
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def, false, 123L);
        assertThat(p.isValid(), is(false));
        assertThat(p.getLastModified(), is(123L));
    }

    @Test
    public void shouldCleanRegistry() {
        TemplateDefinitionRegistry r1 = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/i", stubTitle("I"));
        assertThat(r1.getAllDefinitions().size(), is(1));
        TemplateMockUtils.cleanTemplateManager();
        TemplateDefinitionRegistry r2 = TemplateMockUtils.mockTemplateDefinitionRegistry();
        assertThat(r2, not(sameInstance(r1)));
        assertThat(r2.getAllDefinitions().isEmpty(), is(true));
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldFailNullTemplateStubbings() {
        TemplateMockUtils.mockTemplateDefinition("m:pages/j", (TemplateDefinitionStubbingOperation[]) null);
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings({"deprecation", "rawtypes", "unchecked"})
    public void shouldFailNullConfiguredTemplateStubbings() {
        TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/k", (TemplateDefinitionStubbingOperation[]) null);
    }

    @Test(expected = AssertionError.class)
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void shouldFailNullAreaStubbings() {
        TemplateMockUtils.mockAreaDefinition("m:areas/l", (AreaDefinitionStubbingOperation[]) null);
    }

    @Test
    public void shouldRegisterViaTemplateOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition def = mock(TemplateDefinition.class);
        stubId("m:pages/m").of(def);
        TemplateMockUtils.register("m:pages/m", def);
        assertThat(registry.getAllDefinitions().contains(def), is(true));
    }

    @Test
    public void shouldIgnoreBlankRegisterProviderOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition def = mock(TemplateDefinition.class);
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def, true, 1L);
        TemplateMockUtils.register("", p);
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    @Test
    public void shouldIgnoreBlankRegisterTemplateOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition def = mock(TemplateDefinition.class);
        stubId("").of(def);
        TemplateMockUtils.register("", def);
        assertThat(registry.getAllDefinitions().size(), is(before));
    }

    @Test(expected = IllegalStateException.class)
    @SuppressWarnings("deprecation")
    public void shouldWrapRegistrationException() {
        DefinitionProvider<TemplateDefinition> bad = TemplateMockUtils.mockDefinitionProvider(null);
        when(bad.get()).thenThrow(new Registry.InvalidDefinitionException("boom"));
        TemplateMockUtils.register("m:pages/ex", bad);
    }

    @Test
    public void shouldNotReinitializeRegistryOnSecondCall() {
        TemplateDefinitionRegistry first = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/reuse", stubTitle("R"));
        int sizeAfterAdd = first.getAllDefinitions().size();
        TemplateDefinitionRegistry second = TemplateMockUtils.mockTemplateDefinitionRegistry();
        assertThat(second.getAllDefinitions().size(), equalTo(sizeAfterAdd));
    }
}
