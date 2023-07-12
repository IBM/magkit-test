package de.ibmix.magkit.test.cms.security;

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
import info.magnolia.cms.security.AccessManager;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for AccessManagerStubbingOperation.
 * Stubbing operations to be used as parameters in ContextMockUtils.mockAccessManager(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since : 01.09.2010
 */
public abstract class AccessManagerStubbingOperation implements StubbingOperation<AccessManager> {

    public static AccessManagerStubbingOperation stubPermissions(final String path, final long permissions, final boolean isGranted) {
        return new AccessManagerStubbingOperation() {
            @Override
            public void of(AccessManager am) {
                assertThat(am, notNullValue());
                String pathKey = isBlank(path) ? anyString() : path;
                when(am.getPermissions(pathKey)).thenReturn(permissions);
                when(am.isGranted(eq(pathKey), eq(permissions))).thenReturn(isGranted);
            }
        };
    }
}
