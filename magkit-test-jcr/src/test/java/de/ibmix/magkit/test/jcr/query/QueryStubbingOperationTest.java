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

import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * TestingQueryStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public class QueryStubbingOperationTest {
    @Test
    public void testStubbLanguage() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubLanguage("sql");
        assertNotNull(op);
        op.of(q);
        assertEquals("sql", q.getLanguage());
    }

    @Test
    public void testStubbStatement() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubStatement("statement");
        assertNotNull(op);
        op.of(q);
        assertEquals("statement", q.getStatement());
    }

    @Test
    public void testStubbResult() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubResult();
        assertNotNull(op);
        op.of(q);
        assertNotNull(q.execute());
    }

    @Test
    public void testStubbResultWithQuery() throws RepositoryException {
        Query q = mock(Query.class);
        QueryResult qr = mock(QueryResult.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubResult(qr);
        assertNotNull(op);
        op.of(q);
        assertNotNull(q.execute());
    }

    @Test
    public void testStubbStoredQueryPath() throws RepositoryException {
        Query q = mock(Query.class);
        QueryStubbingOperation op = QueryStubbingOperation.stubStoredQueryPath("path");
        assertNotNull(op);
        op.of(q);
        assertEquals("path", q.getStoredQueryPath());
    }
}
