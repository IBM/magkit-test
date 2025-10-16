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

import info.magnolia.rendering.template.TemplateDefinition;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDeletable;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubDialog;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubEditable;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubId;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubI18nBasename;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubMoveable;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubParameter;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubRenderType;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubSubtype;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubTemplateScript;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubTitle;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubType;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubVisible;
import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.stubWritable;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link TemplateDefinitionStubbingOperation}. Covers all stubbing factory methods and
 * the branching logic inside {@code stubParameter} (null/empty name, null existing map, existing map, add, remove).
 * Demonstrates composability of operations and assertion failure on null template.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-14
 */
public class TemplateDefinitionStubbingOperationTest {

    /**
     * Verifies that every simple property stubbing delegates to the corresponding getter and returns the configured values.
     */
    @Test
    @SuppressWarnings("deprecation")
    public void shouldStubAllSimpleProperties() {
        TemplateDefinition template = mock(TemplateDefinition.class);

        stubDeletable(true).of(template);
        stubDialog("dialog-id").of(template);
        stubEditable(false).of(template);
        stubMoveable(true).of(template);
        stubVisible(false).of(template);
        stubWritable(true).of(template);
        stubType("page").of(template);
        stubSubtype("home").of(template);
        stubI18nBasename("i18n.basename").of(template);
        stubDescription("Some description").of(template);
        stubId("module:pages/home").of(template);
        stubName("home").of(template);
        stubTitle("Home Page").of(template);
        stubTemplateScript("/templates/pages/home.ftl").of(template);
        stubRenderType("freemarker").of(template);
        stubParameter("limit", 5).of(template);

        assertEquals(true, template.getDeletable());
        assertEquals("dialog-id", template.getDialog());
        assertEquals(false, template.getEditable());
        assertEquals(true, template.getMoveable());
        assertEquals(false, template.getVisible());
        assertEquals(true, template.getWritable());
        assertEquals("page", template.getType());
        assertEquals("home", template.getSubtype());
        assertEquals("i18n.basename", template.getI18nBasename());
        assertEquals("Some description", template.getDescription());
        assertEquals("module:pages/home", template.getId());
        assertEquals("home", template.getName());
        assertEquals("Home Page", template.getTitle());
        assertEquals("/templates/pages/home.ftl", template.getTemplateScript());
        assertEquals("freemarker", template.getRenderType());
        assertNotNull(template.getParameters());
        assertEquals(5, template.getParameters().get("limit"));
    }

    /**
     * Verifies parameter addition when the underlying parameters map is initially null.
     */
    @Test
    public void shouldAddParameterWhenNoExistingMap() {
        TemplateDefinition template = mock(TemplateDefinition.class);
        stubParameter("foo", 123).of(template);
        Map<String, Object> params = template.getParameters();
        assertNotNull(params);
        assertEquals(123, params.get("foo"));
    }

    /**
     * Verifies parameter addition when a non-null parameters map already exists.
     */
    @Test
    public void shouldAddParameterToExistingMap() {
        TemplateDefinition template = mock(TemplateDefinition.class);
        Map<String, Object> existing = new HashMap<>();
        existing.put("alpha", "a");
        doReturn(existing).when(template).getParameters();

        stubParameter("beta", "b").of(template);

        Map<String, Object> params = template.getParameters();
        assertSame(existing, params);
        assertEquals("a", params.get("alpha"));
        assertEquals("b", params.get("beta"));
    }

    /**
     * Verifies parameter removal when value is null and entry exists.
     */
    @Test
    public void shouldRemoveParameterWhenValueNull() {
        TemplateDefinition template = mock(TemplateDefinition.class);
        Map<String, Object> existing = new HashMap<>();
        existing.put("removeMe", 1);
        existing.put("keepMe", 2);
        doReturn(existing).when(template).getParameters();

        stubParameter("removeMe", null).of(template);

        Map<String, Object> params = template.getParameters();
        assertFalse(params.containsKey("removeMe"));
        assertEquals(2, params.get("keepMe"));
    }

    /**
     * Verifies no-op when name is null or empty (no interaction with parameters map).
     */
    @Test
    public void shouldDoNothingWhenNameEmptyOrNull() {
        TemplateDefinition templateEmpty = mock(TemplateDefinition.class);
        stubParameter("", "value").of(templateEmpty);
        verify(templateEmpty, never()).getParameters();

        TemplateDefinition templateNull = mock(TemplateDefinition.class);
        stubParameter(null, "value").of(templateNull);
        verify(templateNull, never()).getParameters();
    }

    /**
     * Verifies that passing a null template leads to an IllegalArgumentException (hamcrest assertion inside operation).
     */
    @Test
    public void shouldFailOnNullTemplate() {
        assertThrows(IllegalArgumentException.class, () -> stubId("some:id").of(null));
    }
}
