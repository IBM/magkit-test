package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * Aperto Mockito Test-Utils - JCR
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

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubName;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Utility class for mocking javax.jcr.Workspace.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public final class WorkspaceMockUtils {

    private WorkspaceMockUtils() {
    }

    /**
     * Factory method for a Workspace mock with default name "test".
     *
     * @param stubbings the WorkspaceStubbingOperation to be executed on the mock
     * @return a Workspace mock, never null
     * @throws RepositoryException may be thrown by one of the stubbing operations
     */
    public static Workspace mockWorkspace(WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        return mockWorkspace("test", stubbings);
    }

    /**
     * Factory method for a Workspace mock with the given name.
     *
     * @param stubbings the WorkspaceStubbingOperation to be executed on the mock
     * @return a Workspace mock, never null
     * @throws RepositoryException may be thrown by one of the stubbing operations
     */
    public static Workspace mockWorkspace(String name, WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        assertTrue(isNotBlank(name));
        Workspace result = mock(Workspace.class);
        stubName(name).of(result);
        for (WorkspaceStubbingOperation stub : stubbings) {
            stub.of(result);
        }
        return result;
    }
}
