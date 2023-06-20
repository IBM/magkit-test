package com.aperto.magkit.mockito;

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

import info.magnolia.rendering.template.TemplateDefinition;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubDeletable;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubDescription;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubDialog;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubEditable;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubMoveable;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubName;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubParameter;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubRenderType;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubSubtype;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubTemplateScript;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubTitle;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubType;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubVisible;
import static com.aperto.magkit.mockito.TemplateDefinitionStubbingOperation.stubWritable;
import static com.aperto.magkit.mockito.TemplateMockUtils.cleanTemplateManager;
import static com.aperto.magkit.mockito.TemplateMockUtils.mockTemplateDefinition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing TemplateDefinitionStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 27.05.13
 */
public class TemplateDefinitionStubbingOperationTest {

    private TemplateDefinition _template;

    @Before
    public void setUp() throws RepositoryException {
        cleanTemplateManager();
        _template = mockTemplateDefinition("test");
    }

    @Test
    public void testDeletable() throws Exception {
        assertThat(_template.getDeletable(), is(false));
        stubDeletable(true).of(_template);
        assertThat(_template.getDeletable(), is(true));
    }

    @Test
    public void testMoveable() throws Exception {
        assertThat(_template.getMoveable(), is(false));
        stubMoveable(true).of(_template);
        assertThat(_template.getMoveable(), is(true));
    }

    @Test
    public void testVisible() throws Exception {
        assertThat(_template.getVisible(), is(false));
        stubVisible(true).of(_template);
        assertThat(_template.getVisible(), is(true));
    }

    @Test
    public void testEditable() throws Exception {
        assertThat(_template.getEditable(), is(false));
        stubEditable(true).of(_template);
        assertThat(_template.getEditable(), is(true));
    }

    @Test
    public void testWritable() throws Exception {
        assertThat(_template.getWritable(), is(false));
        stubWritable(true).of(_template);
        assertThat(_template.getWritable(), is(true));
    }

    @Test
    public void testDialog() throws Exception {
        assertThat(_template.getDialog(), nullValue());
        stubDialog("testDialog").of(_template);
        assertThat(_template.getDialog(), is("testDialog"));
    }

    @Test
    public void testDescription() throws Exception {
        assertThat(_template.getDescription(), nullValue());
        stubDescription("test").of(_template);
        assertThat(_template.getDescription(), is("test"));
    }

    @Test
    public void testTemplateScript() throws Exception {
        assertThat(_template.getTemplateScript(), nullValue());
        stubTemplateScript("test").of(_template);
        assertThat(_template.getTemplateScript(), is("test"));
    }

    @Test
    public void testRenderType() throws Exception {
        assertThat(_template.getRenderType(), nullValue());
        stubRenderType("test").of(_template);
        assertThat(_template.getRenderType(), is("test"));
    }

    @Test
    public void testStubName() throws Exception {
        assertThat(_template.getId(), is("test"));
        assertThat(_template.getName(), nullValue());
        stubName("my name").of(_template);
        assertThat(_template.getName(), is("my name"));
    }

    @Test
    public void testStubTitle() throws Exception {
        assertThat(_template.getTitle(), nullValue());
        stubTitle("my title").of(_template);
        assertThat(_template.getTitle(), is("my title"));
    }

    @Test
    public void testStubType() throws Exception {
        assertThat(_template.getType(), nullValue());
        stubType("OK").of(_template);
        assertThat(_template.getType(), is("OK"));
    }

    @Test
    public void testStubSubType() throws Exception {
        assertThat(_template.getSubtype(), nullValue());
        stubSubtype("OK").of(_template);
        assertThat(_template.getSubtype(), is("OK"));
    }

    @Test
    public void stubParameterTest() throws RepositoryException {
        assertThat(_template.getParameters().get("name"), nullValue());
        assertThat(_template.getParameters(), notNullValue());
        assertThat(_template.getParameters().size(), is(0));

        stubParameter("name", "value").of(_template);
        assertThat(_template.getParameters(), notNullValue());
        assertThat(_template.getParameters().size(), is(1));
        assertThat(_template.getParameters().get("name").toString(), is("value"));

        stubParameter("name", null).of(_template);
        assertThat(_template.getParameters(), notNullValue());
        assertThat(_template.getParameters().size(), is(0));
        assertThat(_template.getParameters().get("name"), nullValue());
    }
}
