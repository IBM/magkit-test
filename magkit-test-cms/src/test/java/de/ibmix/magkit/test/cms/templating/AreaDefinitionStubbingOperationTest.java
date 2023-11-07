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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.InheritanceConfiguration;
import info.magnolia.rendering.template.TemplateDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testing AreaDefinitionStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-30
 */
public class AreaDefinitionStubbingOperationTest {

    private AreaDefinition _areaDefinition;
    private TemplateDefinition _templateDefinition;

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _areaDefinition = mock(AreaDefinition.class);
        _templateDefinition = mock(TemplateDefinition.class);
    }

    @After
    public void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubAvailableComponentsOfArea() {
        Map<String, ComponentAvailability> available = new HashMap<>();
        AreaDefinitionStubbingOperation.stubAvailableComponents(available).of(_areaDefinition);
        assertThat(_areaDefinition.getAvailableComponents(), is(available));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubAvailableComponentsOfTemplate() {
        AreaDefinitionStubbingOperation.stubAvailableComponents(null).of(_templateDefinition);
    }

    @Test
    public void stubContentStructureOfArea() {
        assertThat(_areaDefinition.getContentStructure(), nullValue());
        AreaDefinitionStubbingOperation.stubContentStructure("test").of(_areaDefinition);
        assertThat(_areaDefinition.getContentStructure(), is("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubContentStructureOfTemplate() {
        AreaDefinitionStubbingOperation.stubContentStructure(null).of(_templateDefinition);
    }

    @Test
    public void stubCreateAreaNodeOfArea() {
        assertThat(_areaDefinition.getCreateAreaNode(), is(false));

        AreaDefinitionStubbingOperation.stubCreateAreaNode(true).of(_areaDefinition);
        assertThat(_areaDefinition.getCreateAreaNode(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubCreateAreaNodeOfTemplate() {
        AreaDefinitionStubbingOperation.stubCreateAreaNode(null).of(_templateDefinition);
    }

    @Test
    public void stubEnabledOfArea() {
        assertThat(_areaDefinition.getEnabled(), is(false));

        AreaDefinitionStubbingOperation.stubEnabled(true).of(_areaDefinition);
        assertThat(_areaDefinition.getEnabled(), is(true));
    }

    @Test
    public void stubInheritanceOfArea() {
        assertThat(_areaDefinition.getInheritance(), nullValue());

        InheritanceConfiguration ic = mock(InheritanceConfiguration.class);
        AreaDefinitionStubbingOperation.stubInheritance(ic).of(_areaDefinition);
        assertThat(_areaDefinition.getInheritance(), is(ic));
        assertThat(_areaDefinition.getInheritance().isInheritsComponents(), is(false));
        assertThat(_areaDefinition.getInheritance().isInheritsProperties(), is(false));
        assertThat(_areaDefinition.getInheritance().isEnabled(), is(false));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubInheritanceOfTemplate() {
        AreaDefinitionStubbingOperation.stubInheritance(null).of(_templateDefinition);
    }

    @Test
    public void testStubInheritanceOfArea() {
        assertThat(_areaDefinition.getInheritance(), nullValue());

        AreaDefinitionStubbingOperation.stubInheritance(true, true, true).of(_areaDefinition);
        assertThat(_areaDefinition.getInheritance().isInheritsComponents(), is(true));
        assertThat(_areaDefinition.getInheritance().isInheritsProperties(), is(true));
        assertThat(_areaDefinition.getInheritance().isEnabled(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testStubInheritanceOfTemplate() {
        AreaDefinitionStubbingOperation.stubInheritance(true, true, true).of(_templateDefinition);
    }

    @Test
    public void stubMaxComponentsOfArea() {
        assertThat(_areaDefinition.getMaxComponents(), is(0));

        AreaDefinitionStubbingOperation.stubMaxComponents(5).of(_areaDefinition);
        assertThat(_areaDefinition.getMaxComponents(), is(5));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubMaxComponentsOfTemplate() {
        AreaDefinitionStubbingOperation.stubMaxComponents(null).of(_templateDefinition);
    }

    @Test
    public void stubOptionalOfArea() {
        assertThat(_areaDefinition.getOptional(), is(false));

        AreaDefinitionStubbingOperation.stubOptional(true).of(_areaDefinition);
        assertThat(_areaDefinition.getOptional(), is(true));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubOptionalOfTemplate() {
        AreaDefinitionStubbingOperation.stubOptional(null).of(_templateDefinition);
    }

    @Test
    public void stubTypeOfArea() {
        assertThat(_areaDefinition.getType(), is(nullValue()));

        AreaDefinitionStubbingOperation.stubType("test").of(_areaDefinition);
        assertThat(_areaDefinition.getType(), is("test"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void stubOTypeOfTemplate() {
        AreaDefinitionStubbingOperation.stubType(null).of(_templateDefinition);
    }
}
