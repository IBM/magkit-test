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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static org.junit.jupiter.api.Assertions.*;
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

    @BeforeEach
    public void setUp() {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void mockQueryManager() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager();
        assertEquals(qm, mockSession("website").getWorkspace().getQueryManager());
    }

    @Test
    public void mockQueryManagerForWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("  ");
        assertEquals(qm, mockSession("website").getWorkspace().getQueryManager());

        qm = QueryMockUtils.mockQueryManager("test");
        assertEquals(qm, mockSession("test").getWorkspace().getQueryManager());
        assertNotNull(qm.createQuery("anyLanguage", "anyStatement").execute());
    }

    @Test
    public void testMockQueryForNull() {
        assertThrows(IllegalArgumentException.class, () -> QueryMockUtils.mockQuery("de", "statement", (QueryStubbingOperation[]) null));
    }

    @Test
    public void testMockQuery() throws RepositoryException {
        QueryStubbingOperation op = mock(QueryStubbingOperation.class);
        Query q = QueryMockUtils.mockQuery("de", "statement", op);
        assertNotNull(q);
        assertEquals("de", q.getLanguage());
        assertEquals("statement", q.getStatement());
        verify(op, times(1)).of(any(Query.class));
    }

    @Test
    public void mockQueryWithManager() throws RepositoryException {
        Query q = QueryMockUtils.mockQueryWithManager("myWorkspace", "myQueryLanguage", "myStatement");
        assertEquals("myQueryLanguage", q.getLanguage());
        assertEquals("myStatement", q.getStatement());
        assertEquals(q, mockSession("myWorkspace").getWorkspace().getQueryManager().createQuery("myStatement", "myQueryLanguage"));
    }

    @Test
    public void mockEmptyQueryResult() throws RepositoryException {
        QueryResult qr = QueryMockUtils.mockEmptyQueryResult();
        assertNotNull(qr);
        assertFalse(qr.getNodes().hasNext());
        assertFalse(qr.getRows().hasNext());
    }

    @Test
    public void mockNodeQueryResult() throws RepositoryException {
        Node result = mockNode("test", stubProperty("key", "value"));
        QueryResult qr = QueryMockUtils.mockQueryResult(result);
        assertEquals(result, qr.getNodes().nextNode());
        assertEquals(result, qr.getRows().nextRow().getNode());
        assertEquals("value", qr.getRows().nextRow().getValue("key").getString());
    }

    @Test
    public void mockRowQueryResult() throws RepositoryException {
        Row result = QueryMockUtils.mockRow(0.5, RowStubbingOperation.stubValue("name", "value"));
        QueryResult qr = QueryMockUtils.mockRowQueryResult(result);
        assertFalse(qr.getNodes().hasNext());
        assertEquals("value", qr.getRows().nextRow().getValue("name").getString());
    }

    @Test
    public void testMockQueryResultForNull() {
        assertThrows(IllegalArgumentException.class, () -> QueryMockUtils.mockQueryResult((javax.jcr.Node[]) null));
    }

    @Test
    public void testMockQueryResult() throws RepositoryException {
        Node c = mockNode();
        QueryResult qr = QueryMockUtils.mockQueryResult(c);
        assertNotNull(qr);
        assertNotNull(qr.getNodes());
        assertEquals(c, qr.getNodes().next());
        assertNotNull(qr.getRows());
        assertEquals(c, qr.getRows().nextRow().getNode());

        qr = QueryMockUtils.mockQueryResult();
        assertNotNull(qr);
        assertNotNull(qr.getNodes());
        assertFalse(qr.getNodes().hasNext());
    }

    @Test
    public void testMockQueryWithManager() throws RepositoryException {
        Node c = mockNode();
        QueryMockUtils.mockQueryWithManager("repository", "xpath", "statement", QueryStubbingOperation.stubResult(c));
        QueryManager queryManager = mockSession("repository").getWorkspace().getQueryManager();
        assertNotNull(queryManager);
        assertNotNull(queryManager.createQuery("statement", "xpath"));
        assertEquals("xpath", queryManager.createQuery("statement", "xpath").getLanguage());
        assertEquals("statement", queryManager.createQuery("statement", "xpath").getStatement());
        assertNotNull(queryManager.createQuery("statement", "xpath").execute());
        assertNotNull(queryManager.createQuery("statement", "xpath").execute().getNodes());
        assertEquals(c, queryManager.createQuery("statement", "xpath").execute().getNodes().next());
    }

    @Test
    public void mockNodeQueryResultForWorkspace() throws RepositoryException {
        Node c = mockNode();
        QueryResult qr = QueryMockUtils.mockQueryResult("test", "myQueryLang", "myStatement", c);
        assertEquals(c, qr.getNodes().nextNode());
        QueryManager queryManager = mockSession("test").getWorkspace().getQueryManager();
        assertEquals("myQueryLang", queryManager.createQuery("myStatement", "myQueryLang").getLanguage());
        assertEquals("myStatement", queryManager.createQuery("myStatement", "myQueryLang").getStatement());
        assertEquals(qr, queryManager.createQuery("myStatement", "myQueryLang").execute());
        assertEquals(c, queryManager.createQuery("myStatement", "myQueryLang").execute().getNodes().nextNode());
    }

    @Test
    public void mockRowQueryResultForWorkspace() throws RepositoryException {
        Row c = QueryMockUtils.mockRow(0.3);
        QueryResult qr = QueryMockUtils.mockRowQueryResult("test", "myQueryLang", "myStatement", c);
        QueryManager queryManager = mockSession("test").getWorkspace().getQueryManager();
        assertEquals("myQueryLang", queryManager.createQuery("myStatement", "myQueryLang").getLanguage());
        assertEquals("myStatement", queryManager.createQuery("myStatement", "myQueryLang").getStatement());
        assertEquals(qr, queryManager.createQuery("myStatement", "myQueryLang").execute());
        assertEquals(c, queryManager.createQuery("myStatement", "myQueryLang").execute().getRows().nextRow());
    }

    @Test
    public void testToRow() throws RepositoryException {
        Node root = mockNode("root", stubProperty("test", "root-test"));
        Node node = mockNode("root/node", stubProperty("test", "node-test"));
        Row row = QueryMockUtils.toRow(root);
        assertEquals(row.getNode(), root);
        assertEquals("root-test", row.getValue("test").getString());
        assertEquals("/root", row.getPath());
        assertEquals(0.0, row.getScore());
        assertNotNull(row.getValues());

        assertEquals(node, row.getNode("node"));
        assertEquals("node-test", row.getValue("node/test").getString());
        assertEquals("/root/node", row.getPath("node"));
        assertEquals(0.0, row.getScore("node"));
    }

    @Test
    public void mockRow() throws RepositoryException {
        Row row = QueryMockUtils.mockRow(0.0);
        assertNotNull(row);
        assertEquals(0.0, row.getScore());
        assertNotNull(row.getValues());
    }

    @Test
    public void mockQueryManagerWithStubbingOperations() throws RepositoryException {
        QueryManagerStubbingOperation stubbing = mock(QueryManagerStubbingOperation.class);
        QueryManager qm = QueryMockUtils.mockQueryManager(stubbing);
        assertNotNull(qm);
        verify(stubbing, times(1)).of(qm);
    }

    @Test
    public void mockQueryManagerForWorkspaceWithStubbingOperations() throws RepositoryException {
        QueryManagerStubbingOperation stubbing1 = mock(QueryManagerStubbingOperation.class);
        QueryManagerStubbingOperation stubbing2 = mock(QueryManagerStubbingOperation.class);

        QueryManager qm = QueryMockUtils.mockQueryManager("testWorkspace", stubbing1, stubbing2);
        assertNotNull(qm);

        verify(stubbing1, times(1)).of(qm);
        verify(stubbing2, times(1)).of(qm);
    }

    @Test
    public void mockQueryManagerReusesExistingQueryManager() throws RepositoryException {
        // Create first QueryManager
        QueryManager qm1 = QueryMockUtils.mockQueryManager("reuseTest");

        // Get QueryManager again for same workspace - should return the same instance
        QueryManager qm2 = QueryMockUtils.mockQueryManager("reuseTest");

        assertEquals(qm1, qm2);
    }

    @Test
    public void mockQueryManagerWithNullWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager((String) null);
        assertEquals(qm, mockSession("website").getWorkspace().getQueryManager());
    }

    @Test
    public void mockQueryManagerWithEmptyWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("");
        assertEquals(qm, mockSession("website").getWorkspace().getQueryManager());
    }

    @Test
    public void mockQueryManagerWithBlankWorkspace() throws RepositoryException {
        QueryManager qm = QueryMockUtils.mockQueryManager("   ");
        assertEquals(qm, mockSession("website").getWorkspace().getQueryManager());
    }

    @Test
    public void mockQueryWithManagerWithoutAdditionalStubbings() throws RepositoryException {
        Query q = QueryMockUtils.mockQueryWithManager("simpleWorkspace", "SQL-2", "SELECT * FROM [nt:base]");

        assertEquals("SQL-2", q.getLanguage());
        assertEquals("SELECT * FROM [nt:base]", q.getStatement());

        QueryManager qm = mockSession("simpleWorkspace").getWorkspace().getQueryManager();
        assertEquals(qm.createQuery("SELECT * FROM [nt:base]", "SQL-2"), q);
    }

    @Test
    public void mockQueryWithManagerWithMultipleStubbings() throws RepositoryException {
        Node resultNode = mockNode("result");
        QueryStubbingOperation stubbing1 = QueryStubbingOperation.stubResult(resultNode);
        QueryStubbingOperation stubbing2 = mock(QueryStubbingOperation.class);

        Query q = QueryMockUtils.mockQueryWithManager("multiStubWorkspace", "XPATH", "//element(*,nt:base)",
                stubbing1, stubbing2);

        assertEquals("XPATH", q.getLanguage());
        assertEquals("//element(*,nt:base)", q.getStatement());
        assertEquals(resultNode, q.execute().getNodes().nextNode());

        verify(stubbing2, times(1)).of(q);
    }


    @Test
    public void mockEmptyRowQueryResult() throws RepositoryException {
        QueryResult qr = QueryMockUtils.mockRowQueryResult();
        assertNotNull(qr);
        assertFalse(qr.getRows().hasNext());
        assertFalse(qr.getNodes().hasNext());
    }

    @Test
    public void mockRowWithMultipleStubbingOperations() throws RepositoryException {
        RowStubbingOperation stubbing1 = RowStubbingOperation.stubValue("prop1", "value1");
        RowStubbingOperation stubbing2 = RowStubbingOperation.stubValue("prop2", "value2");

        Row row = QueryMockUtils.mockRow(0.8, stubbing1, stubbing2);

        assertEquals(0.8, row.getScore());
        assertEquals("value1", row.getValue("prop1").getString());
        assertEquals("value2", row.getValue("prop2").getString());
        assertNotNull(row.getValues());
        // Default empty values array
        assertEquals(0, row.getValues().length);
    }

    @Test
    public void mockRowWithNoStubbingOperations() throws RepositoryException {
        Row row = QueryMockUtils.mockRow(0.5);

        assertEquals(0.5, row.getScore());
        assertNotNull(row.getValues());
        assertEquals(0, row.getValues().length);
    }

    @Test
    public void testQueryResultWithMultipleNodes() throws RepositoryException {
        Node node1 = mockNode("node1", stubProperty("id", "1"));
        Node node2 = mockNode("node2", stubProperty("id", "2"));
        Node node3 = mockNode("node3", stubProperty("id", "3"));

        QueryResult qr = QueryMockUtils.mockQueryResult(node1, node2, node3);

        // Test nodes iterator
        NodeIterator results = qr.getNodes();
        assertEquals(node1, results.nextNode());
        assertEquals(node2, results.nextNode());
        assertEquals(node3, results.nextNode());
        assertFalse(results.hasNext());

        // Test rows iterator
        RowIterator rows = qr.getRows();
        assertNotNull(rows);
        assertEquals(node1, rows.nextRow().getNode());
        assertEquals(node2, rows.nextRow().getNode());
        assertEquals(node3, rows.nextRow().getNode());
        assertFalse(rows.hasNext());
    }

    @Test
    public void testToRowWithComplexNodeStructure() throws RepositoryException {
        Node parentNode = mockNode("parent", stubProperty("parentProp", "parentValue"));
        Node childNode = mockNode("parent/child", stubProperty("childProp", "childValue"));
        Node grandChildNode = mockNode("parent/child/grandchild", stubProperty("grandChildProp", "grandChildValue"));

        Row row = QueryMockUtils.toRow(parentNode);

        assertEquals(row.getNode(), parentNode);
        assertEquals("parentValue", row.getValue("parentProp").getString());
        assertEquals("/parent", row.getPath());
        assertEquals(0.0, row.getScore());

        // Test access to child nodes through row
        assertEquals(childNode, row.getNode("child"));
        assertEquals("childValue", row.getValue("child/childProp").getString());
        assertEquals("/parent/child", row.getPath("child"));
        assertEquals(0.0, row.getScore("child"));

        // Test access to grandchild nodes through row
        assertEquals(grandChildNode, row.getNode("child/grandchild"));
        assertEquals("grandChildValue", row.getValue("child/grandchild/grandChildProp").getString());
        assertEquals("/parent/child/grandchild", row.getPath("child/grandchild"));
        assertEquals(0.0, row.getScore("child/grandchild"));
    }

    @Test
    public void testAnswerImplementationsWithComplexQueryResult() throws RepositoryException {
        Node node1 = mockNode("testNode1", stubProperty("test", "value1"));
        Node node2 = mockNode("testNode2", stubProperty("test", "value2"));

        QueryResult qr = QueryMockUtils.mockQueryResult(node1, node2);

        // Verify NODES_ANSWER functionality through multiple iterations
        NodeIterator results = qr.getNodes();
        assertEquals(node1, results.nextNode());
        assertEquals(node2, results.nextNode());
        assertFalse(results.hasNext());

        // Reset and test again to ensure Answer works consistently
        results = qr.getNodes();
        assertEquals(node1, results.nextNode());
        assertEquals(node2, results.nextNode());

        // Verify ROWS_ANSWER functionality
        RowIterator rows = qr.getRows();
        Row row1 = rows.nextRow();
        Row row2 = rows.nextRow();

        assertEquals(node1, row1.getNode());
        assertEquals("value1", row1.getValue("test").getString());
        assertEquals(node2, row2.getNode());
        assertEquals("value2", row2.getValue("test").getString());
    }

    @Test
    public void testMultipleRowQueryResultForWorkspace() throws RepositoryException {
        Row row1 = QueryMockUtils.mockRow(0.9, RowStubbingOperation.stubValue("name1", "value1"));
        Row row2 = QueryMockUtils.mockRow(0.7, RowStubbingOperation.stubValue("name2", "value2"));
        Row row3 = QueryMockUtils.mockRow(0.5, RowStubbingOperation.stubValue("name3", "value3"));

        QueryResult qr = QueryMockUtils.mockRowQueryResult("multiRowTest", "SQL-2", "SELECT * FROM [nt:base]", row1, row2, row3);

        QueryManager qm = mockSession("multiRowTest").getWorkspace().getQueryManager();
        assertSame(qr, qm.createQuery("SELECT * FROM [nt:base]", "SQL-2").execute());

        RowIterator rows = qr.getRows();
        assertSame(row1, rows.nextRow());
        assertSame(row2, rows.nextRow());
        assertSame(row3, rows.nextRow());
        assertFalse(rows.hasNext());

        assertFalse(qr.getNodes().hasNext());
    }
}
