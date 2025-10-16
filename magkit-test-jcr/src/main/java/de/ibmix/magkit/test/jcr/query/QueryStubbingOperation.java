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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.query.Query in unit tests.
 * <p>
 * This abstract class provides factory methods to create stubbing operations that configure
 * Query mocks with predefined behavior. The operations can be used to simulate various
 * query execution scenarios in JCR repository testing, including setting up query language,
 * statement, results, and stored query paths.
 * <p>
 * Example usage:
 * <pre>
 * Query mockQuery = mock(Query.class);
 * stubLanguage("JCR-SQL2").of(mockQuery);
 * stubStatement("SELECT * FROM [nt:base]").of(mockQuery);
 * stubResult(mockNode1, mockNode2).of(mockQuery);
 * </pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public abstract class QueryStubbingOperation implements ExceptionStubbingOperation<Query, RepositoryException> {

    /**
     * Creates a stubbing operation for Query.getLanguage() method.
     * <p>
     * Configures the Query mock to return the specified language when getLanguage() is called.
     * Common query languages include "JCR-SQL2", "xpath", and "sql".
     *
     * @param value the query language string to be returned (e.g., "JCR-SQL2", "xpath")
     * @return a QueryStubbingOperation that configures the Query mock
     */
    public static QueryStubbingOperation stubLanguage(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) {
                Require.Argument.notNull(query, "query must not be null");
                when(query.getLanguage()).thenReturn(value);
            }
        };
    }

    /**
     * Creates a stubbing operation for Query.getStatement() method.
     * <p>
     * Configures the Query mock to return the specified statement string when
     * getStatement() is called. This is typically the SQL-2 or XPath query string.
     *
     * @param value the query statement string to be returned
     * @return a QueryStubbingOperation that configures the Query mock
     */
    public static QueryStubbingOperation stubStatement(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) {
                Require.Argument.notNull(query, "query must not be null");
                when(query.getStatement()).thenReturn(value);
            }
        };
    }

    /**
     * Creates a stubbing operation for Query.execute() method with node results.
     * <p>
     * This method creates a mock QueryResult containing the provided nodes and configures
     * the Query mock to return this result when execute() is called. The nodes will be
     * available through the QueryResult's node iterator.
     *
     * @param results array of Node mocks to be included in the query result
     * @return a QueryStubbingOperation that configures the Query mock
     */
    public static QueryStubbingOperation stubResult(final Node... results) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                stubResult(QueryMockUtils.mockQueryResult(results)).of(query);
            }
        };
    }

    /**
     * Creates a stubbing operation for Query.execute() method with a QueryResult.
     * <p>
     * Configures the Query mock to return the specified QueryResult when execute()
     * is called. This allows for complete control over the query execution result,
     * including nodes, rows, and column information.
     *
     * @param result the QueryResult mock to be returned by execute()
     * @return a QueryStubbingOperation that configures the Query mock
     */
    public static QueryStubbingOperation stubResult(final QueryResult result) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                Require.Argument.notNull(query, "query must not be null");
                when(query.execute()).thenReturn(result);
            }
        };
    }

    /**
     * Creates a stubbing operation for Query.getStoredQueryPath() method.
     * <p>
     * Configures the Query mock to return the specified path when getStoredQueryPath()
     * is called. This is used for queries that have been stored in the repository
     * as nodes and can be retrieved by their path.
     *
     * @param value the repository path string where the query is stored
     * @return a QueryStubbingOperation that configures the Query mock
     */
    public static QueryStubbingOperation stubStoredQueryPath(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                Require.Argument.notNull(query, "query must not be null");
                when(query.getStoredQueryPath()).thenReturn(value);
            }
        };
    }
}
