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

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author wolf.bubenik
 */
public class SessionStubbingOperationTest {

    private Session _session;

    @Before
    public void setUp() {
        _session = mock(Session.class);
    }

    /**
     * Test of stubAttribute method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubAttribute() throws RepositoryException {
        assertThat(_session.getAttribute("name1"), nullValue());
        assertThat(_session.getAttributeNames(), nullValue());

        Object value1 = new Object();
        SessionStubbingOperation.stubAttribute("name1", value1).of(_session);
        assertThat(_session.getAttribute("name1"), is(value1));
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().length, is(1));
        assertThat(_session.getAttributeNames()[0], is("name1"));

        Object value2 = new Object();
        SessionStubbingOperation.stubAttribute("name2", value2).of(_session);
        assertThat(_session.getAttribute("name2"), is(value2));
        assertThat(_session.getAttributeNames(), notNullValue());
        assertThat(_session.getAttributeNames().length, is(2));
        assertThat(_session.getAttributeNames()[0], is("name1"));
        assertThat(_session.getAttributeNames()[1], is("name2"));
    }

    /**
     * Test of stubItem method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubItem() throws PathNotFoundException, RepositoryException {
        assertThat(_session.getItem("path/name1"), nullValue());
        assertThat(_session.getNodeByIdentifier("uuid-1"), nullValue());

        Item item = mock(Item.class);
        when(item.getPath()).thenReturn("path/name1");
        SessionStubbingOperation.stubItem(item).of(_session);
        assertThat(_session.getItem("path/name1"), is(item));
        assertThat(_session.getNodeByIdentifier("uuid-1"), nullValue());

        Node node = mock(Node.class);
        when(node.getUUID()).thenReturn("uuid-2");
        when(node.getIdentifier()).thenReturn("uuid-2");
        when(node.isNode()).thenReturn(Boolean.TRUE);
        when(node.getPath()).thenReturn("path/name2");
        SessionStubbingOperation.stubItem(node).of(_session);
        assertThat(_session.getItem("path/name2"), is((Item) node));
        assertThat(_session.getNodeByIdentifier("uuid-2"), is(node));
    }

    /**
     * Test of stubRootNode method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubRootNode() throws RepositoryException {
        assertThat(_session.getRootNode(), nullValue());

        Node node = mock(Node.class);
        SessionStubbingOperation.stubRootNode(node).of(_session);
        assertThat(_session.getRootNode(), is(node));
    }

    /**
     * Test of stubRepository method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubRepository() throws RepositoryException {
        assertThat(_session.getRepository(), nullValue());

        Repository value = mock(Repository.class);
        SessionStubbingOperation.stubRepository(value).of(_session);
        assertThat(_session.getRepository(), is(value));
    }

    /**
     * Test of stubWorkspace method, of class SessionStubbingOperation.
     */
    @Test
    public void testStubWorkspace() throws RepositoryException {
        assertThat(_session.getWorkspace(), nullValue());

        Workspace value = mock(Workspace.class);
        SessionStubbingOperation.stubWorkspace(value).of(_session);
        assertThat(_session.getWorkspace(), is(value));
    }

    @Test
    public void testStubValueFactory() throws RepositoryException {
        assertThat(_session.getValueFactory(), nullValue());
        ValueFactory factory = ValueFactoryMockUtils.mockValueFactory();
        SessionStubbingOperation.stubValueFactory(factory).of(_session);
        assertThat(_session.getValueFactory(), is(factory));
    }

    @Test
    public void testStubValueFactoryWithOperations() throws RepositoryException {
        assertThat(_session.getValueFactory(), nullValue());
        ValueFactoryStubbingOperation op1 = mock(ValueFactoryStubbingOperation.class);
        ValueFactoryStubbingOperation op2 = mock(ValueFactoryStubbingOperation.class);
        SessionStubbingOperation.stubValueFactory(op1, op2).of(_session);
        ValueFactory factory = _session.getValueFactory();
        assertThat(factory, notNullValue());
        verify(op1, times(1)).of(factory);
        verify(op2, times(1)).of(factory);

        ValueFactoryStubbingOperation op3 = mock(ValueFactoryStubbingOperation.class);
        ValueFactoryStubbingOperation op4 = mock(ValueFactoryStubbingOperation.class);
        SessionStubbingOperation.stubValueFactory(op3, op4).of(_session);
        assertThat(_session.getValueFactory(), is(factory));
        verify(op3, times(1)).of(factory);
        verify(op4, times(1)).of(factory);
    }
}
