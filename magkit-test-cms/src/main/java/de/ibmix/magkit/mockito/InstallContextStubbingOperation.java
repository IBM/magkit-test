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

import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 */
public abstract class InstallContextStubbingOperation {

    public abstract void of(InstallContext ic);

    public static InstallContextStubbingOperation stubModuleDefinition(final ModuleDefinition md) {
        return new InstallContextStubbingOperation() {

            @Override
            public void of(InstallContext ic) {
                assertThat(ic, notNullValue());
                when(ic.getCurrentModuleDefinition()).thenReturn(md);
            }
        };
    }

}
