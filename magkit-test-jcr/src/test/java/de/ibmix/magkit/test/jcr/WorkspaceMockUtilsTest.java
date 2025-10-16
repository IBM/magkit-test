package de.ibmix.magkit.test.jcr;

/*-
 * #%L
 * magkit-test-jcr Magnolia Module
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link WorkspaceMockUtils} covering default behavior, custom name support
 * and validation (blank / null name assertions).
 *
 * @author wolf.bubenik@ibmix.de
 */
public class WorkspaceMockUtilsTest {

    /**
     * Ensure the default factory method returns a workspace named "test".
     */
    @Test
    public void createWorkspaceWithDefaultName() throws RepositoryException {
        Workspace ws = WorkspaceMockUtils.mockWorkspace();
        assertEquals("test", ws.getName());
    }

    /**
     * Ensure a provided custom name is applied.
     */
    @Test
    public void createWorkspaceWithCustomName() throws RepositoryException {
        Workspace ws = WorkspaceMockUtils.mockWorkspace("custom");
        assertEquals("custom", ws.getName());
    }

    /**
     * Expect an IllegalArgumentException for a blank name (documented behavior).
     */
    @Test
    public void createWorkspaceWithBlankNameThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> WorkspaceMockUtils.mockWorkspace(" "));
    }

    /**
     * Expect an IllegalArgumentException also for null input (defensive behavior consistent with blank check).
     */
    @Test
    public void createWorkspaceWithNullNameThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> WorkspaceMockUtils.mockWorkspace((String) null));
    }
}
