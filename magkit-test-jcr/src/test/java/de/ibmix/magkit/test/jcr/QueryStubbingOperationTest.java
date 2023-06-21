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

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static de.ibmix.magkit.test.jcr.QueryStubbingOperation.stubbResult;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * TestingQueryStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 29.05.13
 */
public class QueryStubbingOperationTest {
    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testStubbLanguage() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubbLanguage("sql");
        assertThat(op, notNullValue());
        op.of(q);
        assertThat(q.getLanguage(), is("sql"));
    }

    @Test
    public void testStubbStatement() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubbStatement("statement");
        assertThat(op, notNullValue());
        op.of(q);
        assertThat(q.getStatement(), is("statement"));
    }

    @Test
    public void testStubbResult() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubbResult();
        assertThat(op, notNullValue());
        op.of(q);
        assertThat(q.execute(), notNullValue());
    }

    @Test
    public void testStubbResultWithQuery() throws RepositoryException {
        Query q = mock(Query.class);
        QueryResult qr = mock(QueryResult.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubbResult(qr);
        assertThat(op, notNullValue());
        op.of(q);
        assertThat(q.execute(), notNullValue());
    }

    @Test
    public void testStubbStoredQueryPath() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubbStoredQueryPath("path");
        assertThat(op, notNullValue());
        op.of(q);
        assertThat(q.getStoredQueryPath(), is("path"));
    }
}
