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
import static org.mockito.Mockito.mock;

/**
 * Utility class for mocking javax.jcr.Workspace.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 03.08.2012
 */
public final class WorkspaceMockUtils {

    private WorkspaceMockUtils() {
    }

    public static Workspace mockWorkspace(WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        Workspace result = mock(Workspace.class);
        for (WorkspaceStubbingOperation stub : stubbings) {
            stub.of(result);
        }
        return result;
    }

    public static Workspace mockWorkspace(String name, WorkspaceStubbingOperation... stubbings) throws RepositoryException {
        Workspace ws = mockWorkspace(stubbings);
        stubName(name).of(ws);
        return ws;
    }
}
