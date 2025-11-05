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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Test ModuleDefinitionStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public class ModuleDefinitionStubbingOperationTest {

    private ModuleDefinition _moduleDefinition;

    @BeforeEach
    public void setUp() throws Exception {
        _moduleDefinition = mock(ModuleDefinition.class);
    }

    @Test
    public void testDisplayName() {
        assertNull(_moduleDefinition.getDisplayName());

        stubDisplayName("DISPLAY_NAME").of(_moduleDefinition);
        assertEquals("DISPLAY_NAME", _moduleDefinition.getDisplayName());
    }

    @Test
    public void testName() {
        assertNull(_moduleDefinition.getName());

        stubName("NAME").of(_moduleDefinition);
        assertEquals("NAME", _moduleDefinition.getName());
    }

    @Test
    public void testVersion() {
        assertNull(_moduleDefinition.getVersion());

        Version v = Version.parseVersion("1.1.3");
        stubVersion("1.1.3").of(_moduleDefinition);
        assertEquals(v, _moduleDefinition.getVersion());
    }

    @Test
    public void testDescription() {
        assertNull(_moduleDefinition.getDescription());

        stubDescription("test").of(_moduleDefinition);
        assertEquals("test", _moduleDefinition.getDescription());
    }

    @Test
    public void testStubClassName() {
        assertNull(_moduleDefinition.getClassName());

        stubClassName("test").of(_moduleDefinition);
        assertEquals("test", _moduleDefinition.getClassName());
    }

    @Test
    public void testStubServlet() {
        assertTrue(_moduleDefinition.getServlets().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubServlet(sd).of(_moduleDefinition);
        assertEquals(1, _moduleDefinition.getServlets().size());
        assertTrue(_moduleDefinition.getServlets().contains(sd));

        ServletDefinition sd2 = mockServletDefinition("two");
        stubServlet(sd2).of(_moduleDefinition);
        assertEquals(2, _moduleDefinition.getServlets().size());
        assertTrue(_moduleDefinition.getServlets().contains(sd));
        assertTrue(_moduleDefinition.getServlets().contains(sd2));
    }

    @Test
    public void testStubServlets() {
        assertTrue(_moduleDefinition.getServlets().isEmpty());

        ServletDefinition sd = mockServletDefinition("one");
        stubServlet(sd).of(_moduleDefinition);
        assertEquals(1, _moduleDefinition.getServlets().size());
        assertTrue(_moduleDefinition.getServlets().contains(sd));

        ServletDefinition sd2 = mockServletDefinition("two");
        stubServlets(Collections.singletonList(sd2)).of(_moduleDefinition);
        assertEquals(1, _moduleDefinition.getServlets().size());
        assertTrue(_moduleDefinition.getServlets().contains(sd2));
    }

    @Test
    public void testStubDependency() {
        assertTrue(_moduleDefinition.getDependencies().isEmpty());

        stubDependency("dep1", "1.0", true).of(_moduleDefinition);
        List<DependencyDefinition> dependencies = (List<DependencyDefinition>) _moduleDefinition.getDependencies();
        assertEquals(1, dependencies.size());
        assertEquals("dep1", dependencies.get(0).getName());
        assertEquals("1.0", dependencies.get(0).getVersion());
        assertEquals("[1.0.0,1.0.0]", dependencies.get(0).getVersionRange().toString());
        assertTrue(dependencies.get(0).isOptional());

        stubDependency("dep2", "1.1", false).of(_moduleDefinition);
        assertEquals(2, _moduleDefinition.getDependencies().size());
    }

    @Test
    public void testStubRepository() {
        assertTrue(_moduleDefinition.getRepositories().isEmpty());

        stubRepository("rep1", "nodetypes/my-nodetype.yaml", "ws-1", "ws-2").of(_moduleDefinition);
        List<RepositoryDefinition> repositories = (List<RepositoryDefinition>) _moduleDefinition.getRepositories();
        assertEquals(1, repositories.size());
        assertEquals("rep1", repositories.get(0).getName());
        assertEquals("nodetypes/my-nodetype.yaml", repositories.get(0).getNodeTypeFile());
        assertEquals(2, repositories.get(0).getWorkspaces().size());

        stubRepository("rep1", null, "ws-3").of(_moduleDefinition);
        assertEquals(2, _moduleDefinition.getRepositories().size());
    }

    @Test
    public void testStubProperty() {
        assertTrue(_moduleDefinition.getProperties().isEmpty());

        stubProperty("prop1", "value1").of(_moduleDefinition);
        List<PropertyDefinition> properties = (List<PropertyDefinition>) _moduleDefinition.getProperties();
        assertEquals(1, properties.size());
        assertEquals("prop1", properties.get(0).getName());
        assertEquals("value1", properties.get(0).getValue());
        assertEquals("value1", _moduleDefinition.getProperty("prop1"));

        stubProperty("prop2", "value2").of(_moduleDefinition);
        properties = (List<PropertyDefinition>) _moduleDefinition.getProperties();
        assertEquals(2, properties.size());
        assertEquals("value1", _moduleDefinition.getProperty("prop1"));
        assertEquals("value2", _moduleDefinition.getProperty("prop2"));
    }
}
