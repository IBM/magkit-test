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

import static de.ibmix.magkit.test.jcr.QueryManagerStubbingOperation.stubQuery;
import static de.ibmix.magkit.test.jcr.QueryManagerStubbingOperation.stubSupportedQueryLanguages;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Testing QueryManagerStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 29.05.13
 */
public class QueryManagerStubbingOperationTest {
    @Before
    public void setUp() throws Exception {
        SessionMockUtils.cleanSession();
    }

    @Test(expected = AssertionError.class)
    public void testStubbQueryWithQueryNull() throws RepositoryException {
        Query q = mock(Query.class);
        QueryManagerStubbingOperation.stubQuery(q).of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubbQueryWithStatementNull() throws RepositoryException {
        QueryManagerStubbingOperation.stubQuery("", "").of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubbQueryWithNodeNull() throws RepositoryException {
        Query q = mock(Query.class);
        Node n = mock(Node.class);
        stubQuery(n, q).of(null);
    }

    @Test(expected = AssertionError.class)
    public void testStubbSupportedQueryLanguagesNull() throws RepositoryException {
        stubSupportedQueryLanguages().of(null);
    }

    @Test
    public void testStubbQueryWithQuery() throws RepositoryException {
        Query q = mock(Query.class);
        QueryManager m = mock(QueryManager.class);
        QueryManagerStubbingOperation op = QueryManagerStubbingOperation.stubQuery(q);
        assertThat(op, notNullValue());
        op.of(m);
        assertThat(m.createQuery("", ""), is(q));
    }

    @Test
    public void testStubbQueryWithStatement() throws RepositoryException {
        QueryManagerStubbingOperation op = QueryManagerStubbingOperation.stubQuery("language", "statement");
        assertThat(op, notNullValue());
        QueryManager m = mock(QueryManager.class);
        op.of(m);
        assertThat(m.createQuery("statement", "language"), notNullValue());
    }

    @Test
    public void testStubbQueryWithNode() throws RepositoryException {
        Query q = mock(Query.class);
        Node n = mock(Node.class);
        QueryManagerStubbingOperation op = stubQuery(n, q);
        assertThat(op, notNullValue());
        QueryManager m = mock(QueryManager.class);
        op.of(m);
    }

    @Test
    public void testStubbSupportedQueryLanguages() throws RepositoryException {
        QueryManagerStubbingOperation op = stubSupportedQueryLanguages("sql", "xpath");
        assertThat(op, notNullValue());
        QueryManager m = mock(QueryManager.class);
        op.of(m);
        assertThat(m.getSupportedQueryLanguages(), notNullValue());
        assertThat(m.getSupportedQueryLanguages().length, is(2));
        assertThat(m.getSupportedQueryLanguages()[0], is("sql"));
        assertThat(m.getSupportedQueryLanguages()[1], is("xpath"));
    }
}
