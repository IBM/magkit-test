package de.ibmix.magkit.test.cms.tamplating;

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
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;

import java.util.ArrayList;
import java.util.List;

import static de.ibmix.magkit.test.cms.tamplating.TemplateDefinitionStubbingOperation.stubId;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Collection of utility methods for mocking Mgnl TemplateDefinitions (e.g. STKPage) with Mockito.
 *
 * @author Wolf Bubenik, Aperto AG
 * @since 2010-08-27
 */
public final class TemplateMockUtils extends ComponentsMockUtils {

    /**
     * If a TemplateDefinitionRegistry has been mocked, the existing mock will be returned.
     * If not, a new mock instance will be created and registered at the magnolia ComponentProvider.
     *
     * @return a TemplateManager mock instance
     */
    public static TemplateDefinitionRegistry mockTemplateDefinitionRegistry() {
        TemplateDefinitionRegistry result = mockComponentInstance(TemplateDefinitionRegistry.class);
        if (result.getAllDefinitions() == null) {
            List<TemplateDefinition> allDefinitions = new ArrayList<>();
            when(result.getAllDefinitions()).thenReturn(allDefinitions);
            when(result.getTemplateDefinitions()).thenReturn(allDefinitions);
        }
        return result;
    }

    @Deprecated
    public static ConfiguredTemplateDefinition mockTemplate(String id, TemplateDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        ConfiguredTemplateDefinition result = null;
        try {
            TemplateDefinition existing = registry.getTemplateDefinition(id);
            if (existing instanceof ConfiguredTemplateDefinition) {
                result = (ConfiguredTemplateDefinition) existing;
            } else {
                result = mock(ConfiguredTemplateDefinition.class);
                stubId(id).of(result);
                register(id, result);
            }
            for (TemplateDefinitionStubbingOperation stubbing : stubbings) {
                stubbing.of(result);
            }
        } catch (RegistrationException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static TemplateDefinition mockTemplateDefinition(String id, TemplateDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        TemplateDefinition result = null;
        try {
            result = registry.getTemplateDefinition(id);
            if (result == null) {
                result = mock(TemplateDefinition.class);
                stubId(id).of(result);
                register(id, result);
            }
            for (TemplateDefinitionStubbingOperation stubbing : stubbings) {
                stubbing.of(result);
            }
        } catch (RegistrationException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static TemplateDefinition mockAreaDefinition(String id, AreaDefinitionStubbingOperation... stubbings) {
        assertThat(stubbings, notNullValue());
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        TemplateDefinition result = null;
        try {
            result = registry.getTemplateDefinition(id);
            if (result == null) {
                result = mock(AreaDefinition.class);
                stubId(id).of(result);
                register(id, result);
            }
            for (AreaDefinitionStubbingOperation stubbing : stubbings) {
                stubbing.of(result);
            }
        } catch (RegistrationException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    /**
     * Stubs the TemplateManager mock returned by mockTemplateManager() to return the given Template instance
     * on each call of templateManager.getTemplateDefinition(templateKey).
     * TemplateManager.getAvailableTemplates() will be stubbed to return all previously registered Templates and the new one.
     *
     * @param id       the id of the template as String
     * @param template the Template to be registered at TemplateManager
     */
    public static void register(String id, TemplateDefinition template) {
        TemplateDefinitionRegistry registry = mockTemplateDefinitionRegistry();
        // register Template only if we have a key. SubTemplates do not have an owen key and should not be registered here. Is that really correct?
        if (isNotBlank(id)) {
            try {
                DefinitionProvider<TemplateDefinition> definitionProvider = mockDefinitionProvider(template);
                doReturn(definitionProvider).when(registry).getProvider(id);
                when(registry.getTemplateDefinition(id)).thenReturn(template);
                // update mocking of getAvailableTemplates():
                List<TemplateDefinition> newDefinitions = new ArrayList<TemplateDefinition>();
                newDefinitions.addAll(registry.getAllDefinitions());
                newDefinitions.add(template);
                when(registry.getTemplateDefinitions()).thenReturn(newDefinitions);
                when(registry.getAllDefinitions()).thenReturn(newDefinitions);
            } catch (RegistrationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public static <T> DefinitionProvider<T> mockDefinitionProvider(T definition) {
        DefinitionProvider<T> result = mock(DefinitionProvider.class);
        doReturn(definition).when(result).get();
        return result;
    }

    public static void cleanTemplateManager() {
        clearComponentProvider(TemplateDefinitionRegistry.class);
    }

    private TemplateMockUtils() {
    }
}

