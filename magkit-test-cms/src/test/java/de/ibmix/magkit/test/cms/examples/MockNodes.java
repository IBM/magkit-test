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

import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.test.mock.jcr.NodeTestUtil;
import org.junit.After;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-01-29
 */
public class MockNodes {

    @After
    public void cleanUp() {
        cleanContext();
    }

    /**
     * This test demonstrates, how to create a mock Node hierarchy using the MagnoliaNodeMockUtils of the Magkit.
     */
    @Test
    public void mockMockitoNodeWithPath() throws RepositoryException {
        // What you do: Just mock the node for the desired workspace
        Node child = MagnoliaNodeMockUtils.mockMgnlNode("testWorkspace", "root/parent/child", NodeTypes.ContentNode.NAME);
        Node child2 = MagnoliaNodeMockUtils.mockMgnlNode("testWorkspace", "root/parent/child", NodeTypes.ContentNode.NAME);
        assertThat(child2, is(child));
        assertThat(child2.getSession(), is(child.getSession()));

        // And what you get:
        // basic node properties:
        assertThat(child.getName(), is("child"));
        assertThat(child.getPath(), is("/root/parent/child"));
        assertThat(child.getDepth(), is(3));
        assertThat(child.hasNodes(), is(false));
        // we have one property "jcr:primaryType..."
        assertThat(child.getProperties().getSize(), is(1L));
        assertThat(child.hasProperties(), is(true));
        // ... with the defined value 'mgnl:contentNode' ({http://www.jcp.org/jcr/nt/1.0}base is the default value)
        assertThat(child.hasProperty("jcr:primaryType"), is(true));
        assertThat(child.getProperty("jcr:primaryType").getString(), is("mgnl:contentNode"));
        assertThat(child.getPrimaryNodeType().getName(), is("mgnl:contentNode"));
        assertThat(child.isNodeType(NodeTypes.ContentNode.NAME), is(true));

        // Session
        Session session = child.getSession();
        assertThat(session, notNullValue());
        assertThat(session.getWorkspace().getName(), is("testWorkspace"));
        assertThat(session.getNode("/root/parent/child"), is(child));
        assertThat(session.getProperty("/root/parent/child/jcr:primaryType"), is(child.getProperty("jcr:primaryType")));

        // hierarchy
        Node parent = child.getParent();
        assertThat(parent, notNullValue());
        assertThat(parent.getPath(), is("/root/parent"));
        assertThat(parent.getDepth(), is(2));
        assertThat(parent.hasNodes(), is(true));
        assertThat(parent.hasNode("child"), is(true));
        assertThat(parent.getNode("child"), is(child));
        assertThat(parent.getProperty("child/jcr:primaryType"), notNullValue());

        Node root = parent.getParent();
        assertThat(root, notNullValue());
        assertThat(root.getPath(), is("/root"));
        assertThat(root.getDepth(), is(1));
        assertThat(root.hasNodes(), is(true));
        assertThat(root.hasNode("parent"), is(true));
        assertThat(root.getNode("parent"), is(parent));
        assertThat(root.getProperty("parent/jcr:primaryType"), notNullValue());
        assertThat(root.hasNode("parent/child"), is(true));
        assertThat(root.getNode("parent/child"), is(child));
        assertThat(root.getProperty("parent/child/jcr:primaryType"), notNullValue());

        Node sessionRoot = root.getParent();
        assertThat(sessionRoot, notNullValue());
        assertThat(sessionRoot.getPath(), is("/"));
        assertThat(sessionRoot.getDepth(), is(0));
        assertThat(sessionRoot.hasNodes(), is(true));

        assertThat(child.getAncestor(3), is(child));
        assertThat(child.getAncestor(2), is(parent));
        assertThat(child.getAncestor(1), is(root));
        assertThat(child.getAncestor(0), is(sessionRoot));
    }

    /**
     * This test demonstrates, how to create a mock Node hierarchy using the Magnolia NodeTestUtil.
     */
    @Test
    public void mockMagnoliaNodeWithPath() throws RepositoryException, IOException {
        // What you do: Just mock the node for the desired workspace
        Node child = NodeTestUtil.createNode("/root/parent/child", "testWorkspace", "/root/parent/child");
        Node child2 = NodeTestUtil.createNode("/root/parent/child", "testWorkspace", "/root/parent/child");
        assertThat(child2, not(is(child)));
        assertThat(child2.getSession(), not(is(child.getSession())));

        // And what ypu get:
        // basic node properties:
        assertThat(child.getName(), is("child"));
        assertThat(child.getPath(), is("/root/parent/child"));
        assertThat(child.getDepth(), is(3));
        assertThat(child.hasNodes(), is(false));
        // Magnolia mocks appear to be inconsistent here: We have one property "jcr:primaryType" but no properties..
        // ... but this may be correct if the magnolia Node implementation filters the node properties.
        //  However, I cannot see anything like this in the jackrabbit Node implementation.
        assertThat(child.getProperties().getSize(), is(0L));
        assertThat(child.hasProperties(), is(false));
        assertThat(child.hasProperty("jcr:primaryType"), is(false));
        // ... with default value "mgnl:contentNode"
        assertThat(child.getProperty("jcr:primaryType").getString(), is("mgnl:contentNode"));
        assertThat(child.getPrimaryNodeType().getName(), is("mgnl:contentNode"));
        assertThat(child.isNodeType(NodeTypes.ContentNode.NAME), is(true));

        // Session
        Session session = child.getSession();
        assertThat(session, notNullValue());
        assertThat(session.getWorkspace().getName(), is("testWorkspace"));
        assertThat(session.getNode("/root/parent/child"), is(child));
        // We do not get the same instance here, but same values
        assertThat(session.getProperty("/root/parent/child/jcr:primaryType").getString(), is(child.getProperty("jcr:primaryType").getString()));

        // hierarchy
        Node parent = child.getParent();
        assertThat(parent, notNullValue());
        assertThat(parent.getPath(), is("/root/parent"));
        assertThat(parent.getDepth(), is(2));
        assertThat(parent.hasNodes(), is(true));
        assertThat(parent.hasNode("child"), is(true));
        assertThat(parent.getNode("child"), is(child));
        assertThat(parent.getProperty("child/jcr:primaryType"), notNullValue());

        Node root = parent.getParent();
        assertThat(root, notNullValue());
        assertThat(root.getPath(), is("/root"));
        assertThat(root.getDepth(), is(1));
        assertThat(root.hasNodes(), is(true));
        assertThat(root.hasNode("parent"), is(true));
        assertThat(root.getNode("parent"), is(parent));
        assertThat(root.getProperty("parent/jcr:primaryType"), notNullValue());
        assertThat(root.hasNode("parent/child"), is(true));
        assertThat(root.getNode("parent/child"), is(child));
        assertThat(root.getProperty("parent/child/jcr:primaryType"), notNullValue());

        Node sessionRoot = root.getParent();
        assertThat(sessionRoot, notNullValue());
        assertThat(sessionRoot.getPath(), is("/"));
        assertThat(sessionRoot.getDepth(), is(0));
        assertThat(sessionRoot.hasNodes(), is(true));

        assertThat(child.getAncestor(3), is(child));
        assertThat(child.getAncestor(2), is(parent));
        assertThat(child.getAncestor(1), is(root));
        assertThat(child.getAncestor(0), is(sessionRoot));
    }
}
