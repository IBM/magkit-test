package com.aperto.magkit.mockito;

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

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static com.aperto.magkit.mockito.AccessManagerStubbingOperation.stubPermissions;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockAccessManager;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 04.11.2010
 */
public class AccessManagerStubbingOperationTest {

    @Before
    public void setUp() {
        cleanContext();
    }

    @Test
    public void stubPermissionsTest() throws RepositoryException {
        AccessManager am = mockAccessManager();
        stubPermissions("/", Permission.ALL, true).of(am);

        assertThat(am.getPermissions("/"), is(Permission.ALL));
        assertThat(am.isGranted("/", Permission.ALL), is(true));
    }
}
