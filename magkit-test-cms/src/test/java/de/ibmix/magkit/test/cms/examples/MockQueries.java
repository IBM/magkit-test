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
 * @author wolf.bubenik
 * @since 04.03.16.
 */
public class MockQueries {

    @Before
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void mockJcrQueryWithMagkit() throws RepositoryException {
        // 1. die gewünschten Nodes für das Suchergebnis erzeugen:
        Node first = mockNode("testRepo", "result/one", stubProperty("test", "test-1"));
        Node second = mockNode("testRepo", "result/two", stubProperty("test", "test-2"));
        Node third = mockNode("testRepo", "result/three", stubProperty("test", "test-3"));

        // 2. Ein QueryResult mit den gewünschten Nodes erzeugen:
        // Erzeugt man das QueryResult-Mock mit den neuen Methoden des ContextMockUtils, wird sicher gestellt, dass die Session auch über den MagnoliaContext verfügbar ist.
        ContextMockUtils.mockQueryResult("testRepo", Query.XPATH, "test query statement", first, second, third, second);

        // Fertig. Die Magnolia QueryUtils funktionieren damit auch:
        NodeIterator mgnlResult = QueryUtil.search("testRepo", "test query statement", Query.XPATH);
        assertThat(mgnlResult, notNullValue());
        assertThat(mgnlResult.nextNode(), is(first));
        assertThat(mgnlResult.nextNode(), is(second));
        assertThat(mgnlResult.nextNode(), is(third));
        // the duplicated Node (/result/two) has been removed from result by QueryUtils.
        assertThat(mgnlResult.hasNext(), is(false));

        // Nimmt man die Ausführung der Query selbst in die Hand, können die Node properties auch als Rows des QueryResults abgefragt werden:
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
        // 1. Für die Suche brauchen wir eine Session mit den Nodes des Suchergebnisses. Die werden alle zurückgeliefert.
        // Achtung: Wir müssen die MockNodes für das QueryResult mit einem type erzeugen, der NUR aus Kleinbuchstaben besteht.
        // Grund ist ein Bugs im MockQueryResult. Es filtert des Ergebnisses nach nodeType, der zuvor in Kleinbuchstaben umgewandelt wurde.
        Session session = SessionTestUtil.createSession("testRepo",
            "/result/one.@type=mgnl:contentnode",
            "/result/two.@type=mgnl:contentnode",
            "/result/three.@type=mgnl:contentnode"
        );
        Node first = session.getNode("/result/one");
        Node parent = session.getNode("/result");

        // 2. Ein QueryResult mit der Session der gewünschten Nodes erzeugen, eine Query, deren execute()-Methode das QueryResult zurückliefert
        // und einen QueryManager über den "testRepo"-Workspace der JcrSession verfügbar machen, der die Query mit createQuery(..) zurück gibt:
        // Wird von der MockSession und dem MockWorkspace bereit gestellt.

        // Fertig. Aber Achtung: Das MockQueryResult von Magnolia filtert die Nodes in der Session nach ihrem PrimaryNodeType, den es aus dem Query-Statement extrahiert.
        // Dort muss der passende primaryNodeType nach dem Literal "from" und ggf. vor dem Literal "where" enthalten sein.
        // MockNodes werden von Magnolia per default mit dem Type "mgnl:contentNode" erzeugt.
        QueryResult result = session.getWorkspace().getQueryManager().createQuery("test query statement from mgnl:contentNode", Query.XPATH).execute();
        assertThat(result, notNullValue());
        // Achtung: Das MockQueryResult liefert ALLE Knoden der Session mit passendem NodeType, auch den parent-Knoten unserer gewünschten Suchergebnisse:
        assertThat(result.getNodes().nextNode(), not(is(first)));
        assertThat(result.getNodes().nextNode(), is(parent));
    }
}
