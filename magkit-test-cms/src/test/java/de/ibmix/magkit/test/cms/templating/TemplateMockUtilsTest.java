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

import info.magnolia.registry.RegistrationException;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.cleanTemplateManager;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.mockAreaDefinition;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.mockConfiguredTemplateDefinition;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.mockTemplateDefinition;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.mockTemplateDefinitionRegistry;
import static de.ibmix.magkit.test.cms.templating.TemplateMockUtils.register;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.util.MockUtil.isMock;

/**
 * Testing TemplateMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-04-08
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
        assertTrue(isMock(mock));

        TemplateDefinitionRegistry mock2 = mockTemplateDefinitionRegistry();
        assertThat(mock, is(mock2));
    }

    @Test
    public void testMockSTKPage() throws Exception {
        TemplateDefinitionStubbingOperation op1 = mock(TemplateDefinitionStubbingOperation.class);
        ConfiguredTemplateDefinition t = mockConfiguredTemplateDefinition("key", op1);
        assertThat(t, notNullValue());
        assertThat((ConfiguredTemplateDefinition) mockTemplateDefinitionRegistry().getTemplateDefinition("key"), is(t));
        verify(op1, Mockito.times(1)).of(t);

        // Repeated stubbing of templates with same key should NOT create a new mock but return the existing instance.
        TemplateDefinitionStubbingOperation op2 = mock(TemplateDefinitionStubbingOperation.class);
        ConfiguredTemplateDefinition t2 = mockConfiguredTemplateDefinition("key", op2);
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
        List<TemplateDefinition> availableTemplates = (List<TemplateDefinition>) tm.getTemplateDefinitions();
        assertThat(availableTemplates, notNullValue());
        assertThat(availableTemplates.size(), is(1));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(0), is(t1));

        ConfiguredTemplateDefinition t2 = mock(ConfiguredTemplateDefinition.class);
        register("t2", t2);
        availableTemplates = (List<TemplateDefinition>) tm.getTemplateDefinitions();
        assertThat(availableTemplates.size(), is(2));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(0), is(t1));
        assertThat((ConfiguredTemplateDefinition) availableTemplates.get(1), is(t2));
    }

    @Test
    public void testRegisterNoId() throws RegistrationException {
        TemplateDefinition td = mock(TemplateDefinition.class);
        register(null, td);
        TemplateDefinitionRegistry tdr = mockTemplateDefinitionRegistry();
        assertThat(tdr.getAllDefinitions().isEmpty(), is(true));
        assertThat(tdr.getProvider((String) null), nullValue());
        assertThat(tdr.getTemplateDefinition(null), nullValue());

        register("", td);
        assertThat(tdr.getAllDefinitions().isEmpty(), is(true));
        assertThat(tdr.getProvider(""), nullValue());
        assertThat(tdr.getTemplateDefinition(""), nullValue());

        register(" ", td);
        assertThat(tdr.getAllDefinitions().isEmpty(), is(true));
        assertThat(tdr.getProvider(" "), nullValue());
        assertThat(tdr.getTemplateDefinition(" "), nullValue());
    }

    @Test
    public void testMockTemplateDefinition() throws RegistrationException {
        TemplateDefinitionStubbingOperation op1 = mock(TemplateDefinitionStubbingOperation.class);
        TemplateDefinition td = mockTemplateDefinition("test", op1);
        assertThat(td.getId(), is("test"));
        verify(op1, Mockito.times(1)).of(td);
        assertThat(mockTemplateDefinitionRegistry().getTemplateDefinition("test"), is(td));
        assertThat(mockTemplateDefinitionRegistry().getProvider("test").get(), is(td));
        assertThat(mockTemplateDefinitionRegistry().getAllDefinitions().size(), is(1));
        assertThat(mockTemplateDefinitionRegistry().getAllDefinitions().contains(td), is(true));

        TemplateDefinition td2 = mockTemplateDefinition("test");
        assertThat(td2, is(td));
    }

    @Test
    public void testMockAreaDefinition() throws RegistrationException {
        AreaDefinitionStubbingOperation op1 = mock(AreaDefinitionStubbingOperation.class);
        AreaDefinition td = mockAreaDefinition("test", op1);
        assertThat(td.getId(), is("test"));
        verify(op1, Mockito.times(1)).of(td);
        assertThat(mockTemplateDefinitionRegistry().getTemplateDefinition("test"), is(td));
        assertThat(mockTemplateDefinitionRegistry().getProvider("test").get(), is(td));
        assertThat(mockTemplateDefinitionRegistry().getAllDefinitions().size(), is(1));
        assertThat(mockTemplateDefinitionRegistry().getAllDefinitions().contains(td), is(true));

        TemplateDefinition td2 = mockAreaDefinition("test");
        assertThat(td2, is(td));
    }
}
