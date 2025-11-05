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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubId;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubTitle;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @BeforeEach
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
        assertEquals(beforeSize + 1, afterList.size());
        assertNotSame(generic, area);
        assertEquals(Boolean.TRUE, area.getEnabled());
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
        assertSame(first, second);
        assertEquals(Boolean.TRUE, second.getEnabled());
        assertEquals(3, second.getMaxComponents());
        assertEquals(before, registry.getAllDefinitions().size());
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
        assertEquals(before, registry.getAllDefinitions().size());
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
        assertEquals(before, registry.getAllDefinitions().size());
    }

    /**
     * Both generic and area with same id are present after creation, order agnostic.
     */
    @Test
    public void shouldContainBothGenericAndAreaAfterUpgrade() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition generic = TemplateMockUtils.mockTemplateDefinition("mod:dual/id", stubTitle("G"));
        AreaDefinition area = TemplateMockUtils.mockAreaDefinition("mod:dual/id", AreaDefinitionStubbingOperation.stubEnabled(true));
        Collection<TemplateDefinition> defs = registry.getAllDefinitions();
        assertEquals(2, defs.size());
        assertTrue(defs.contains(generic));
        assertTrue(defs.contains(area));
    }

    @Test
    public void shouldInitRegistryAndAddDefinition() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        Collection<TemplateDefinition> initial = registry.getAllDefinitions();
        assertTrue(initial.isEmpty());
        TemplateDefinition def = TemplateMockUtils.mockTemplateDefinition("m:pages/a", stubTitle("A"));
        assertEquals("A", def.getTitle());
        assertEquals(1, registry.getAllDefinitions().size());
        assertTrue(registry.getAllDefinitions().contains(def));
    }

    @Test
    public void shouldReuseTemplateAndOverride() {
        TemplateDefinition first = TemplateMockUtils.mockTemplateDefinition("m:pages/b", stubTitle("T1"));
        TemplateDefinition second = TemplateMockUtils.mockTemplateDefinition("m:pages/b", stubDescription("Desc"), stubTitle("T2"));
        assertSame(first, second);
        assertEquals("T2", second.getTitle());
        assertEquals("Desc", second.getDescription());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldReuseConfiguredTemplate() {
        ConfiguredTemplateDefinition a = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/c", stubTitle("X"));
        ConfiguredTemplateDefinition b = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/c", stubTitle("Y"));
        assertSame(a, b);
        assertEquals("Y", b.getTitle());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldUpgradeGenericToConfigured() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition generic = TemplateMockUtils.mockTemplateDefinition("m:pages/d", stubTitle("G"));
        int before = registry.getAllDefinitions().size();
        ConfiguredTemplateDefinition configured = TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/d", stubTitle("C"));
        assertInstanceOf(ConfiguredTemplateDefinition.class, configured);
        assertEquals("C", configured.getTitle());
        Collection<TemplateDefinition> defs = registry.getAllDefinitions();
        assertEquals(before + 1, defs.size());
        assertTrue(defs.contains(generic));
        assertTrue(defs.contains(configured));
    }

    @Test
    public void shouldReuseArea() {
        AreaDefinition a = TemplateMockUtils.mockAreaDefinition("m:areas/header", AreaDefinitionStubbingOperation.stubEnabled(true));
        AreaDefinition b = TemplateMockUtils.mockAreaDefinition("m:areas/header", AreaDefinitionStubbingOperation.stubMaxComponents(9));
        assertSame(a, b);
        assertEquals(Boolean.TRUE, b.getEnabled());
        assertEquals(9, b.getMaxComponents());
    }

    @Test
    public void shouldNotRegisterBlankTemplateId() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition t = TemplateMockUtils.mockTemplateDefinition("", stubTitle("NoReg"));
        assertEquals("NoReg", t.getTitle());
        assertEquals(before, registry.getAllDefinitions().size());
    }

    @Test
    public void shouldRegisterProvidersAndAccumulate() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/e", stubTitle("One"));
        TemplateDefinition two = mock(TemplateDefinition.class);
        when(two.getId()).thenReturn("m:pages/f");
        DefinitionProvider<TemplateDefinition> provider = TemplateMockUtils.mockDefinitionProvider(two, true, 99L);
        TemplateMockUtils.register("m:pages/f", provider);
        assertTrue(registry.getAllDefinitions().contains(two));
        assertEquals(2, registry.getAllDefinitions().size());
    }

    @Test
    public void shouldCreateProviderDefaults() {
        TemplateDefinition def = mock(TemplateDefinition.class);
        when(def.getId()).thenReturn("m:pages/g");
        long start = System.currentTimeMillis();
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def);
        assertEquals(def, p.get());
        assertTrue(p.isValid());
        assertTrue(p.getLastModified() >= start);
        // tolerate minimal clock drift
        long endTime = System.currentTimeMillis() + 5;
        assertTrue(p.getLastModified() <= endTime);
    }

    @Test
    public void shouldCreateProviderExplicit() {
        TemplateDefinition def = mock(TemplateDefinition.class);
        when(def.getId()).thenReturn("m:pages/h");
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def, false, 123L);
        assertFalse(p.isValid());
        assertEquals(123L, p.getLastModified());
    }

    @Test
    public void shouldCleanRegistry() {
        TemplateDefinitionRegistry r1 = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/i", stubTitle("I"));
        assertEquals(1, r1.getAllDefinitions().size());
        TemplateMockUtils.cleanTemplateManager();
        TemplateDefinitionRegistry r2 = TemplateMockUtils.mockTemplateDefinitionRegistry();
        assertNotSame(r1, r2);
        assertTrue(r2.getAllDefinitions().isEmpty());
    }

    @Test
    public void shouldFailNullTemplateStubbings() {
        assertThrows(IllegalArgumentException.class, () -> TemplateMockUtils.mockTemplateDefinition("m:pages/j", (TemplateDefinitionStubbingOperation[]) null));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldFailNullConfiguredTemplateStubbings() {
        assertThrows(IllegalArgumentException.class, () -> TemplateMockUtils.mockConfiguredTemplateDefinition("m:pages/k", (TemplateDefinitionStubbingOperation[]) null));
    }

    @Test
    public void shouldFailNullAreaStubbings() {
        assertThrows(IllegalArgumentException.class, () -> TemplateMockUtils.mockAreaDefinition("m:areas/l", (AreaDefinitionStubbingOperation[]) null));
    }

    @Test
    public void shouldRegisterViaTemplateOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateDefinition def = mock(TemplateDefinition.class);
        stubId("m:pages/m").of(def);
        TemplateMockUtils.register("m:pages/m", def);
        assertTrue(registry.getAllDefinitions().contains(def));
    }

    @Test
    public void shouldIgnoreBlankRegisterProviderOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition def = mock(TemplateDefinition.class);
        DefinitionProvider<TemplateDefinition> p = TemplateMockUtils.mockDefinitionProvider(def, true, 1L);
        TemplateMockUtils.register("", p);
        assertEquals(before, registry.getAllDefinitions().size());
    }

    @Test
    public void shouldIgnoreBlankRegisterTemplateOverload() {
        TemplateDefinitionRegistry registry = TemplateMockUtils.mockTemplateDefinitionRegistry();
        int before = registry.getAllDefinitions().size();
        TemplateDefinition def = mock(TemplateDefinition.class);
        stubId("").of(def);
        TemplateMockUtils.register("", def);
        assertEquals(before, registry.getAllDefinitions().size());
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldWrapRegistrationException() {
        DefinitionProvider<TemplateDefinition> bad = TemplateMockUtils.mockDefinitionProvider(null);
        when(bad.get()).thenThrow(new Registry.InvalidDefinitionException("boom"));
        assertThrows(IllegalStateException.class, () -> TemplateMockUtils.register("m:pages/ex", bad));
    }

    @Test
    public void shouldNotReinitializeRegistryOnSecondCall() {
        TemplateDefinitionRegistry first = TemplateMockUtils.mockTemplateDefinitionRegistry();
        TemplateMockUtils.mockTemplateDefinition("m:pages/reuse", stubTitle("R"));
        int sizeAfterAdd = first.getAllDefinitions().size();
        TemplateDefinitionRegistry second = TemplateMockUtils.mockTemplateDefinitionRegistry();
        assertEquals(sizeAfterAdd, second.getAllDefinitions().size());
    }
}
