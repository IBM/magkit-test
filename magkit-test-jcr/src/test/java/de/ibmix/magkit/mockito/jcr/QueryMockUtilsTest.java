package de.ibmix.magkit.mockito.jcr;

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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.mockito.jcr.QueryMockUtils.mockQuery;
import static de.ibmix.magkit.mockito.jcr.QueryMockUtils.mockQueryResult;
import static de.ibmix.magkit.mockito.jcr.SessionMockUtils.mockSession;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing QueryMockUtils.
 *
 * @author wolf.bubenik
 * @since 29.05.13
 */
public class QueryMockUtilsTest {

    @Before
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test(expected = AssertionError.class)
    public void testMockQueryForNull() throws RepositoryException {
        mockQuery("de", "statement", null);
    }

    @Test
    public void testMockQuery() throws RepositoryException {
        QueryStubbingOperation op = mock(QueryStubbingOperation.class);
        Query q = mockQuery("de", "statement", op);
        assertThat(q, notNullValue());
        assertThat(q.getLanguage(), is("de"));
        assertThat(q.getStatement(), is("statement"));
        verify(op, times(1)).of(any(Query.class));
    }

    @Test(expected = AssertionError.class)
    public void testMockQueryResultForNull() throws RepositoryException {
        mockQueryResult((Node[]) null);
    }

    @Test
    public void testMockQueryResult() throws RepositoryException {
        Node c = mockNode();
        QueryResult qr = mockQueryResult(c);
        assertThat(qr, notNullValue());
        assertThat(qr.getNodes(), notNullValue());
        assertThat(qr.getNodes().next(), is(c));
        assertThat(qr.getRows(), notNullValue());
        assertThat(qr.getRows().nextRow().getNode(), is(c));

        qr = mockQueryResult(new Node[0]);
        assertThat(qr, notNullValue());
        assertThat(qr.getNodes(), notNullValue());
        assertThat(qr.getNodes().hasNext(), is(false));
    }

    @Test
    public void testMockQueryWithManager() throws RepositoryException {
        Node c = mockNode();
        mockQuery("repository", "xpath", "statement", c);
        QueryManager queryManager = mockSession("repository").getWorkspace().getQueryManager();
        assertThat(queryManager, notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath"), notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath").execute(), notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath").execute().getNodes(), notNullValue());
        assertThat((Node) queryManager.createQuery("statement", "xpath").execute().getNodes().next(), is(c));
    }
}
