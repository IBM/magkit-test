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

import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Workspace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author wolf
 */
public class WorkspaceMockUtilsTest {

    @Before
    public void setUp() {
        RepositoryMockUtils.cleanRepository();
    }

    /**
     * Test of mockWorkspace method, of class WorkspaceMockUtils.
     */
    @Test
    public void testMockWorkspace() throws RepositoryException {
        WorkspaceStubbingOperation op1 = mock(WorkspaceStubbingOperation.class);
        WorkspaceStubbingOperation op2 = mock(WorkspaceStubbingOperation.class);
        Workspace ws = WorkspaceMockUtils.mockWorkspace("ws", op1, op2);
        assertThat(ws, notNullValue());
        assertThat(ws.getName(), is("ws"));
        verify(op1, times(1)).of(ws);
        verify(op2, times(1)).of(ws);
    }
}
