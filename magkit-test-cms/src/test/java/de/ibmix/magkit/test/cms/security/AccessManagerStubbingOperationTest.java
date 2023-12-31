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

import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.security.AccessManagerStubbingOperation.stubPermissions;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.security.SecurityMockUtils.mockAccessManager;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing AccessManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2010-11-04
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
