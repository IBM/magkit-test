package de.ibmix.magkit.test.cms.examples;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.cms.util.QueryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.mock.jcr.SessionTestUtil;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.IOException;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 * Demonstrate how to mock a JCR Query for testing code that uses jcr queries and QueryManager.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 04.03.2016.
 */
public class MockQueries {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockJcrQueryWithMagkit() throws RepositoryException {
        // 1. mock the nodes for the search result:
        Node first = mockNode("testRepo", "result/one", stubProperty("test", "test-1"));
        Node second = mockNode("testRepo", "result/two", stubProperty("test", "test-2"));
        Node third = mockNode("testRepo", "result/three", stubProperty("test", "test-3"));

        // 2. Mock a QueryResult that returns the desired nodes:
        // Using ContextMockUtils.mockQueryResult() ensures that the node session is available via the MgnlContext.
        ContextMockUtils.mockQueryResult("testRepo", Query.XPATH, "test query statement", first, second, third, second);

        // Done. The Magnolia QueryUtils can be used to access the Query:
        NodeIterator mgnlResult = QueryUtil.search("testRepo", "test query statement", Query.XPATH);
        assertThat(mgnlResult, notNullValue());
        assertThat(mgnlResult.nextNode(), is(first));
        assertThat(mgnlResult.nextNode(), is(second));
        assertThat(mgnlResult.nextNode(), is(third));
        // the duplicated Node (/result/two) has been removed from result by QueryUtils.
        assertThat(mgnlResult.hasNext(), is(false));

        // When accessing the mocked QueryResult (via workspace QueryManager) we may access the Node properties as Rows:
        QueryResult result = MgnlContext.getJCRSession("testRepo").getWorkspace().getQueryManager().createQuery("test query statement", Query.XPATH).execute();
        RowIterator rows = result.getRows();
        Row firstRow = rows.nextRow();
        assertThat(firstRow, notNullValue());
        assertThat(firstRow.getValue("test").getString(), is("test-1"));
        assertThat(rows.nextRow().getValue("test").getString(), is("test-2"));
        assertThat(rows.nextRow().getValue("test").getString(), is("test-3"));
    }

    @Test
    public void mockJcrQueryWithMagnolia() throws IOException, RepositoryException {
        // 1. To mock a search with the Magnolia TestUtils we need a Session with the desired Nodes of the QueryResult.
        // Note: We have to create the MockNodes with a NodeType that contains only lowercase letters.
        // This is due to a bug in the MockQueryResult implementation that filters the result by lowercase node type.
        Session session = SessionTestUtil.createSession("testRepo",
            "/result/one.@type=mgnl:contentnode",
            "/result/two.@type=mgnl:contentnode",
            "/result/three.@type=mgnl:contentnode"
        );
        Node first = session.getNode("/result/one");
        Node parent = session.getNode("/result");

        // 2. The Magnolia MockSession and MockWorkspace create all required mocks of QueryManager, Query and QueryResult.

        // Done but note:  that has been extracted from the query string.
        // The query string must contain the required primaryNodeType after the "from" and before the "where" clause.
        // The default primaryNodeType of MockNodes created by Magnolia is "mgnl:contentNode".
        QueryResult result = session.getWorkspace().getQueryManager().createQuery("test query statement from mgnl:contentNode", Query.XPATH).execute();
        assertThat(result, notNullValue());
        // Achtung: The MockQueryResult of Magnolia contains ALL Nodes of the Session filtered by their PrimaryNodeType:
//        assertThat(result.getNodes().nextNode(), not(is(first)));
//        assertThat(result.getNodes().nextNode(), is(parent));
    }
}
