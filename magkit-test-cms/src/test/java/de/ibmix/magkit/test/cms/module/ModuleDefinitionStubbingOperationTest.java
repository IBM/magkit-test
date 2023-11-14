package de.ibmix.magkit.test.cms.module;

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

import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.PropertyDefinition;
import info.magnolia.module.model.RepositoryDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.Version;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubClassName;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubDependency;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubRepository;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubServlet;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubServlets;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubDisplayName;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.test.cms.module.ModuleDefinitionStubbingOperation.stubVersion;
import static de.ibmix.magkit.test.cms.module.ModuleMockUtils.mockServletDefinition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test ModuleDefinitionStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public class ModuleDefinitionStubbingOperationTest {

    private ModuleDefinition _moduleDefinition;

    @Before
    public void setUp() throws Exception {
        _moduleDefinition = mock(ModuleDefinition.class);
    }

    @Test
    public void testDisplayName() {
        assertThat(_moduleDefinition.getDisplayName(), nullValue());

        stubDisplayName("DISPLAY_NAME").of(_moduleDefinition);
        assertThat(_moduleDefinition.getDisplayName(), is("DISPLAY_NAME"));
    }

    @Test
    public void testName() {
        assertThat(_moduleDefinition.getName(), nullValue());

        stubName("NAME").of(_moduleDefinition);
        assertThat(_moduleDefinition.getName(), is("NAME"));
    }

    @Test
    public void testVersion() {
        assertThat(_moduleDefinition.getVersion(), nullValue());

        Version v = Version.parseVersion("1.1.3");
        stubVersion("1.1.3").of(_moduleDefinition);
        assertThat(_moduleDefinition.getVersion(), is(v));
    }

    @Test
    public void testDescription() {
        assertThat(_moduleDefinition.getDescription(), nullValue());

        stubDescription("test").of(_moduleDefinition);
        assertThat(_moduleDefinition.getDescription(), is("test"));
    }

    @Test
    public void testStubClassName() {
        assertThat(_moduleDefinition.getClassName(), nullValue());

        stubClassName("test").of(_moduleDefinition);
        assertThat(_moduleDefinition.getClassName(), is("test"));
    }

    @Test
    public void testStubServlet() {
        assertTrue(_moduleDefinition.getServlets().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubServlet(sd).of(_moduleDefinition);
        assertThat(_moduleDefinition.getServlets().size(), is(1));
        assertTrue(_moduleDefinition.getServlets().contains(sd));

        ServletDefinition sd2 = mockServletDefinition("two");
        stubServlet(sd2).of(_moduleDefinition);
        assertThat(_moduleDefinition.getServlets().size(), is(2));
        assertTrue(_moduleDefinition.getServlets().contains(sd));
        assertTrue(_moduleDefinition.getServlets().contains(sd2));
    }

    @Test
    public void testStubServlets() {
        assertTrue(_moduleDefinition.getServlets().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubServlet(sd).of(_moduleDefinition);
        assertThat(_moduleDefinition.getServlets().size(), is(1));
        assertTrue(_moduleDefinition.getServlets().contains(sd));

        ServletDefinition sd2 = mockServletDefinition("two");
        stubServlets(Arrays.asList(sd2)).of(_moduleDefinition);
        assertThat(_moduleDefinition.getServlets().size(), is(1));
        assertTrue(_moduleDefinition.getServlets().contains(sd2));
    }

    @Test
    public void testStubDependency() {
        assertTrue(_moduleDefinition.getDependencies().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubDependency("dep1", "1.0", true).of(_moduleDefinition);
        List<DependencyDefinition> dependencies = (List<DependencyDefinition>) _moduleDefinition.getDependencies();
        assertThat(dependencies.size(), is(1));
        assertThat(dependencies.get(0).getName(), is("dep1"));
        assertThat(dependencies.get(0).getVersion(), is("1.0"));
        assertThat(dependencies.get(0).getVersionRange().toString(), is("[1.0.0,1.0.0]"));
        assertThat(dependencies.get(0).isOptional(), is(true));

        stubDependency("dep2", "1.1", false).of(_moduleDefinition);
        assertThat(_moduleDefinition.getDependencies().size(), is(2));
    }

    @Test
    public void testStubRepository() {
        assertTrue(_moduleDefinition.getRepositories().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubRepository("rep1", "nodetypes/my-nodetype.yaml", "ws-1", "ws-2").of(_moduleDefinition);
        List<RepositoryDefinition> repositories = (List<RepositoryDefinition>) _moduleDefinition.getRepositories();
        assertThat(repositories.size(), is(1));
        assertThat(repositories.get(0).getName(), is("rep1"));
        assertThat(repositories.get(0).getNodeTypeFile(), is("nodetypes/my-nodetype.yaml"));
        assertThat(repositories.get(0).getWorkspaces().size(), is(2));

        stubRepository("rep1", null, "ws-3").of(_moduleDefinition);
        assertThat(_moduleDefinition.getRepositories().size(), is(2));
    }

    @Test
    public void testStubProperty() {
        assertTrue(_moduleDefinition.getProperties().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubProperty("prop1", "value1").of(_moduleDefinition);
        List<PropertyDefinition> properties = (List<PropertyDefinition>) _moduleDefinition.getProperties();
        assertThat(properties.size(), is(1));
        assertThat(properties.get(0).getName(), is("prop1"));
        assertThat(properties.get(0).getValue(), is("value1"));
        assertThat(_moduleDefinition.getProperty("prop1"), is("value1"));

        stubProperty("prop2", "value2").of(_moduleDefinition);
        properties = (List<PropertyDefinition>) _moduleDefinition.getProperties();
        assertThat(properties.size(), is(2));
        assertThat(_moduleDefinition.getProperty("prop1"), is("value1"));
        assertThat(_moduleDefinition.getProperty("prop2"), is("value2"));
    }
}
