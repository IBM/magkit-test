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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.module.model.DependencyDefinition;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.PropertyDefinition;
import info.magnolia.module.model.RepositoryDefinition;
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.module.model.VersionRange;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A factory class to create StubbingOperations that define the behaviour of info.magnolia.module.model.ModuleDefinition mocks.
 * To be used standalone or as parameter of ModuleMockUtils.mockModuleDefinition(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public abstract class ModuleDefinitionStubbingOperation implements StubbingOperation<ModuleDefinition> {

    public static ModuleDefinitionStubbingOperation stubDisplayName(final String displayName) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition md) {
                assertThat(md, notNullValue());
                when(md.getDisplayName()).thenReturn(displayName);
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubName(final String name) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition md) {
                assertThat(md, notNullValue());
                when(md.getName()).thenReturn(name);
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubDescription(final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition md) {
                assertThat(md, notNullValue());
                when(md.getDescription()).thenReturn(value);
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubClassName(final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition md) {
                assertThat(md, notNullValue());
                when(md.getClassName()).thenReturn(value);
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubVersion(final String version) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition md) {
                assertThat(md, notNullValue());
                Version v = Version.parseVersion(version);
                when(md.getVersion()).thenReturn(v);
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubServlet(final ServletDefinition value) {
        assertThat(value, notNullValue());
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition mock) {
                assertThat(mock, notNullValue());
                Collection<ServletDefinition> servlets = mock.getServlets();
                servlets.add(value);
                doReturn(servlets).when(mock).getServlets();
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubServlets(final Collection<ServletDefinition> values) {
        assertThat(values, notNullValue());
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(values).when(mock).getServlets();
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubDependency(final String name, final String version, boolean optional) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition mock) {
                assertThat(mock, notNullValue());
                DependencyDefinition result = mock(DependencyDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(version).when(result).getVersion();
                doReturn(optional).when(result).isOptional();
                doReturn(VersionRange.parse(version)).when(result).getVersionRange();
                Collection<DependencyDefinition> dependencies = mock.getDependencies();
                dependencies.add(result);
                doReturn(dependencies).when(mock).getDependencies();
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubRepository(final String name, final String nodeTypeFile, final String... workspaces) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition mock) {
                assertThat(mock, notNullValue());
                RepositoryDefinition result = mock(RepositoryDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(nodeTypeFile).when(result).getNodeTypeFile();
                doReturn(Arrays.asList(workspaces)).when(result).getWorkspaces();
                Collection<RepositoryDefinition> repositories = mock.getRepositories();
                repositories.add(result);
                doReturn(repositories).when(mock).getRepositories();
            }
        };
    }

    public static ModuleDefinitionStubbingOperation stubProperty(final String name, final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition mock) {
                assertThat(mock, notNullValue());
                PropertyDefinition result = mock(PropertyDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(value).when(result).getValue();
                Collection<PropertyDefinition> properties = mock.getProperties();
                properties.add(result);
                doReturn(properties).when(mock).getProperties();
                doReturn(value).when(mock).getProperty(name);
            }
        };
    }
}
