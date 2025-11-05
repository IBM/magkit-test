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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;

import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubObservationManager;
import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubQueryManager;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing WorkspaceStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class WorkspaceStubbingOperationTest {

    private Workspace _ws;

    @BeforeEach
    public void setUp() {
        RepositoryMockUtils.cleanRepository();
        _ws = mock(Workspace.class);
        doReturn("test").when(_ws).getName();
    }

    /**
     * Test of stubSession method, of class WorkspaceStubbingOperation.
     */
    @Test
    public void testStubSession() throws RepositoryException {
        assertNull(_ws.getSession());
        Session s = SessionMockUtils.mockSession("repo");
        WorkspaceStubbingOperation.stubSession(s).of(_ws);
        assertEquals(s, _ws.getSession());
        assertEquals(_ws, s.getWorkspace());
    }

    @Test
    public void testStubSessionWithOperations() throws RepositoryException {
        assertNull(_ws.getSession());
        SessionStubbingOperation op1 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op2 = mock(SessionStubbingOperation.class);
        WorkspaceStubbingOperation.stubSession(op1, op2).of(_ws);
        Session s = _ws.getSession();
        assertNotNull(s);
        assertEquals(_ws, s.getWorkspace());
        verify(op1, times(1)).of(s);
        verify(op2, times(1)).of(s);
        SessionStubbingOperation op3 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op4 = mock(SessionStubbingOperation.class);
        WorkspaceStubbingOperation.stubSession(op3, op4).of(_ws);
        assertEquals(s, _ws.getSession());
        assertEquals(_ws, s.getWorkspace());
        verify(op3, times(1)).of(s);
        verify(op4, times(1)).of(s);
    }

    @Test
    public void testStubQueryManager() throws RepositoryException {
        assertNull(_ws.getQueryManager());
        QueryManager qm = mock(QueryManager.class);
        stubQueryManager(qm).of(_ws);
        assertEquals(qm, _ws.getQueryManager());
    }

    @Test
    public void testStubObservationManager() throws RepositoryException {
        assertNull(_ws.getObservationManager());
        ObservationManager om = mock(ObservationManager.class);
        stubObservationManager(om).of(_ws);
        assertEquals(om, _ws.getObservationManager());
    }
}
