package com.aperto.magkit.freemarker;

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

import info.magnolia.rendering.template.AutoGenerationConfiguration;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.fragment.FragmentDefinition;

import java.util.Map;

/**
 * Renderable definition for Freemarker unit tests.
 *
 * @author lars.gendner
 */
public class FreemarkerTestRenderableDefinition implements RenderableDefinition {

    private String _id;

    private final String _templateScript;

    public FreemarkerTestRenderableDefinition(String templateScript) {
        _templateScript = templateScript;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public void setId(String id) {
        _id = id;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getRenderType() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getI18nBasename() {
        return null;
    }

    @Override
    public String getTemplateScript() {
        return _templateScript;
    }

    @Override
    public Map<String, RenderableDefinition> getVariations() {
        return null;
    }

    @Override
    public Map<String, Object> getParameters() {
        return null;
    }

    @Override
    public Class<?> getModelClass() {
        return null;
    }

    @Override
    public AutoGenerationConfiguration getAutoGeneration() {
        return null;
    }

    @Override
    public Boolean getAutoPopulateFromRequest() {
        return false;
    }

    @Override
    public FragmentDefinition getFragmentDefinition() {
        return null;
    }
}
