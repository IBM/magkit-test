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
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static de.ibmix.magkit.test.cms.templating.TemplateDefinitionStubbingOperation.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
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

        assertThat(template.getDeletable(), is(true));
        assertThat(template.getDialog(), is("dialog-id"));
        assertThat(template.getEditable(), is(false));
        assertThat(template.getMoveable(), is(true));
        assertThat(template.getVisible(), is(false));
        assertThat(template.getWritable(), is(true));
        assertThat(template.getType(), is("page"));
        assertThat(template.getSubtype(), is("home"));
        assertThat(template.getI18nBasename(), is("i18n.basename"));
        assertThat(template.getDescription(), is("Some description"));
        assertThat(template.getId(), is("module:pages/home"));
        assertThat(template.getName(), is("home"));
        assertThat(template.getTitle(), is("Home Page"));
        assertThat(template.getTemplateScript(), is("/templates/pages/home.ftl"));
        assertThat(template.getRenderType(), is("freemarker"));
        assertThat(template.getParameters(), allOf(notNullValue(), hasEntry("limit", 5)));
    }

    /**
     * Verifies parameter addition when the underlying parameters map is initially null.
     */
    @Test
    public void shouldAddParameterWhenNoExistingMap() {
        TemplateDefinition template = mock(TemplateDefinition.class);
        stubParameter("foo", 123).of(template);
        Map<String, Object> params = template.getParameters();
        assertThat(params, notNullValue());
        assertThat(params.get("foo"), is((Object) 123));
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
        assertThat(params, sameInstance(existing));
        assertThat(params, allOf(hasEntry("alpha", (Object) "a"), hasEntry("beta", (Object) "b")));
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
        assertThat(params.containsKey("removeMe"), is(false));
        assertThat(params.get("keepMe"), is((Object) 2));
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
     * Verifies that passing a null template leads to an AssertionError (hamcrest assertion inside operation).
     */
    @Test(expected = AssertionError.class)
    public void shouldFailOnNullTemplate() {
        stubId("some:id").of(null);
    }
}
