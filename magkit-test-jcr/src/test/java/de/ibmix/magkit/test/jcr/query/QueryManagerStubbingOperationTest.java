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

import de.ibmix.magkit.test.jcr.SessionMockUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import static de.ibmix.magkit.test.jcr.query.QueryManagerStubbingOperation.stubQuery;
import static de.ibmix.magkit.test.jcr.query.QueryManagerStubbingOperation.stubSupportedQueryLanguages;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

/**
 * Testing QueryManagerStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2013-05-29
 */
public class QueryManagerStubbingOperationTest {
    @BeforeEach
    public void setUp() throws Exception {
        SessionMockUtils.cleanSession();
    }

    @Test
    public void testStubbQueryWithQueryNull() throws javax.jcr.RepositoryException {
        Query q = mock(Query.class);
        assertThrows(IllegalArgumentException.class, () -> QueryManagerStubbingOperation.stubQuery(q).of(null));
    }

    @Test
    public void testStubbQueryWithStatementNull() {
        assertThrows(IllegalArgumentException.class, () -> QueryManagerStubbingOperation.stubQuery("", "").of(null));
    }

    @Test
    public void testStubbQueryWithNodeNull() throws javax.jcr.RepositoryException {
        Query q = mock(Query.class);
        Node n = mock(Node.class);
        assertThrows(IllegalArgumentException.class, () -> stubQuery(n, q).of(null));
    }

    @Test
    public void testStubbSupportedQueryLanguagesNull() {
        assertThrows(IllegalArgumentException.class, () -> stubSupportedQueryLanguages().of(null));
    }

    @Test
    public void testStubbQueryWithQuery() throws RepositoryException {
        Query q = mock(Query.class);
        QueryManager m = mock(QueryManager.class);
        QueryManagerStubbingOperation op = QueryManagerStubbingOperation.stubQuery(q);
        assertNotNull(op);
        op.of(m);
        assertEquals(q, m.createQuery("", ""));
    }

    @Test
    public void testStubbQueryWithStatement() throws RepositoryException {
        QueryManagerStubbingOperation op = QueryManagerStubbingOperation.stubQuery("language", "statement");
        assertNotNull(op);
        QueryManager m = mock(QueryManager.class);
        op.of(m);
        assertNotNull(m.createQuery("statement", "language"));
    }

    @Test
    public void testStubbQueryWithNode() throws RepositoryException {
        Query q = mock(Query.class);
        Node n = mock(Node.class);
        QueryManagerStubbingOperation op = stubQuery(n, q);
        assertNotNull(op);
        QueryManager m = mock(QueryManager.class);
        op.of(m);
        assertEquals(q, m.getQuery(n));

        op = stubQuery(null, q);
        op.of(m);
        assertEquals(q, m.getQuery(null));

        op = stubQuery(null, null);
        op.of(m);
        assertNull(m.getQuery(null));
    }

    @Test
    public void testStubbSupportedQueryLanguages() throws RepositoryException {
        QueryManagerStubbingOperation op = stubSupportedQueryLanguages("sql", "xpath");
        assertNotNull(op);
        QueryManager m = mock(QueryManager.class);
        op.of(m);
        assertNotNull(m.getSupportedQueryLanguages());
        assertEquals(2, m.getSupportedQueryLanguages().length);
        assertEquals("sql", m.getSupportedQueryLanguages()[0]);
        assertEquals("xpath", m.getSupportedQueryLanguages()[1]);
    }
}
