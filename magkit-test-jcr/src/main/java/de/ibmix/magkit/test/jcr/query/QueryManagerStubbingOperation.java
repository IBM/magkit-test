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

import de.ibmix.magkit.test.ExceptionStubbingOperation;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Utility class for stubbing mocks of javax.jcr.QueryManager.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public abstract class QueryManagerStubbingOperation implements ExceptionStubbingOperation<QueryManager, RepositoryException> {

    /**
     * If null or an empty string is passed for query.getStatement(), queryManager.createQuery(..) will be stubbed for any statement and any language.
     *
     * @param query the javax.jcr.query to be stubbed
     * @return the QueryManagerStubbingOperation that stubs a QueryManager with the provided Query
     */
    public static QueryManagerStubbingOperation stubQuery(final Query query) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                if (isEmpty(query.getStatement())) {
                    when(manager.createQuery(anyString(), anyString())).thenReturn(query);
                } else {
                    when(manager.createQuery(query.getStatement(), query.getLanguage())).thenReturn(query);
                }
            }
        };
    }

    public static QueryManagerStubbingOperation stubQuery(final String language, final String statement, final QueryStubbingOperation... stubbings) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                stubQuery(QueryMockUtils.mockQuery(language, statement, stubbings)).of(manager);
            }
        };
    }

    public static QueryManagerStubbingOperation stubQuery(final Node node, final Query query) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getQuery(node)).thenReturn(query);
            }
        };
    }

    public static QueryManagerStubbingOperation stubSupportedQueryLanguages(final String... languages) {
        return new QueryManagerStubbingOperation() {

            public void of(QueryManager manager) throws RepositoryException {
                assertThat(manager, notNullValue());
                when(manager.getSupportedQueryLanguages()).thenReturn(languages);
            }
        };
    }
}
