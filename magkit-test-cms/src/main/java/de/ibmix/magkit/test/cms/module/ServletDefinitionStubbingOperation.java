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
import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.ServletParameterDefinition;

import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Factory class providing {@link ServletDefinitionStubbingOperation} instances to configure Mockito mocks of
 * {@link ServletDefinition}. Each static method focuses on one aspect (name, class name, comment, mappings, parameters),
 * enabling readable, composable test setup when passed to {@code ModuleMockUtils.mockServletDefinition(...)} or applied
 * directly to a mock.
 * <p>
 * Operations either append to existing collections (e.g. {@link #stubMapping(String)}, {@link #stubParameter(String, String)})
 * or replace them entirely (e.g. {@link #stubMappings(Collection)}, {@link #stubParams(Collection)}).
 * </p>
 * <h3>Usage Example</h3>
 * <pre>{@code
 * ServletDefinition servlet = ModuleMockUtils.mockServletDefinition(
 *     "assetServlet",
 *     ServletDefinitionStubbingOperation.stubClassName("de.example.AssetServlet"),
 *     ServletDefinitionStubbingOperation.stubMapping("/assets/*"),
 *     ServletDefinitionStubbingOperation.stubParameter("cache", "true")
 * );
 * }</pre>
 * <h3>Thread Safety</h3>
 * Returned operations are stateless; concurrent application to the same mock requires external synchronization of the test.
 * <h3>Null Handling</h3>
 * Parameters must be non-null unless explicitly documented; assertions surface invalid test configuration early.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-13
 */
public abstract class ServletDefinitionStubbingOperation implements StubbingOperation<ServletDefinition> {

    /**
     * Stubs the servlet name returned by {@link ServletDefinition#getName()}.
     * <h4>Example</h4>
     * <pre>{@code
     * ServletDefinitionStubbingOperation.stubName("myServlet");
     * }</pre>
     * @param name servlet name identifier
     * @return operation configuring servlet name
     */
    public static ServletDefinitionStubbingOperation stubName(final String name) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(name).when(mock).getName();
            }
        };
    }

    /**
     * Stubs the fully qualified servlet implementation class returned by {@link ServletDefinition#getClassName()}.
     * @param name class name implementing the servlet
     * @return operation configuring servlet class name
     */
    public static ServletDefinitionStubbingOperation stubClassName(final String name) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(name).when(mock).getClassName();
            }
        };
    }

    /**
     * Stubs the descriptive comment returned by {@link ServletDefinition#getComment()}.
     * @param value human readable comment
     * @return operation configuring comment
     */
    public static ServletDefinitionStubbingOperation stubComment(final String value) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(value).when(mock).getComment();
            }
        };
    }

    /**
     * Appends a single URL mapping to the servlet's mapping collection returned by {@link ServletDefinition#getMappings()}.
     * Existing mappings are preserved.
     * <h4>Example</h4>
     * <pre>{@code
     * ServletDefinitionStubbingOperation.stubMapping("/api/*");
     * }</pre>
     * @param value mapping pattern to add
     * @return operation adding the mapping
     */
    public static ServletDefinitionStubbingOperation stubMapping(final String value) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                Collection<String> mappings = mock.getMappings();
                mappings.add(value);
                doReturn(mappings).when(mock).getMappings();
            }
        };
    }

    /**
     * Replaces existing mappings with the provided collection for {@link ServletDefinition#getMappings()}.
     * @param mappings collection of mapping patterns (non-null)
     * @return operation configuring mappings
     */
    public static ServletDefinitionStubbingOperation stubMappings(final Collection<String> mappings) {
        assertThat(mappings, notNullValue());
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(mappings).when(mock).getMappings();
            }
        };
    }

    /**
     * Appends a single servlet init parameter definition to the collection returned by {@link ServletDefinition#getParams()}.
     * Existing parameters are preserved.
     * <h4>Example</h4>
     * <pre>{@code
     * ServletDefinitionStubbingOperation.stubParameter("cache", "true");
     * }</pre>
     * @param name parameter name
     * @param value parameter value (may be null if test requires that scenario)
     * @return operation adding parameter definition
     */
    public static ServletDefinitionStubbingOperation stubParameter(final String name, final String value) {
        assertThat(name, notNullValue());
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                ServletParameterDefinition param = mock(ServletParameterDefinition.class);
                doReturn(name).when(param).getName();
                doReturn(value).when(param).getValue();
                Collection<ServletParameterDefinition> params = mock.getParams();
                params.add(param);
                doReturn(params).when(mock).getParams();
            }
        };
    }

    /**
     * Replaces existing servlet parameters with the provided collection for {@link ServletDefinition#getParams()}.
     * @param params collection of servlet parameter definitions (non-null)
     * @return operation configuring parameter definitions
     */
    public static ServletDefinitionStubbingOperation stubParams(final Collection<ServletParameterDefinition> params) {
        assertThat(params, notNullValue());
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(params).when(mock).getParams();
            }
        };
    }
}
