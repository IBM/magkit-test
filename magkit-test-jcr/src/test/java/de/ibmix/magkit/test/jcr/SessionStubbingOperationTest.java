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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testing SessionStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-08-03
 */
public class SessionStubbingOperationTest {

    private Session _session;

    @BeforeEach
    public void setUp() {
        _session = mock(Session.class);
    }

    /**
     * Test of stubAttribute method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubAttribute() throws RepositoryException {
        assertNull(_session.getAttribute("name1"));
        assertNull(_session.getAttributeNames());

        Object value1 = new Object();
        SessionStubbingOperation.stubAttribute("name1", value1).of(_session);
        assertEquals(value1, _session.getAttribute("name1"));
        assertNotNull(_session.getAttributeNames());
        assertEquals(1, _session.getAttributeNames().length);
        assertEquals("name1", _session.getAttributeNames()[0]);

        Object value2 = new Object();
        SessionStubbingOperation.stubAttribute("name2", value2).of(_session);
        assertEquals(value2, _session.getAttribute("name2"));
        assertNotNull(_session.getAttributeNames());
        assertEquals(2, _session.getAttributeNames().length);
        assertEquals("name1", _session.getAttributeNames()[0]);
        assertEquals("name2", _session.getAttributeNames()[1]);
    }

    /**
     * Test of stubItem method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubItem() throws RepositoryException {
        assertNull(_session.getItem("path/name1"));
        assertNull(_session.getNodeByIdentifier("uuid-1"));

        Item item = mock(Item.class);
        when(item.getPath()).thenReturn("path/name1");
        SessionStubbingOperation.stubItem(item).of(_session);
        assertEquals(item, _session.getItem("path/name1"));
        assertNull(_session.getNodeByIdentifier("uuid-1"));

        Node node = mock(Node.class);
        when(node.getUUID()).thenReturn("uuid-2");
        when(node.getIdentifier()).thenReturn("uuid-2");
        when(node.isNode()).thenReturn(Boolean.TRUE);
        when(node.getPath()).thenReturn("path/name2");
        SessionStubbingOperation.stubItem(node).of(_session);
        assertEquals(node, _session.getItem("path/name2"));
        assertEquals(node, _session.getNodeByIdentifier("uuid-2"));
    }

    /**
     * Test of stubRootNode method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubRootNode() throws RepositoryException {
        assertNull(_session.getRootNode());

        Node node = mock(Node.class);
        SessionStubbingOperation.stubRootNode(node).of(_session);
        assertEquals(node, _session.getRootNode());
    }

    /**
     * Test of stubRepository method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubRepository() throws RepositoryException {
        assertNull(_session.getRepository());

        Repository value = mock(Repository.class);
        SessionStubbingOperation.stubRepository(value).of(_session);
        assertEquals(value, _session.getRepository());
    }

    /**
     * Test of stubWorkspace method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubWorkspace() throws RepositoryException {
        assertNull(_session.getWorkspace());

        Workspace value = mock(Workspace.class);
        SessionStubbingOperation.stubWorkspace(value).of(_session);
        assertEquals(value, _session.getWorkspace());
    }

    @Test
    public void testStubValueFactory() throws RepositoryException {
        assertNull(_session.getValueFactory());
        ValueFactory factory = ValueFactoryMockUtils.mockValueFactory();
        SessionStubbingOperation.stubValueFactory(factory).of(_session);
        assertEquals(factory, _session.getValueFactory());
    }

    @Test
    public void testStubValueFactoryWithOperations() throws RepositoryException {
        assertNull(_session.getValueFactory());
        ValueFactoryStubbingOperation op1 = mock(ValueFactoryStubbingOperation.class);
        ValueFactoryStubbingOperation op2 = mock(ValueFactoryStubbingOperation.class);
        SessionStubbingOperation.stubValueFactory(op1, op2).of(_session);
        ValueFactory factory = _session.getValueFactory();
        assertNotNull(factory);
        verify(op1, times(1)).of(factory);
        verify(op2, times(1)).of(factory);

        ValueFactoryStubbingOperation op3 = mock(ValueFactoryStubbingOperation.class);
        ValueFactoryStubbingOperation op4 = mock(ValueFactoryStubbingOperation.class);
        SessionStubbingOperation.stubValueFactory(op3, op4).of(_session);
        assertEquals(factory, _session.getValueFactory());
        verify(op3, times(1)).of(factory);
        verify(op4, times(1)).of(factory);
    }
}
