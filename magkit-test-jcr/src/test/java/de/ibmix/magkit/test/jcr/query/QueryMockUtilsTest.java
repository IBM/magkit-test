package de.ibmix.magkit.test.jcr.query;

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

import de.ibmix.magkit.test.jcr.RowStubbingOperation;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
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
        QueryMockUtils.mockQuery("de", "statement", (QueryStubbingOperation[]) null);
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

        qr = QueryMockUtils.mockQueryResult();
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

    @Test
    public void mockQueryManagerWithStubbingOperations() throws RepositoryException {
        QueryManagerStubbingOperation stubbing = mock(QueryManagerStubbingOperation.class);
        QueryManager qm = QueryMockUtils.mockQueryManager(stubbing);
        assertThat(qm, notNullValue());
        verify(stubbing, times(1)).of(qm);
    }

    @Test
    public void mockQueryManagerForWorkspaceWithStubbingOperations() throws RepositoryException {
        QueryManagerStubbingOperation stubbing1 = mock(QueryManagerStubbingOperation.class);
        QueryManagerStubbingOperation stubbing2 = mock(QueryManagerStubbingOperation.class);

        QueryManager qm = QueryMockUtils.mockQueryManager("testWorkspace", stubbing1, stubbing2);
        assertThat(qm, notNullValue());

        verify(stubbing1, times(1)).of(qm);
        verify(stubbing2, times(1)).of(qm);
    }

    @Test
    public void mockQueryManagerReusesExistingQueryManager() throws RepositoryException {
        // Create first QueryManager
        QueryManager qm1 = QueryMockUtils.mockQueryManager("reuseTest");

        // Get QueryManager again for same workspace - should return the same instance
        QueryManager qm2 = QueryMockUtils.mockQueryManager("reuseTest");

        assertThat(qm1, is(qm2));
    }

    @Test
    public void mockQueryManagerWithNullWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager((String) null);
        assertThat(mockSession("website").getWorkspace().getQueryManager(), is(qm));
    }

    @Test
    public void mockQueryManagerWithEmptyWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("");
        assertThat(mockSession("website").getWorkspace().getQueryManager(), is(qm));
    }

    @Test
    public void mockQueryManagerWithBlankWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("   ");
        assertThat(mockSession("website").getWorkspace().getQueryManager(), is(qm));
    }

    @Test
    public void mockQueryWithManagerWithoutAdditionalStubbings() throws RepositoryException {
        Query q = QueryMockUtils.mockQueryWithManager("simpleWorkspace", "SQL-2", "SELECT * FROM [nt:base]");

        assertThat(q.getLanguage(), is("SQL-2"));
        assertThat(q.getStatement(), is("SELECT * FROM [nt:base]"));

        QueryManager qm = mockSession("simpleWorkspace").getWorkspace().getQueryManager();
        assertThat(qm.createQuery("SELECT * FROM [nt:base]", "SQL-2"), is(q));
    }

    @Test
    public void mockQueryWithManagerWithMultipleStubbings() throws RepositoryException {
        Node resultNode = mockNode("result");
        QueryStubbingOperation stubbing1 = QueryStubbingOperation.stubResult(resultNode);
        QueryStubbingOperation stubbing2 = mock(QueryStubbingOperation.class);

        Query q = QueryMockUtils.mockQueryWithManager("multiStubWorkspace", "XPATH", "//element(*,nt:base)",
                stubbing1, stubbing2);

        assertThat(q.getLanguage(), is("XPATH"));
        assertThat(q.getStatement(), is("//element(*,nt:base)"));
        assertThat(q.execute().getNodes().nextNode(), is(resultNode));

        verify(stubbing2, times(1)).of(q);
    }

    @Test(expected = AssertionError.class)
    public void testMockRowQueryResultForNull() throws RepositoryException {
        QueryMockUtils.mockRowQueryResult((Row[]) null);
    }

    @Test
    public void mockEmptyRowQueryResult() throws RepositoryException {
        QueryResult qr = QueryMockUtils.mockRowQueryResult();
        assertThat(qr, notNullValue());
        assertThat(qr.getRows().hasNext(), is(false));
        assertThat(qr.getNodes().hasNext(), is(false));
    }

    @Test
    public void mockRowWithMultipleStubbingOperations() throws RepositoryException {
        RowStubbingOperation stubbing1 = RowStubbingOperation.stubValue("prop1", "value1");
        RowStubbingOperation stubbing2 = RowStubbingOperation.stubValue("prop2", "value2");

        Row row = QueryMockUtils.mockRow(0.8, stubbing1, stubbing2);

        assertThat(row.getScore(), is(0.8));
        assertThat(row.getValue("prop1").getString(), is("value1"));
        assertThat(row.getValue("prop2").getString(), is("value2"));
        assertThat(row.getValues(), notNullValue());
        assertThat(row.getValues().length, is(0)); // Default empty values array
    }

    @Test
    public void mockRowWithNoStubbingOperations() throws RepositoryException {
        Row row = QueryMockUtils.mockRow(0.5);

        assertThat(row.getScore(), is(0.5));
        assertThat(row.getValues(), notNullValue());
        assertThat(row.getValues().length, is(0));
    }

    @Test
    public void testQueryResultWithMultipleNodes() throws RepositoryException {
        Node node1 = mockNode("node1", stubProperty("id", "1"));
        Node node2 = mockNode("node2", stubProperty("id", "2"));
        Node node3 = mockNode("node3", stubProperty("id", "3"));

        QueryResult qr = QueryMockUtils.mockQueryResult(node1, node2, node3);

        // Test nodes iterator
        assertThat(qr.getNodes().nextNode(), is(node1));
        assertThat(qr.getNodes().nextNode(), is(node2));
        assertThat(qr.getNodes().nextNode(), is(node3));
        assertThat(qr.getNodes().hasNext(), is(false));

        // Test rows iterator
        assertThat(qr.getRows().nextRow().getNode(), is(node1));
        assertThat(qr.getRows().nextRow().getNode(), is(node2));
        assertThat(qr.getRows().nextRow().getNode(), is(node3));
        assertThat(qr.getRows().hasNext(), is(false));
    }

    @Test
    public void testToRowWithComplexNodeStructure() throws RepositoryException {
        Node parentNode = mockNode("parent", stubProperty("parentProp", "parentValue"));
        Node childNode = mockNode("parent/child", stubProperty("childProp", "childValue"));
        Node grandChildNode = mockNode("parent/child/grandchild", stubProperty("grandChildProp", "grandChildValue"));

        Row row = QueryMockUtils.toRow(parentNode);

        assertThat(row.getNode(), is(parentNode));
        assertThat(row.getValue("parentProp").getString(), is("parentValue"));
        assertThat(row.getPath(), is("/parent"));
        assertThat(row.getScore(), is(0.0));

        // Test access to child nodes through row
        assertThat(row.getNode("child"), is(childNode));
        assertThat(row.getValue("child/childProp").getString(), is("childValue"));
        assertThat(row.getPath("child"), is("/parent/child"));
        assertThat(row.getScore("child"), is(0.0));

        // Test access to grandchild nodes through row
        assertThat(row.getNode("child/grandchild"), is(grandChildNode));
        assertThat(row.getValue("child/grandchild/grandChildProp").getString(), is("grandChildValue"));
        assertThat(row.getPath("child/grandchild"), is("/parent/child/grandchild"));
        assertThat(row.getScore("child/grandchild"), is(0.0));
    }

    @Test
    public void testAnswerImplementationsWithComplexQueryResult() throws RepositoryException {
        Node node1 = mockNode("testNode1", stubProperty("test", "value1"));
        Node node2 = mockNode("testNode2", stubProperty("test", "value2"));

        QueryResult qr = QueryMockUtils.mockQueryResult(node1, node2);

        // Verify NODES_ANSWER functionality through multiple iterations
        assertThat(qr.getNodes().nextNode(), is(node1));
        assertThat(qr.getNodes().nextNode(), is(node2));
        assertThat(qr.getNodes().hasNext(), is(false));

        // Reset and test again to ensure Answer works consistently
        assertThat(qr.getNodes().nextNode(), is(node1));
        assertThat(qr.getNodes().nextNode(), is(node2));

        // Verify ROWS_ANSWER functionality
        Row row1 = qr.getRows().nextRow();
        Row row2 = qr.getRows().nextRow();

        assertThat(row1.getNode(), is(node1));
        assertThat(row1.getValue("test").getString(), is("value1"));
        assertThat(row2.getNode(), is(node2));
        assertThat(row2.getValue("test").getString(), is("value2"));
    }

    @Test
    public void testMultipleRowQueryResultForWorkspace() throws RepositoryException {
        Row row1 = QueryMockUtils.mockRow(0.9, RowStubbingOperation.stubValue("name1", "value1"));
        Row row2 = QueryMockUtils.mockRow(0.7, RowStubbingOperation.stubValue("name2", "value2"));
        Row row3 = QueryMockUtils.mockRow(0.5, RowStubbingOperation.stubValue("name3", "value3"));

        QueryResult qr = QueryMockUtils.mockRowQueryResult("multiRowTest", "SQL-2", "SELECT * FROM [nt:base]", row1, row2, row3);

        QueryManager qm = mockSession("multiRowTest").getWorkspace().getQueryManager();
        assertThat(qm.createQuery("SELECT * FROM [nt:base]", "SQL-2").execute(), is(qr));

        // Verify all rows are returned in correct order
        assertThat(qr.getRows().nextRow(), is(row1));
        assertThat(qr.getRows().nextRow(), is(row2));
        assertThat(qr.getRows().nextRow(), is(row3));
        assertThat(qr.getRows().hasNext(), is(false));

        // Nodes should be empty for row-only result
        assertThat(qr.getNodes().hasNext(), is(false));
    }
}
