package de.ibmix.magkit.test.cms.context;

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

import de.ibmix.magkit.test.cms.module.ModuleDefinitionMockUtils;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 */
public class InstallContextMockUtilsTest {

    @Before
    public void setUp() {

    }

    @Test
    public void testInstallContext() {
        InstallContext ic = InstallContextMockUtils.mockInstallContext();
        assertThat(ic, notNullValue());
        ModuleDefinition md = ModuleDefinitionMockUtils.mockModuleDefinition();
        ic = InstallContextMockUtils.mockInstallContext(InstallContextStubbingOperation.stubModuleDefinition(md));
        assertThat(ic.getCurrentModuleDefinition(), is(md));
    }

}
