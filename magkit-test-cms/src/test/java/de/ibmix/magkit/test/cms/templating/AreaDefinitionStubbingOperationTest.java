package de.ibmix.magkit.test.cms.templating;

/*-
 * #%L
 * magkit-test Magnolia Module
 * %%
 * Copyright (C) 2025 IBM iX
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

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.InheritanceConfiguration;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubAvailableComponents;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubContentStructure;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubCreateAreaNode;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubEnabled;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubInheritance;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubMaxComponents;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubOptional;
import static de.ibmix.magkit.test.cms.templating.AreaDefinitionStubbingOperation.stubType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link AreaDefinitionStubbingOperation} covering all stubbing factory methods, both inheritance overloads,
 * null handling (allowed null values) and assertion behavior on null template instance.
 * Ensures chaining possibility implicitly by applying multiple operations to one mock.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-14
 */
public class AreaDefinitionStubbingOperationTest {

    /**
     * Tests stubbing of all simple area-specific properties including availableComponents, contentStructure,
     * createAreaNode, enabled, inheritance (direct object), maxComponents, optional, and type.
     */
    @Test
    public void shouldStubAllAreaSpecificProperties() {
        AreaDefinition area = mock(AreaDefinition.class);

        Map<String, ComponentAvailability> components = new HashMap<>();
        ComponentAvailability availability = mock(ComponentAvailability.class);
        components.put("compA", availability);

        InheritanceConfiguration inheritance = mock(InheritanceConfiguration.class);

        stubAvailableComponents(components).of(area);
        stubContentStructure("grid/12").of(area);
        stubCreateAreaNode(Boolean.TRUE).of(area);
        stubEnabled(Boolean.FALSE).of(area);
        stubInheritance(inheritance).of(area);
        stubMaxComponents(7).of(area);
        stubOptional(Boolean.TRUE).of(area);
        stubType("main").of(area);

        assertThat(area.getAvailableComponents(), is(components));
        assertThat(area.getAvailableComponents(), hasEntry("compA", availability));
        assertThat(area.getContentStructure(), is("grid/12"));
        assertThat(area.getCreateAreaNode(), is(Boolean.TRUE));
        assertThat(area.getEnabled(), is(Boolean.FALSE));
        assertThat(area.getInheritance(), is(inheritance));
        assertThat(area.getMaxComponents(), is(7));
        assertThat(area.getOptional(), is(Boolean.TRUE));
        assertThat(area.getType(), is("main"));
    }

    /**
     * Tests the overload stubInheritance(Boolean, Boolean, Boolean) generating a mocked configuration with predicate and comparator.
     */
    @Test
    public void shouldStubInheritanceFromFlags() {
        AreaDefinition area = mock(AreaDefinition.class);
        stubInheritance(Boolean.TRUE, Boolean.FALSE, Boolean.TRUE).of(area);

        InheritanceConfiguration cfg = area.getInheritance();
        assertThat(cfg, notNullValue());
        assertThat(cfg.isEnabled(), is(Boolean.TRUE));
        assertThat(cfg.isInheritsProperties(), is(Boolean.FALSE));
        assertThat(cfg.isInheritsComponents(), is(Boolean.TRUE));
        assertThat(cfg.getComponentPredicate(), notNullValue());
        assertThat(cfg.getComponentComparator(), notNullValue());
    }

    /**
     * Verifies that null values provided to stubs are returned as null (allowed), without throwing.
     */
    @Test
    public void shouldAllowNullValues() {
        AreaDefinition area = mock(AreaDefinition.class);
        stubAvailableComponents(null).of(area);
        stubContentStructure(null).of(area);
        stubCreateAreaNode(null).of(area);
        stubEnabled(null).of(area);
        stubInheritance((InheritanceConfiguration) null).of(area);
        stubMaxComponents(null).of(area);
        stubOptional(null).of(area);
        stubType(null).of(area);

        assertThat(area.getAvailableComponents(), nullValue());
        assertThat(area.getContentStructure(), nullValue());
        assertThat(area.getCreateAreaNode(), nullValue());
        assertThat(area.getEnabled(), nullValue());
        assertThat(area.getInheritance(), nullValue());
        assertThat(area.getMaxComponents(), nullValue());
        assertThat(area.getOptional(), nullValue());
        assertThat(area.getType(), nullValue());
    }

    /**
     * Verifies assertion failure (Hamcrest) when a null AreaDefinition is supplied.
     */
    @Test(expected = AssertionError.class)
    public void shouldFailOnNullArea() {
        stubEnabled(true).of(null);
    }
}
