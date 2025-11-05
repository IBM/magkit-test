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

import de.ibmix.magkit.assertions.Require;
import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.QueryManager in unit tests.
 * <p>
 * This abstract class provides factory methods to create stubbing operations that configure
 * QueryManager mocks with predefined behavior. The operations can be used to simulate
 * various query-related scenarios in JCR repository testing.
 * <p>
 * Example usage:
 * <pre>
 * QueryManager mockManager = mock(QueryManager.class);
 * Query mockQuery = mock(Query.class);
 * stubQuery(mockQuery).of(mockManager);
 * </pre>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public abstract class QueryManagerStubbingOperation implements ExceptionStubbingOperation<QueryManager, RepositoryException> {

    /**
     * Creates a stubbing operation for QueryManager.createQuery() method.
     * <p>
     * If the provided query has a null or empty statement, the QueryManager will be stubbed
     * to return the query for any statement and language combination. Otherwise, it will only
     * return the query for the exact statement and language from the provided query.
     *
     * @param query the javax.jcr.query.Query mock to be returned by createQuery()
     * @return a QueryManagerStubbingOperation that configures the QueryManager mock
     */
    public static QueryManagerStubbingOperation stubQuery(final Query query) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                Require.Argument.notNull(manager, "manager must not be null");
                if (isEmpty(query.getStatement())) {
                    when(manager.createQuery(anyString(), anyString())).thenReturn(query);
                } else {
                    when(manager.createQuery(query.getStatement(), query.getLanguage())).thenReturn(query);
                }
            }
        };
    }

    /**
     * Creates a stubbing operation for QueryManager.createQuery() with a mock query.
     * <p>
     * This method creates a mock Query with the specified language and statement,
     * applies the provided stubbings to it, and then configures the QueryManager
     * to return this mock query when createQuery() is called.
     *
     * @param language the query language (e.g., Query.JCR_SQL2, Query.XPATH)
     * @param statement the query statement string
     * @param stubbings optional query stubbing operations to apply to the created mock
     * @return a QueryManagerStubbingOperation that configures the QueryManager mock
     */
    public static QueryManagerStubbingOperation stubQuery(final String language, final String statement, final QueryStubbingOperation... stubbings) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                stubQuery(QueryMockUtils.mockQuery(language, statement, stubbings)).of(manager);
            }
        };
    }

    /**
     * Creates a stubbing operation for QueryManager.getQuery() method.
     * <p>
     * Configures the QueryManager mock to return the specified query when
     * getQuery() is called with the provided node.
     *
     * @param node the JCR node that represents a stored query
     * @param query the Query mock to be returned for the specified node
     * @return a QueryManagerStubbingOperation that configures the QueryManager mock
     */
    public static QueryManagerStubbingOperation stubQuery(final Node node, final Query query) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                Require.Argument.notNull(manager, "manager must not be null");
                when(manager.getQuery(node)).thenReturn(query);
            }
        };
    }

    /**
     * Creates a stubbing operation for QueryManager.getSupportedQueryLanguages() method.
     * <p>
     * Configures the QueryManager mock to return the specified array of supported
     * query languages when getSupportedQueryLanguages() is called.
     *
     * @param languages array of supported query language names (e.g., "JCR-SQL2", "xpath")
     * @return a QueryManagerStubbingOperation that configures the QueryManager mock
     */
    public static QueryManagerStubbingOperation stubSupportedQueryLanguages(final String... languages) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                Require.Argument.notNull(manager, "manager must not be null");
                when(manager.getSupportedQueryLanguages()).thenReturn(languages);
            }
        };
    }
}
