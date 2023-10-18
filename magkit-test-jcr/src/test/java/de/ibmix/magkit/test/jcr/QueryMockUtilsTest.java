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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing QueryMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public class QueryMockUtilsTest {

    @Before
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void mockQueryManager() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager();
        assertThat(mockSession("website").getWorkspace().getQueryManager(), is(qm));
    }

    @Test
    public void mockQueryManagerForWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("  ");
        assertThat(mockSession("website").getWorkspace().getQueryManager(), is(qm));

        qm = QueryMockUtils.mockQueryManager("test");
        assertThat(mockSession("test").getWorkspace().getQueryManager(), is(qm));
        assertThat(qm.createQuery("anyLanguage", "anyStatement").execute(), notNullValue());
    }

    @Test(expected = AssertionError.class)
    public void testMockQueryForNull() throws RepositoryException {
        QueryMockUtils.mockQuery("de", "statement", null);
    }

    @Test
    public void testMockQuery() throws RepositoryException {
        QueryStubbingOperation op = mock(QueryStubbingOperation.class);
        Query q = QueryMockUtils.mockQuery("de", "statement", op);
        assertThat(q, notNullValue());
        assertThat(q.getLanguage(), is("de"));
        assertThat(q.getStatement(), is("statement"));
        verify(op, times(1)).of(any(Query.class));
    }

    @Test
    public void mockQueryWithManager() throws RepositoryException {
        Query q = QueryMockUtils.mockQueryWithManager("myWorkspace", "myQueryLanguage", "myStatement");
        assertThat(q.getLanguage(), is("myQueryLanguage"));
        assertThat(q.getStatement(), is("myStatement"));
        assertThat(mockSession("myWorkspace").getWorkspace().getQueryManager().createQuery("myStatement", "myQueryLanguage"), is(q));
    }

    @Test
    public void mockEmptyQueryResult() throws RepositoryException {
        QueryResult qr = QueryMockUtils.mockEmptyQueryResult();
        assertThat(qr, notNullValue());
        assertThat(qr.getNodes().hasNext(), is(false));
        assertThat(qr.getRows().hasNext(), is(false));
    }

    @Test
    public void mockNodeQueryResult() throws RepositoryException {
        Node result = mockNode("test", stubProperty("key", "value"));
        QueryResult qr = QueryMockUtils.mockQueryResult(result);
        assertThat(qr.getNodes().nextNode(), is(result));
        assertThat(qr.getRows().nextRow().getNode(), is(result));
        assertThat(qr.getRows().nextRow().getValue("key").getString(), is("value"));
    }

    @Test
    public void mockRowQueryResult() throws RepositoryException {
        Row result = QueryMockUtils.mockRow(0.5, RowStubbingOperation.stubValue("name", "value"));
        QueryResult qr = QueryMockUtils.mockRowQueryResult(result);
        assertThat(qr.getNodes().hasNext(), is(false));
        assertThat(qr.getRows().nextRow().getValue("name").getString(), is("value"));
    }

    @Test(expected = AssertionError.class)
    public void testMockQueryResultForNull() throws RepositoryException {
        QueryMockUtils.mockQueryResult((Node[]) null);
    }

    @Test
    public void testMockQueryResult() throws RepositoryException {
        Node c = mockNode();
        QueryResult qr = QueryMockUtils.mockQueryResult(c);
        assertThat(qr, notNullValue());
        assertThat(qr.getNodes(), notNullValue());
        assertThat(qr.getNodes().next(), is(c));
        assertThat(qr.getRows(), notNullValue());
        assertThat(qr.getRows().nextRow().getNode(), is(c));

        qr = QueryMockUtils.mockQueryResult(new Node[0]);
        assertThat(qr, notNullValue());
        assertThat(qr.getNodes(), notNullValue());
        assertThat(qr.getNodes().hasNext(), is(false));
    }

    @Test
    public void testMockQueryWithManager() throws RepositoryException {
        Node c = mockNode();
        QueryMockUtils.mockQueryWithManager("repository", "xpath", "statement", QueryStubbingOperation.stubResult(c));
        QueryManager queryManager = mockSession("repository").getWorkspace().getQueryManager();
        assertThat(queryManager, notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath"), notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath").getLanguage(), is("xpath"));
        assertThat(queryManager.createQuery("statement", "xpath").getStatement(), is("statement"));
        assertThat(queryManager.createQuery("statement", "xpath").execute(), notNullValue());
        assertThat(queryManager.createQuery("statement", "xpath").execute().getNodes(), notNullValue());
        assertThat((Node) queryManager.createQuery("statement", "xpath").execute().getNodes().next(), is(c));
    }

    @Test
    public void mockNodeQueryResultForWorkspace() throws RepositoryException {
        Node c = mockNode();
        QueryResult qr = QueryMockUtils.mockQueryResult("test", "myQueryLang", "myStatement", c);
        assertThat(qr.getNodes().nextNode(), is(c));
        QueryManager queryManager = mockSession("test").getWorkspace().getQueryManager();
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").getLanguage(), is("myQueryLang"));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").getStatement(), is("myStatement"));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").execute(), is(qr));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").execute().getNodes().nextNode(), is(c));
    }

    @Test
    public void mockRowQueryResultForWorkspace() throws RepositoryException {
        Row c = QueryMockUtils.mockRow(0.3);
        QueryResult qr = QueryMockUtils.mockRowQueryResult("test", "myQueryLang", "myStatement", c);
        QueryManager queryManager = mockSession("test").getWorkspace().getQueryManager();
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").getLanguage(), is("myQueryLang"));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").getStatement(), is("myStatement"));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").execute(), is(qr));
        assertThat(queryManager.createQuery("myStatement", "myQueryLang").execute().getRows().nextRow(), is(c));
    }

    @Test
    public void testToRow() throws RepositoryException {
        Node root = mockNode("root", stubProperty("test", "root-test"));
        Node node = mockNode("root/node", stubProperty("test", "node-test"));
        Row row = QueryMockUtils.toRow(root);
        assertThat(row.getNode(), is(root));
        assertThat(row.getValue("test").getString(), is("root-test"));
        assertThat(row.getPath(), is("/root"));
        assertThat(row.getScore(), is(0.0));
        assertThat(row.getValues(), notNullValue());

        assertThat(row.getNode("node"), is(node));
        assertThat(row.getValue("node/test").getString(), is("node-test"));
        assertThat(row.getPath("node"), is("/root/node"));
        assertThat(row.getScore("node"), is(0.0));
    }

    @Test
    public void mockRow() throws RepositoryException {
        Row row = QueryMockUtils.mockRow(0.0);
        assertThat(row, notNullValue());
        assertThat(row.getScore(), is(0.0));
        assertThat(row.getValues(), notNullValue());
    }
}
