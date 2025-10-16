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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compare Magnolia JCR Mock-Objects with this API.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-01-29
 */
public class MockNodes {

    @AfterEach
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
        assertSame(child, child2);
        assertSame(child.getSession(), child2.getSession());

        // And what you get:
        // basic node properties:
        assertEquals("child", child.getName());
        assertEquals("/root/parent/child", child.getPath());
        assertEquals(3, child.getDepth());
        assertFalse(child.hasNodes());
        // we have one property "jcr:primaryType..."
        assertEquals(1L, child.getProperties().getSize());
        assertTrue(child.hasProperties());
        // ... with the defined value 'mgnl:contentNode' ({http://www.jcp.org/jcr/nt/1.0}base is the default value)
        assertTrue(child.hasProperty("jcr:primaryType"));
        assertEquals("mgnl:contentNode", child.getProperty("jcr:primaryType").getString());
        assertEquals("mgnl:contentNode", child.getPrimaryNodeType().getName());
        assertTrue(child.isNodeType(NodeTypes.ContentNode.NAME));

        // Session
        Session session = child.getSession();
        assertNotNull(session);
        assertEquals("testWorkspace", session.getWorkspace().getName());
        assertSame(child, session.getNode("/root/parent/child"));
        assertSame(child.getProperty("jcr:primaryType"), session.getProperty("/root/parent/child/jcr:primaryType"));

        // hierarchy
        Node parent = child.getParent();
        assertNotNull(parent);
        assertEquals("/root/parent", parent.getPath());
        assertEquals(2, parent.getDepth());
        assertTrue(parent.hasNodes());
        assertTrue(parent.hasNode("child"));
        assertSame(child, parent.getNode("child"));
        assertNotNull(parent.getProperty("child/jcr:primaryType"));

        Node root = parent.getParent();
        assertNotNull(root);
        assertEquals("/root", root.getPath());
        assertEquals(1, root.getDepth());
        assertTrue(root.hasNodes());
        assertTrue(root.hasNode("parent"));
        assertSame(parent, root.getNode("parent"));
        assertNotNull(root.getProperty("parent/jcr:primaryType"));
        assertTrue(root.hasNode("parent/child"));
        assertSame(child, root.getNode("parent/child"));
        assertNotNull(root.getProperty("parent/child/jcr:primaryType"));

        Node sessionRoot = root.getParent();
        assertNotNull(sessionRoot);
        assertEquals("/", sessionRoot.getPath());
        assertEquals(0, sessionRoot.getDepth());
        assertTrue(sessionRoot.hasNodes());

        assertSame(child, child.getAncestor(3));
        assertSame(parent, child.getAncestor(2));
        assertSame(root, child.getAncestor(1));
        assertSame(sessionRoot, child.getAncestor(0));
    }

    /**
     * This test demonstrates, how to create a mock Node hierarchy using the Magnolia NodeTestUtil.
     */
    @Test
    public void mockMagnoliaNodeWithPath() throws RepositoryException, IOException {
        // What you do: Just mock the node for the desired workspace
        Node child = NodeTestUtil.createNode("/root/parent/child", "testWorkspace", "/root/parent/child");
        Node child2 = NodeTestUtil.createNode("/root/parent/child", "testWorkspace", "/root/parent/child");
        assertNotSame(child, child2);
        assertNotSame(child.getSession(), child2.getSession());

        // And what ypu get:
        // basic node properties:
        assertEquals("child", child.getName());
        assertEquals("/root/parent/child", child.getPath());
        assertEquals(3, child.getDepth());
        assertFalse(child.hasNodes());
        // Magnolia mocks appear to be inconsistent here: We have one property "jcr:primaryType" but no properties..
        // ... but this may be correct if the magnolia Node implementation filters the node properties.
        //  However, I cannot see anything like this in the jackrabbit Node implementation.
        assertEquals(0L, child.getProperties().getSize());
        assertFalse(child.hasProperties());
        assertFalse(child.hasProperty("jcr:primaryType"));
        // ... with default value "mgnl:contentNode"
        assertEquals("mgnl:contentNode", child.getProperty("jcr:primaryType").getString());
        assertEquals("mgnl:contentNode", child.getPrimaryNodeType().getName());
        assertTrue(child.isNodeType(NodeTypes.ContentNode.NAME));

        // Session
        Session session = child.getSession();
        assertNotNull(session);
        assertEquals("testWorkspace", session.getWorkspace().getName());
        assertSame(child, session.getNode("/root/parent/child"));
        // We do not get the same instance here, but same values
        assertEquals(child.getProperty("jcr:primaryType").getString(), session.getProperty("/root/parent/child/jcr:primaryType").getString());

        // hierarchy
        Node parent = child.getParent();
        assertNotNull(parent);
        assertEquals("/root/parent", parent.getPath());
        assertEquals(2, parent.getDepth());
        assertTrue(parent.hasNodes());
        assertTrue(parent.hasNode("child"));
        assertSame(child, parent.getNode("child"));
        assertNotNull(parent.getProperty("child/jcr:primaryType"));

        Node root = parent.getParent();
        assertNotNull(root);
        assertEquals("/root", root.getPath());
        assertEquals(1, root.getDepth());
        assertTrue(root.hasNodes());
        assertTrue(root.hasNode("parent"));
        assertSame(parent, root.getNode("parent"));
        assertNotNull(root.getProperty("parent/jcr:primaryType"));
        assertTrue(root.hasNode("parent/child"));
        assertSame(child, root.getNode("parent/child"));
        assertNotNull(root.getProperty("parent/child/jcr:primaryType"));

        Node sessionRoot = root.getParent();
        assertNotNull(sessionRoot);
        assertEquals("/", sessionRoot.getPath());
        assertEquals(0, sessionRoot.getDepth());
        assertTrue(sessionRoot.hasNodes());

        assertSame(child, child.getAncestor(3));
        assertSame(parent, child.getAncestor(2));
        assertSame(root, child.getAncestor(1));
        assertSame(sessionRoot, child.getAncestor(0));
    }
}
