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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static de.ibmix.magkit.test.jcr.QueryStubbingOperation.stubLanguage;
import static de.ibmix.magkit.test.jcr.QueryStubbingOperation.stubResult;
import static de.ibmix.magkit.test.jcr.QueryStubbingOperation.stubStatement;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static de.ibmix.magkit.test.jcr.WorkspaceStubbingOperation.stubQueryManager;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class for mocking javax.jcr.QueryResult, javax.jcr.Query and javax.jcr.QueryManager.
 *
 * @author wolf.bubenik
 * @since 29.05.13
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
     */
    public static QueryManager mockQueryManager(final String workspace, QueryManagerStubbingOperation... stubbings) throws RepositoryException {
        assertThat(stubbings, notNullValue());
        Session session = isBlank(workspace) ? mockSession("website") : mockSession(workspace);
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
    public static JackrabbitQueryResult mockQueryResult(final Row... results) throws RepositoryException {
        assertThat(results, notNullValue());
        JackrabbitQueryResult result = mockQueryResult(emptyList());
        doReturn(new RowIteratorAdapter(Arrays.asList(results))).when(result).getRows();
        return result;
    }

    static JackrabbitQueryResult mockQueryResult(List<Node> nodes) throws RepositoryException {
        TestQueryResult result = mock(TestQueryResult.class);
        when(result.getNodeCollection()).thenReturn(nodes);
        when(result.getNodes()).then(NODES_ANSWER);
        when(result.getRows()).then(ROWS_ANSWER);
        return result;
    }

    /**
     * Creates a JackrabbitQueryResult mock that is connected to a Query instance but not to a QueryManager.
     *
     * @param language  the query language, e.g. "SQL-2", "SQL" or "XPATH"
     * @param statement the statement of the Query that returns this result
     * @param results   the Nodes to be returned as query result
     * @return a Mockito mock of the org.apache.jackrabbit.api.query.JackrabbitQueryResult interface
     * @throws RepositoryException never, declared only to match interfaces
     */
    public static JackrabbitQueryResult mockQueryResult(final String language, final String statement, final Node... results) throws RepositoryException {
        JackrabbitQueryResult result = mockQueryResult(results);
        mockQuery(language, statement, stubResult(result));
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
    public static JackrabbitQueryResult mockQueryResult(final String workspace, final String queryLang, final String queryStatement, final Row... results) throws RepositoryException {
        JackrabbitQueryResult result = mockQueryResult(results);
        mockQueryWithManager(workspace, queryLang, queryStatement, stubResult(result));
        return result;
    }

    public static Row mockRow(String selector, String value, double score) throws RepositoryException {
        Row result = Mockito.mock(Row.class);
        if (value != null) {
            doReturn(ValueMockUtils.mockValue(value)).when(result).getValue(selector);
        }
        doReturn(score).when(result).getScore(selector);
        return result;
    }

    static Row toRow(final Node node) {
        return new Row() {
            @Override
            public Value[] getValues() {
                return new Value[0];
            }

            @Override
            public Value getValue(String columnName) throws RepositoryException {
                return node.getProperty(columnName).getValue();
            }

            @Override
            public Node getNode() {
                return node;
            }

            @Override
            public Node getNode(String selectorName) throws RepositoryException {
                return node.getNode(selectorName);
            }

            @Override
            public String getPath() throws RepositoryException {
                return node.getPath();
            }

            @Override
            public String getPath(String selectorName) throws RepositoryException {
                return getNode(selectorName).getPath();
            }

            @Override
            public double getScore() {
                return 0;
            }

            @Override
            public double getScore(String selectorName) {
                return 0;
            }
        };
    }

    public static final Answer<NodeIteratorAdapter> NODES_ANSWER = invocation -> {
        TestQueryResult result = (TestQueryResult) invocation.getMock();
        return new NodeIteratorAdapter(result.getNodeCollection());
    };

    public static final Answer<RowIterator> ROWS_ANSWER = invocation -> {
        TestQueryResult result = (TestQueryResult) invocation.getMock();
        Collection<Row> rows = result.getNodeCollection().stream().map(QueryMockUtils::toRow).collect(Collectors.toList());
        return new RowIteratorAdapter(rows);
    };

    /**
     * Extended Interface to simplify mocking.
     */
    interface TestQueryResult extends JackrabbitQueryResult {
        Collection<Node> getNodeCollection();
    }

}
