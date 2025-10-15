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
import org.apache.jackrabbit.api.query.JackrabbitQueryResult;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubLanguage;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubStatement;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubQueryManager;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for mocking JCR query components including QueryResult, Query, QueryManager, and Row objects.
 * <p>
 * This utility provides a comprehensive set of factory methods for creating Mockito mocks of JCR query-related
 * interfaces. It simplifies unit testing by allowing developers to create pre-configured query mocks with
 * predefined results and behaviors.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Mock QueryManager instances with automatic registration to JCR Workspace</li>
 *   <li>Create Query mocks with configurable language, statements, and results</li>
 *   <li>Generate QueryResult mocks for both Node and Row-based query results</li>
 *   <li>Support for empty query results and custom stubbing operations</li>
 * </ul>
 * </p>
 * <p>
 * Usage examples:
 * <pre>
 * // Create a QueryManager with empty result for all queries
 * QueryManager qm = QueryMockUtils.mockQueryManager();
 *
 * // Create a Query with specific result nodes
 * Node node1 = NodeMockUtils.mockNode("node1");
 * Node node2 = NodeMockUtils.mockNode("node2");
 * JackrabbitQueryResult result = QueryMockUtils.mockQueryResult("workspace", "SQL-2",
 *     "SELECT * FROM [nt:base]", node1, node2);
 *
 * // Create a standalone Query mock
 * Query query = QueryMockUtils.mockQuery("SQL-2", "SELECT * FROM [nt:base]");
 * </pre>
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public final class QueryMockUtils {

    private QueryMockUtils() {
    }

    /**
     * Mocks the QueryManager and registers it at the Workspace for "website" repository.
     * If Session and Workspace does not exist new mocks will be created.
     * If Workspace already has a QueryManager registered, the existing QueryManager will be returned.
     * Otherwise, a new QueryManager mock will be created.
     * Finally, all passed QueryManagerStubbingOperation will be executed on the QueryManager mock.
     *
     * @param stubbings the QueryManagerStubbingOperations to be executed
     * @return a QueryManager Mockito mock
     * @throws RepositoryException declared exception from node api but never thrown
     */
    public static QueryManager mockQueryManager(QueryManagerStubbingOperation... stubbings) throws RepositoryException {
        return mockQueryManager("website", stubbings);
    }

    /**
     * Mocks the QueryManager and registers it at the Workspace for the repository with the given id.
     * If Session and Workspace does not exist new mocks will be created.
     * If Workspace already has a QueryManager registered, the existing QueryManager will be returned.
     * Otherwise, a new QueryManager mock will be created.
     * If the repositoryId is null, empty or blank the QueryManager will be registered for workspace with name "website".
     * Finally, all passed QueryManagerStubbingOperation will be executed on the QueryManager mock.
     *
     * @param workspace the repository id as String.
     * @param stubbings the QueryManagerStubbingOperations to be executed
     * @return a QueryManager Mockito mock
     * @throws RepositoryException declared exception from node api but never thrown
     */
    public static QueryManager mockQueryManager(final String workspace, QueryManagerStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        Session session = mockSession(defaultIfBlank(workspace, "website"));
        QueryManager qm = session.getWorkspace().getQueryManager();
        if (qm == null) {
            qm = mock(QueryManager.class);
            // mock default query: empty result for all languages, ItemTypes and statements:
            Query q = mockQuery(EMPTY, EMPTY, QueryStubbingOperation.stubResult());
            QueryManagerStubbingOperation.stubQuery(q).of(qm);
            stubQueryManager(qm).of(session.getWorkspace());
        }
        for (QueryManagerStubbingOperation stubbing : stubbings) {
            stubbing.of(qm);
        }
        return qm;
    }

    /**
     * Create a Query mock that is not connected to any QueryManager of any Session.
     *
     * @param language  the query language, e.g. "SQL-2", "SQL" or "XPATH"
     * @param statement the query statement
     * @param stubbings any further stubbing operations defining the behaviour of this Query instance
     * @return a Mockito mock of the javax.jcr.query.Query interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static Query mockQuery(final String language, final String statement, QueryStubbingOperation... stubbings) throws RepositoryException {
        assertThat("stubbings should not be null.", stubbings, notNullValue());
        Query q = mock(Query.class);
        stubLanguage(language).of(q);
        stubStatement(statement).of(q);
        for (QueryStubbingOperation stubbing : stubbings) {
            stubbing.of(q);
        }
        return q;
    }

    /**
     * Create a Query mock that is connected to QueryManager for the Session in given workspace.
     *
     * @param workspace the workspace of the session to mock a QueryManager for
     * @param language  the query language, e.g. "SQL-2", "SQL" or "XPATH"
     * @param statement the query statement
     * @param stubbings any further stubbing operations defining the behaviour of this Query instance
     * @return a Mockito mock of the javax.jcr.query.Query interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static Query mockQueryWithManager(final String workspace, final String language, final String statement, QueryStubbingOperation... stubbings) throws RepositoryException {
        Query q = mockQuery(language, statement, stubbings);
        mockQueryManager(workspace, QueryManagerStubbingOperation.stubQuery(q));
        return q;
    }

    /**
     * Creates an empty JackrabbitQueryResult mock that is not connected to a Query instance.
     *
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockEmptyQueryResult() throws RepositoryException {
        return mockQueryResult(emptyList());
    }

    /**
     * Creates a JackrabbitQueryResult mock that is not connected to a Query instance.
     *
     * @param results the Nodes to be returned as query result
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockQueryResult(final Node... results) throws RepositoryException {
        assertThat(results, notNullValue());
        return mockQueryResult(asList(results));
    }

    /**
     * Creates a JackrabbitQueryResult mock that is not connected to a Query instance.
     *
     * @param results the Rows to be returned as query result
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockRowQueryResult(final Row... results) throws RepositoryException {
        assertThat(results, notNullValue());
        JackrabbitQueryResult result = mockEmptyQueryResult();
        doReturn(new RowIteratorAdapter(asList(results))).when(result).getRows();
        return result;
    }

    /**
     * Creates a JackrabbitQueryResult mock from a list of Nodes using the TestQueryResult interface.
     * <p>
     * This package-private method serves as the core implementation for creating query result mocks.
     * It creates a TestQueryResult mock that stores the provided nodes and uses Answer implementations
     * to provide both NodeIterator and RowIterator access to the same underlying data.
     * </p>
     *
     * @param nodes the list of Nodes to be returned as query result
     * @return a Mockito mock of the TestQueryResult interface extending JackrabbitQueryResult
     * @throws RepositoryException never thrown, declared only to match JCR API signatures
     */
    static JackrabbitQueryResult mockQueryResult(List<Node> nodes) throws RepositoryException {
        TestQueryResult result = mock(TestQueryResult.class);
        when(result.getNodeCollection()).thenReturn(nodes);
        when(result.getNodes()).then(NODES_ANSWER);
        when(result.getRows()).then(ROWS_ANSWER);
        return result;
    }

    /**
     * Creates a JackrabbitQueryResult mock for Nodes that is connected to a Query instance of a QueryManager for the defined workspace.
     *
     * @param workspace      the workspace of the session to mock a QueryManager for
     * @param queryLang      the query language, e.g. "SQL-2", "SQL" or "XPATH"
     * @param queryStatement the statement of the Query that returns this result
     * @param results        the Nodes to be returned as query result
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockQueryResult(final String workspace, final String queryLang, final String queryStatement, final Node... results) throws RepositoryException {
        JackrabbitQueryResult result = mockQueryResult(results);
        mockQueryWithManager(workspace, queryLang, queryStatement, stubResult(result));
        return result;
    }

    /**
     * Creates a JackrabbitQueryResult mock for Rows that is connected to a Query instance of a QueryManager for the defined workspace.
     *
     * @param workspace      the workspace of the session to mock a QueryManager for
     * @param queryLang      the query language, e.g. "SQL-2", "SQL" or "XPATH"
     * @param queryStatement the statement of the Query that returns this result
     * @param results        the Rows to be returned as query result
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockRowQueryResult(final String workspace, final String queryLang, final String queryStatement, final Row... results) throws RepositoryException {
        JackrabbitQueryResult result = mockRowQueryResult(results);
        mockQueryWithManager(workspace, queryLang, queryStatement, stubResult(result));
        return result;
    }

    /**
     * Creates a Row mock with the specified score and additional stubbing operations.
     * <p>
     * The Row mock is initialized with a default score and an empty Values array. Additional
     * behavior can be configured using the provided RowStubbingOperation instances.
     * </p>
     *
     * @param score the relevance score for this row (typically between 0.0 and 1.0)
     * @param stubbings optional stubbing operations to configure additional Row behavior
     * @return a Mockito mock of the javax.jcr.query.Row interface with configured score and values
     * @throws RepositoryException never thrown, declared only to match JCR API signatures
     */
    public static Row mockRow(double score, RowStubbingOperation... stubbings) throws RepositoryException {
        Row result = Mockito.mock(Row.class);
        doReturn(score).when(result).getScore();
        doReturn(new Value[0]).when(result).getValues();
        for (RowStubbingOperation stubbing : stubbings) {
            stubbing.of(result);
        }
        return result;
    }

    /**
     * Converts a Node to a Row mock with default score of 0.0.
     * <p>
     * This utility method creates a Row mock that wraps the provided Node, allowing
     * Node-based query results to be used in Row-based query operations.
     * </p>
     *
     * @param node the Node to wrap in a Row mock
     * @return a Row mock containing the specified Node with score 0.0
     * @throws RuntimeException if RepositoryException occurs during Row creation
     */
    static Row toRow(final Node node) {
        try {
            return mockRow(0.0, RowStubbingOperation.stubNode(node));
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mockito Answer implementation for providing NodeIterator results from TestQueryResult mocks.
     * <p>
     * This Answer extracts the node collection from a TestQueryResult mock and wraps it
     * in a NodeIteratorAdapter for use in JCR query result iteration.
     * </p>
     */
    public static final Answer<NodeIteratorAdapter> NODES_ANSWER = invocation -> {
        TestQueryResult result = (TestQueryResult) invocation.getMock();
        return new NodeIteratorAdapter(result.getNodeCollection());
    };

    /**
     * Mockito Answer implementation for providing RowIterator results from TestQueryResult mocks.
     * <p>
     * This Answer extracts the node collection from a TestQueryResult mock, converts each
     * Node to a Row using {@link #toRow(Node)}, and wraps the collection in a RowIteratorAdapter
     * for use in JCR query result iteration.
     * </p>
     */
    public static final Answer<RowIterator> ROWS_ANSWER = invocation -> {
        TestQueryResult result = (TestQueryResult) invocation.getMock();
        Collection<Row> rows = result.getNodeCollection().stream().map(QueryMockUtils::toRow).collect(Collectors.toList());
        return new RowIteratorAdapter(rows);
    };

    /**
     * Extended interface for JackrabbitQueryResult that provides access to the underlying Node collection.
     * <p>
     * This interface extends JackrabbitQueryResult with an additional method to retrieve the
     * Node collection directly. This simplifies the implementation of query result mocks by
     * providing a single source of truth for the result nodes, which can then be adapted
     * to both NodeIterator and RowIterator formats as needed.
     * </p>
     * <p>
     * This interface is used internally by the QueryMockUtils to create flexible query result
     * mocks that can provide both Node and Row representations of the same underlying data.
     * </p>
     */
    interface TestQueryResult extends JackrabbitQueryResult {
        /**
         * Returns the collection of Nodes that this query result contains.
         * <p>
         * This method provides direct access to the underlying Node collection,
         * which can be used by Answer implementations to generate appropriate
         * iterators for both Node and Row-based access patterns.
         * </p>
         *
         * @return the collection of Nodes in this query result
         */
        Collection<Node> getNodeCollection();
    }

}
