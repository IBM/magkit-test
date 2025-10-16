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

import de.ibmix.magkit.assertions.Require;

import javax.jcr.Node;

/**
 * Factory helpers for stubbing Magnolia group node related properties for unit tests.
 * <p>
 * A Magnolia group node typically holds simple metadata (title, description) plus membership relations to other groups and roles.
 * This utility provides small, composable factory methods that return {@link UserNodeStubbingOperation} instances (historic choice to
 * reuse the same operation type) which, when executed via {@link MagnoliaNodeStubbingOperation#of(Node)}, stub the respective properties
 * or create reference list child nodes.
 * </p>
 * <p><strong>Provided operations:</strong>
 * <ul>
 *   <li><code>stubTitle</code>: Stubs textual title property "title".</li>
 *   <li><code>stubDescription</code>: Stubs textual description property "description".</li>
 *   <li><code>stubGroups</code>: Creates/overwrites a child node "groups" and stores referenced group node identifiers under numbered properties ("00", "01", ...).</li>
 *   <li><code>stubRoles</code>: Creates/overwrites a child node "roles" and stores referenced role node identifiers under numbered properties.</li>
 * </ul>
 * </p>
 * <p><strong>Reference list semantics:</strong> The helper {@link MagnoliaNodeStubbingOperation#stubNodeReferenceList(Node, String, Node...)} writes identifiers only; no JCR REFERENCE type or referential integrity is enforced.</p>
 * <p><strong>Thread-safety:</strong> Operations are stateless. Generated mock graphs are not thread-safe and should be confined to a single test method.</p>
 * <p><strong>Example usage:</strong></p>
 * <pre>
 *   Node group = MagnoliaNodeMockUtils.mockGroupNode("editors",
 *       GroupNodeStubbingOperation.stubTitle("Editors"),
 *       GroupNodeStubbingOperation.stubDescription("Group of content editors")
 *   );
 *   Node role = MagnoliaNodeMockUtils.mockRoleNode("editorRole");
 *   GroupNodeStubbingOperation.stubRoles(role).of(group);
 * </pre>
 * <p><strong>Error handling:</strong> Null target nodes trigger a fast Hamcrest assertion failure inside the delegated operations.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-10
 */
public abstract class GroupNodeStubbingOperation extends MagnoliaNodeStubbingOperation {

    /**
     * Stub the group's title (property name: "title"). No validation is performed.
     * @param title group title value (may be null or blank)
     * @return stubbing operation (returns a UserNodeStubbingOperation for historical API reasons)
     */
    public static GroupNodeStubbingOperation stubTitle(final String title) {
        return new GroupNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                Require.Argument.notNull(node, "node should not be null");
                stubProperty("title", title).of(node);
            }
        };
    }

    /**
     * Stub the group's description (property name: "description").
     * @param description description text (may be null or blank)
     * @return stubbing operation
     */
    public static GroupNodeStubbingOperation stubDescription(final String description) {
        return new GroupNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                Require.Argument.notNull(node, "node should not be null");
                stubProperty("description", description).of(node);
            }
        };
    }

    /**
     * Create (or overwrite) a child node named "groups" listing subgroup references by identifier properties ("00", "01", ...).
     * @param groupNodes subgroup nodes referenced (order preserved for property numbering)
     * @return stubbing operation
     */
    public static GroupNodeStubbingOperation stubGroups(final Node... groupNodes) {
        return new GroupNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                stubNodeReferenceList(node, "groups", groupNodes);
            }
        };
    }

    /**
     * Create (or overwrite) a child node named "roles" listing role references by identifier properties ("00", "01", ...).
     * @param roleNodes role nodes referenced (order preserved for property numbering)
     * @return stubbing operation
     */
    public static GroupNodeStubbingOperation stubRoles(final Node... roleNodes) {
        return new GroupNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                stubNodeReferenceList(node, "roles", roleNodes);
            }
        };
    }
}
