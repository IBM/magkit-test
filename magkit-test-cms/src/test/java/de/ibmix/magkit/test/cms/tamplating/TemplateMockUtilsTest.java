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

import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.tamplating.TemplateMockUtils.cleanTemplateManager;
import static de.ibmix.magkit.test.cms.tamplating.TemplateMockUtils.mockTemplate;
import static de.ibmix.magkit.test.cms.tamplating.TemplateMockUtils.mockTemplateDefinitionRegistry;
import static de.ibmix.magkit.test.cms.tamplating.TemplateMockUtils.register;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 08.04.11
 */
public class TemplateMockUtilsTest {

    @Before
    public void setUp() throws Exception {
        cleanContext();
        mockTemplateDefinitionRegistry();
    }

    @Test
    public void testMockTemplateManager() {
        TemplateDefinitionRegistry mock = mockTemplateDefinitionRegistry();
        assertThat(mock, notNullValue());
        assertThat(mock.getClass().getName().contains("EnhancerByMockito"), is(true));

        TemplateDefinitionRegistry mock2 = mockTemplateDefinitionRegistry();
        assertThat(mock, is(mock2));
    }

    @Test
    public void testMockSTKPage() throws Exception {
        TemplateDefinitionStubbingOperation op1 = mock(TemplateDefinitionStubbingOperation.class);
        ConfiguredTemplateDefinition t = mockTemplate("key", op1);
        assertThat(t, notNullValue());
        assertThat((ConfiguredTemplateDefinition) mockTemplateDefinitionRegistry().getTemplateDefinition("key"), is(t));
        verify(op1, Mockito.times(1)).of(t);

        // Repeated stubbing of templates with same key should NOT create a new mock but return the existing instance.
        TemplateDefinitionStubbingOperation op2 = mock(TemplateDefinitionStubbingOperation.class);
        ConfiguredTemplateDefinition t2 = mockTemplate("key", op2);
        verify(op2, Mockito.times(1)).of(t);
        assertThat((ConfiguredTemplateDefinition) mockTemplateDefinitionRegistry().getTemplateDefinition("key"), is(t2));
    }

    @Test
    public void testCleanTemplateManager() {
        // TemplateManager.getInstance() always returns an ordinary instance of TemplateManager.
        TemplateDefinitionRegistry tm = mockTemplateDefinitionRegistry();
        TemplateDefinitionRegistry tm2 = mockTemplateDefinitionRegistry();
        assertThat(tm, notNullValue());
        assertThat(tm2, notNullValue());
        assertThat(tm, is(tm2));

        cleanTemplateManager();

        // now we should get a new instance of TemplateManager
        TemplateDefinitionRegistry tm3 = mockTemplateDefinitionRegistry();
        assertThat(tm3, notNullValue());
        assertThat(tm3, not(is(tm)));
        assertThat(tm3, not(is(tm2)));
    }

    @Test
    public void testRegister() throws RegistrationException {
        ConfiguredTemplateDefinition t1 = mock(ConfiguredTemplateDefinition.class);
        register("t1", t1);
        TemplateDefinitionRegistry tm = mockTemplateDefinitionRegistry();
        assertThat(tm, notNullValue());
        assertThat((ConfiguredTemplateDefinition) tm.getTemplateDefinition("t1"), is(t1));
        List availableTemplates = (List) tm.getTemplateDefinitions();
        assertThat(availableTemplates, notNullValue());
        assertThat(availableTemplates.size(), is(1));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(0), is(t1));

        ConfiguredTemplateDefinition t2 = mock(ConfiguredTemplateDefinition.class);
        register("t2", t2);
        availableTemplates = (List) tm.getTemplateDefinitions();
        assertThat(availableTemplates.size(), is(2));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(0), is(t1));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(1), is(t2));
    }
}
