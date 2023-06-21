package de.ibmix.magkit.test.cms;

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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 */
public abstract class ModuleDefinitionStubbingOperation {

    public abstract void of(ModuleDefinition md);

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

}
