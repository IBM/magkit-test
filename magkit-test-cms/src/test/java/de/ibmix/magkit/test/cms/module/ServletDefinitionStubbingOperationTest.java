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

import info.magnolia.module.model.ServletDefinition;
import info.magnolia.module.model.ServletParameterDefinition;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Testing ServletDefinitionStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-13
 */
public class ServletDefinitionStubbingOperationTest {

    private ServletDefinition _servletDefinition;

    @Before
    public void setUp() throws Exception {
        _servletDefinition = mock(ServletDefinition.class);
    }

    @Test
    public void stubName() {
        assertThat(_servletDefinition.getName(), nullValue());

        ServletDefinitionStubbingOperation.stubName("test").of(_servletDefinition);
        assertThat(_servletDefinition.getName(), is("test"));
    }

    @Test
    public void stubClassName() {
        assertThat(_servletDefinition.getClassName(), nullValue());

        ServletDefinitionStubbingOperation.stubClassName("test").of(_servletDefinition);
        assertThat(_servletDefinition.getClassName(), is("test"));
    }

    @Test
    public void stubComment() {
        assertThat(_servletDefinition.getComment(), nullValue());

        ServletDefinitionStubbingOperation.stubComment("test").of(_servletDefinition);
        assertThat(_servletDefinition.getComment(), is("test"));
    }

    @Test
    public void stubMapping() {
        assertTrue(_servletDefinition.getMappings().isEmpty());

        ServletDefinitionStubbingOperation.stubMapping("test").of(_servletDefinition);
        assertTrue(_servletDefinition.getMappings().contains("test"));

        ServletDefinitionStubbingOperation.stubMapping("other").of(_servletDefinition);
        assertTrue(_servletDefinition.getMappings().contains("test"));
        assertTrue(_servletDefinition.getMappings().contains("other"));
    }

    @Test
    public void stubMappings() {
        assertTrue(_servletDefinition.getMappings().isEmpty());

        ServletDefinitionStubbingOperation.stubMapping("test").of(_servletDefinition);
        assertTrue(_servletDefinition.getMappings().contains("test"));

        ServletDefinitionStubbingOperation.stubMappings(List.of("new", "mappings")).of(_servletDefinition);
        assertFalse(_servletDefinition.getMappings().contains("test"));
        assertTrue(_servletDefinition.getMappings().contains("new"));
        assertTrue(_servletDefinition.getMappings().contains("mappings"));
    }

    @Test
    public void stubParameter() {
        assertTrue(_servletDefinition.getParams().isEmpty());

        ServletDefinitionStubbingOperation.stubParameter("test1", "value1").of(_servletDefinition);
        assertThat(_servletDefinition.getParams().size(), is(1));

        ServletDefinitionStubbingOperation.stubParameter("test2", "value2").of(_servletDefinition);
        assertThat(_servletDefinition.getParams().size(), is(2));
    }

    @Test
    public void stubParams() {
        assertTrue(_servletDefinition.getParams().isEmpty());

        ServletDefinitionStubbingOperation.stubParams(List.of(mock(ServletParameterDefinition.class), mock(ServletParameterDefinition.class))).of(_servletDefinition);
        assertThat(_servletDefinition.getParams().size(), is(2));

        ServletDefinitionStubbingOperation.stubParams(List.of(mock(ServletParameterDefinition.class))).of(_servletDefinition);
        assertThat(_servletDefinition.getParams().size(), is(1));
    }
}
