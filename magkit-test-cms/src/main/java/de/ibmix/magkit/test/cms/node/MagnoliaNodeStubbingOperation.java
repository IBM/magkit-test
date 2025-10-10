package de.ibmix.magkit.test.cms.node;

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

import de.ibmix.magkit.test.jcr.NodeStubbingOperation;
import de.ibmix.magkit.test.ExceptionStubbingOperation;
import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import java.util.Calendar;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Base class aggregating Magnolia specific {@link NodeStubbingOperation} factory helpers for frequently used system properties
 * (creation, modification and activation meta data) as well as convenience methods to build reference lists.
 * <p>
 * Each static factory method returns a {@link MagnoliaNodeStubbingOperation} that can be passed to one of the node mock
 * creation utilities (e.g. {@code MagnoliaNodeMockUtils.mockPageNode(...)}). When executed via {@link ExceptionStubbingOperation#of(Object)},
 * the operation stubs the corresponding JCR property on the supplied mock node. All operations perform a null-check on the target node
 * (Hamcrest assertion) to fail fast in case of test setup issues.
 * </p>
 * <p>
 * Provided calendar and string values are written verbatim to their Magnolia meta property keys as defined in {@link NodeTypes}.
 * No additional validation (e.g. chronological consistency between created and last modified dates) is performed.
 * </p>
 * <p>Example usage:</p>
 * <pre>
 *   Node page = MagnoliaNodeMockUtils.mockPageNode(
 *       MagnoliaNodeStubbingOperation.stubCreated(Calendar.getInstance()),
 *       MagnoliaNodeStubbingOperation.stubCreatedBy("author"),
 *       MagnoliaNodeStubbingOperation.stubActivationStatus(false)
 *   );
 * </pre>
 * <p>
 * Thread-safety: operations are stateless and thus thread-safe, but should only be used in test code.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2016-06-03
 */
public abstract class MagnoliaNodeStubbingOperation extends NodeStubbingOperation {

    /**
     * Create an operation that stubs the Magnolia creation date property ({@code mgnl:created}).
     *
     * @param creationDate calendar instance representing the creation timestamp (must not be null)
     * @return stubbing operation setting {@link NodeTypes.Created#CREATED} on execution
     */
    public static MagnoliaNodeStubbingOperation stubCreated(final Calendar creationDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Created.CREATED, creationDate).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia created-by user property ({@code mgnl:lastModifiedBy}).
     *
     * @param createdBy user name or system identifier that created the node (may be blank but not null)
     * @return stubbing operation setting {@link NodeTypes.Created#CREATED_BY} on execution
     */
    public static MagnoliaNodeStubbingOperation stubCreatedBy(final String createdBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Created.CREATED_BY, createdBy).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia last modified timestamp property ({@code mgnl:lastModified}).
     *
     * @param lastModifiedDate calendar instance representing the last modification timestamp (must not be null)
     * @return stubbing operation setting {@link NodeTypes.LastModified#LAST_MODIFIED}
     */
    public static MagnoliaNodeStubbingOperation stubLastModified(final Calendar lastModifiedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED, lastModifiedDate).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia last modified by user property ({@code mgnl:lastModifiedBy}).
     *
     * @param modifiedBy user name or system identifier who performed the last modification (may be blank but not null)
     * @return stubbing operation setting {@link NodeTypes.LastModified#LAST_MODIFIED_BY}
     */
    public static MagnoliaNodeStubbingOperation stubLastModifiedBy(final String modifiedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.LastModified.LAST_MODIFIED_BY, modifiedBy).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia last activated timestamp property ({@code mgnl:lastActivated}).
     *
     * @param lastActivatedDate calendar instance representing the last activation timestamp (must not be null)
     * @return stubbing operation setting {@link NodeTypes.Activatable#LAST_ACTIVATED}
     */
    public static MagnoliaNodeStubbingOperation stubLastActivated(final Calendar lastActivatedDate) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED, lastActivatedDate).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia last activated by user property ({@code mgnl:lastActivatedBy}).
     *
     * @param activatedBy user name or system identifier who performed the last activation (may be blank but not null)
     * @return stubbing operation setting {@link NodeTypes.Activatable#LAST_ACTIVATED_BY}
     */
    public static MagnoliaNodeStubbingOperation stubLastActivatedBy(final String activatedBy) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.LAST_ACTIVATED_BY, activatedBy).of(node);
            }
        };
    }

    /**
     * Create an operation that stubs the Magnolia activation status property ({@code mgnl:activationStatus}).
     *
     * @param activated boolean flag indicating whether the node is considered activated in the test context
     * @return stubbing operation setting {@link NodeTypes.Activatable#ACTIVATION_STATUS}
     */
    public static MagnoliaNodeStubbingOperation stubActivationStatus(final boolean activated) {
        return new MagnoliaNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                assertThat(node, notNullValue());
                stubProperty(NodeTypes.Activatable.ACTIVATION_STATUS, activated).of(node);
            }
        };
    }

    /**
     * Convenience helper that builds (or overwrites) a child node acting as a simple reference list container. The method:
     * <ol>
     *   <li>Stubs (creates if necessary) a child node with the provided {@code listName} and Magnolia type {@code mgnl:contentNode}.</li>
     *   <li>Writes one property per referenced node on that child using keys {@code 00}, {@code 01}, ... (leading zero + index).</li>
     *   <li>Each property value holds the referenced node's {@link Node#getIdentifier()}.</li>
     * </ol>
     * This is useful for emulating multi-value reference lists without modelling full JCR reference semantics.
     *
     * @param node parent node receiving the reference list (must not be null)
     * @param listName name of the child node container (must not be blank and should avoid collision with existing children)
     * @param referencedNodes ordered array of nodes whose identifiers will be stored; may be empty
     * @throws RepositoryException if property or node stubbing fails
     */
    public static void stubNodeReferenceList(Node node, String listName, Node... referencedNodes) throws RepositoryException {
        assert node != null;
        stubNode(listName, stubType(NodeTypes.ContentNode.NAME)).of(node);
        Node groups = node.getNode(listName);
        for (int i = 0; i < referencedNodes.length; i++) {
            stubProperty("0" + i, referencedNodes[i].getIdentifier()).of(groups);
        }
    }
}
