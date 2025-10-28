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

import org.apache.jackrabbit.JcrConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing SessionMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class SessionMockUtilsTest {

    @BeforeEach
    public void setUp() {
    }

    /**
     * Test of mockSession method, of class SessionMockUtils.
     */
    @Test
    public void testMockSessionForWorkspace() throws RepositoryException {
        SessionStubbingOperation op1 = mock(SessionStubbingOperation.class);
        SessionStubbingOperation op2 = mock(SessionStubbingOperation.class);
        Session session = SessionMockUtils.mockSession("testRepository", op1, op2);
        Mockito.verifyNoInteractions(session);
        assertNotNull(session);
        verify(op1, times(1)).of(session);
        verify(op2, times(1)).of(session);
        assertEquals(RepositoryMockUtils.mockRepository(), session.getRepository());
        assertEquals(RepositoryMockUtils.mockRepository().login("testRepository"), session);
        assertEquals("testRepository", session.getWorkspace().getName());
    }

    @Test
    public void testMockPlainSession() throws RepositoryException {
        Session session = SessionMockUtils.mockPlainSession();
        Mockito.verifyNoInteractions(session);
        assertNotNull(session);
        Node root = session.getRootNode();
        assertNotNull(root);
        assertEquals("/", root.getPath());
        assertEquals("", root.getName());
        assertEquals("cafebabe-cafe-babe-cafe-babecafebabe", root.getIdentifier());
        assertNotNull(root.getPrimaryNodeType());
        assertEquals("rep:root", root.getPrimaryNodeType().getName());
        assertEquals("rep:root", root.getProperty(JcrConstants.JCR_PRIMARYTYPE).getString());
        assertEquals(root, session.getNode("/"));
        assertEquals(root, session.getItem("/"));
        assertEquals(root, session.getNodeByIdentifier("cafebabe-cafe-babe-cafe-babecafebabe"));
        assertTrue(session.itemExists("/"));
        assertTrue(session.nodeExists("/"));
        assertNull(session.getWorkspace());
        assertNull(session.getRepository());
        assertNull(session.getRetentionManager());
    }
}
