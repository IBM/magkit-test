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
 * A factory class to create StubbingOperations that define the behaviour of info.magnolia.module.model.ServletDefinition mocks.
 * To be used standalone or as parameter of ModuleMockUtils.mockServletDefinition(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-13
 */
public abstract class ServletDefinitionStubbingOperation implements StubbingOperation<ServletDefinition> {

    public static ServletDefinitionStubbingOperation stubName(final String name) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(name).when(mock).getName();
            }
        };
    }

    public static ServletDefinitionStubbingOperation stubClassName(final String name) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(name).when(mock).getClassName();
            }
        };
    }

    public static ServletDefinitionStubbingOperation stubComment(final String value) {
        return new ServletDefinitionStubbingOperation() {
            @Override
            public void of(ServletDefinition mock) {
                assertThat(mock, notNullValue());
                doReturn(value).when(mock).getComment();
            }
        };
    }

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
