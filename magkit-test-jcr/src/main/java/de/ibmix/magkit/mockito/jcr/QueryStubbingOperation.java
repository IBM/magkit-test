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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.mockito.jcr.QueryMockUtils.mockQueryResult;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.Query.
 *
 * @author wolf.bubenik
 * @since 29.05.13
 */
public abstract class QueryStubbingOperation {
    public abstract void of(Query query) throws RepositoryException;

    public static QueryStubbingOperation stubbLanguage(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) {
                assertThat(query, notNullValue());
                when(query.getLanguage()).thenReturn(value);
            }
        };
    }

    public static QueryStubbingOperation stubbStatement(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) {
                assertThat(query, notNullValue());
                when(query.getStatement()).thenReturn(value);
            }
        };
    }

    public static QueryStubbingOperation stubbResult(final Node... results) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                stubbResult(mockQueryResult(results)).of(query);
            }
        };
    }

    public static QueryStubbingOperation stubbResult(final QueryResult result) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                assertThat(query, notNullValue());
                when(query.execute()).thenReturn(result);
            }
        };
    }

    public static QueryStubbingOperation stubbStoredQueryPath(final String value) {
        return new QueryStubbingOperation() {

            public void of(Query query) throws RepositoryException {
                assertThat(query, notNullValue());
                when(query.getStoredQueryPath()).thenReturn(value);
            }
        };
    }
}