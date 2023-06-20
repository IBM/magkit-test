package de.ibmix.magkit.mockito;

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

import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import org.junit.Test;

import static de.ibmix.magkit.mockito.ModuleDefinitionMockUtils.mockModuleDefinition;
import static de.ibmix.magkit.mockito.ModuleDefinitionStubbingOperation.stubDisplayName;
import static de.ibmix.magkit.mockito.ModuleDefinitionStubbingOperation.stubName;
import static de.ibmix.magkit.mockito.ModuleDefinitionStubbingOperation.stubVersion;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 */
public class ModuleDefinitionStubbingOperationTest {

    @Test
    public void testDisplayName() {
        ModuleDefinition md = mockModuleDefinition(stubDisplayName("DISPLAY_NAME"));
        assertThat(md.getDisplayName(), is("DISPLAY_NAME"));
    }

    @Test
    public void testName() {
        ModuleDefinition md = mockModuleDefinition(stubName("NAME"));
        assertThat(md.getName(), is("NAME"));
    }

    @Test
    public void testVersion() {
        Version v = Version.parseVersion("1.1.3");
        ModuleDefinition md = mockModuleDefinition(stubVersion("1.1.3"));
        assertThat(md.getVersion(), is(v));
    }
}
