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

import de.ibmix.magkit.assertations.Require;
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

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A factory class to create StubbingOperations that define the behaviour of info.magnolia.module.model.ModuleDefinition mocks.
 * To be used standalone or as parameter of ModuleMockUtils.mockModuleDefinition(...).
 * <p>
 * Each static method returns a single-responsibility {@link ModuleDefinitionStubbingOperation} that configures one facet of a
 * {@link ModuleDefinition} Mockito mock. By composing multiple operations you can build rich test fixtures without exposing
 * mocking details throughout your test code.
 * </p>
 * <h3>Usage Example</h3>
 * <pre>{@code
 * ModuleDefinition module = ModuleMockUtils.mockModuleDefinition(
 *     "search",
 *     ModuleDefinitionStubbingOperation.stubDisplayName("Search Module"),
 *     ModuleDefinitionStubbingOperation.stubVersion("1.2.0"),
 *     ModuleDefinitionStubbingOperation.stubDescription("Adds search capabilities"),
 *     ModuleDefinitionStubbingOperation.stubDependency("core", "[1.0,2.0)", false)
 * );
 * }</pre>
 * <h3>Thread Safety</h3>
 * Returned operations are stateless and may be reused; applying them concurrently to the same mock requires external synchronization.
 * <h3>Null Handling</h3>
 * All parameters must be non-null unless explicitly documented; assertions surface invalid test setup early.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2020-03-04
 */
public abstract class ModuleDefinitionStubbingOperation implements StubbingOperation<ModuleDefinition> {

    /**
     * Stubs the display name returned by {@link ModuleDefinition#getDisplayName()}.
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinitionStubbingOperation op = ModuleDefinitionStubbingOperation.stubDisplayName("Demo Module");
     * }</pre>
     * @param displayName human readable module display name
     * @return operation configuring display name
     */
    public static ModuleDefinitionStubbingOperation stubDisplayName(final String displayName) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                when(moduleDefinition.getDisplayName()).thenReturn(displayName);
            }
        };
    }

    /**
     * Stubs the technical name returned by {@link ModuleDefinition#getName()}.
     * @param name unique module identifier
     * @return operation configuring name
     */
    public static ModuleDefinitionStubbingOperation stubName(final String name) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                when(moduleDefinition.getName()).thenReturn(name);
            }
        };
    }

    /**
     * Stubs the description returned by {@link ModuleDefinition#getDescription()}.
     * @param value textual description of module purpose
     * @return operation configuring description
     */
    public static ModuleDefinitionStubbingOperation stubDescription(final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                when(moduleDefinition.getDescription()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the implementation class name returned by {@link ModuleDefinition#getClassName()}.
     * @param value fully qualified class name implementing module
     * @return operation configuring class name
     */
    public static ModuleDefinitionStubbingOperation stubClassName(final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                when(moduleDefinition.getClassName()).thenReturn(value);
            }
        };
    }

    /**
     * Stubs the version returned by {@link ModuleDefinition#getVersion()} using {@link Version#parseVersion(String)}.
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinitionStubbingOperation.stubVersion("1.0.0");
     * }</pre>
     * @param version semantic version string (e.g. "1.0.0")
     * @return operation configuring version
     */
    public static ModuleDefinitionStubbingOperation stubVersion(final String version) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                Version v = Version.parseVersion(version);
                when(moduleDefinition.getVersion()).thenReturn(v);
            }
        };
    }

    /**
     * Adds a servlet definition to the module's servlet collection returned by {@link ModuleDefinition#getServlets()}.
     * Existing servlets are preserved; the provided one is appended.
     * @param value servlet definition to add
     * @return operation adding servlet definition
     */
    public static ModuleDefinitionStubbingOperation stubServlet(final ServletDefinition value) {
        Require.Argument.notNull(value, "value should not be null");
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                Collection<ServletDefinition> servlets = moduleDefinition.getServlets();
                servlets.add(value);
                doReturn(servlets).when(moduleDefinition).getServlets();
            }
        };
    }

    /**
     * Replaces servlet definitions with the provided collection for {@link ModuleDefinition#getServlets()}.
     * @param values collection of servlet definitions (non-null)
     * @return operation configuring servlet definitions
     */
    public static ModuleDefinitionStubbingOperation stubServlets(final Collection<ServletDefinition> values) {
        Require.Argument.notNull(values, "values should not be null");
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                doReturn(values).when(moduleDefinition).getServlets();
            }
        };
    }

    /**
     * Adds a dependency definition to the module's dependencies returned by {@link ModuleDefinition#getDependencies()}.
     * The dependency specifies name, version string and optional flag; a {@link VersionRange} parsed from version is also stubbed.
     * <h4>Example</h4>
     * <pre>{@code
     * ModuleDefinitionStubbingOperation.stubDependency("core", "[1.0,2.0)", false);
     * }</pre>
     * @param name dependency module name
     * @param version version or range specification
     * @param optional whether dependency is optional
     * @return operation adding dependency definition
     */
    public static ModuleDefinitionStubbingOperation stubDependency(final String name, final String version, boolean optional) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "groupManager should not be null");
                DependencyDefinition result = mock(DependencyDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(version).when(result).getVersion();
                doReturn(optional).when(result).isOptional();
                doReturn(VersionRange.parse(version)).when(result).getVersionRange();
                Collection<DependencyDefinition> dependencies = moduleDefinition.getDependencies();
                dependencies.add(result);
                doReturn(dependencies).when(moduleDefinition).getDependencies();
            }
        };
    }

    /**
     * Adds a repository definition to the module's repositories returned by {@link ModuleDefinition#getRepositories()}.
     * Workspaces are provided as varargs and converted to a list.
     * @param name repository name
     * @param nodeTypeFile path or resource name of node type definitions
     * @param workspaces one or more workspace names managed by the repository
     * @return operation adding repository definition
     */
    public static ModuleDefinitionStubbingOperation stubRepository(final String name, final String nodeTypeFile, final String... workspaces) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                RepositoryDefinition result = mock(RepositoryDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(nodeTypeFile).when(result).getNodeTypeFile();
                doReturn(Arrays.asList(workspaces)).when(result).getWorkspaces();
                Collection<RepositoryDefinition> repositories = moduleDefinition.getRepositories();
                repositories.add(result);
                doReturn(repositories).when(moduleDefinition).getRepositories();
            }
        };
    }

    /**
     * Adds a property definition to the module's properties returned by {@link ModuleDefinition#getProperties()} and stubs direct property lookup.
     * @param name property name
     * @param value property value
     * @return operation adding property definition and direct access
     */
    public static ModuleDefinitionStubbingOperation stubProperty(final String name, final String value) {
        return new ModuleDefinitionStubbingOperation() {
            @Override
            public void of(ModuleDefinition moduleDefinition) {
                Require.Argument.notNull(moduleDefinition, "moduleDefinition should not be null");
                PropertyDefinition result = mock(PropertyDefinition.class);
                doReturn(name).when(result).getName();
                doReturn(value).when(result).getValue();
                Collection<PropertyDefinition> properties = moduleDefinition.getProperties();
                properties.add(result);
                doReturn(properties).when(moduleDefinition).getProperties();
                doReturn(value).when(moduleDefinition).getProperty(name);
            }
        };
    }
}
