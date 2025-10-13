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

import javax.jcr.Node;

/**
 * Factory helpers for stubbing Magnolia role node properties for unit tests.
 * <p>A Magnolia role node typically defines permissions/ACLs (not covered here) and simple descriptive metadata such as
 * a title and description. These factory methods return {@link UserNodeStubbingOperation} instances (historic reuse of the
 * same operation type) which, when executed via {@link MagnoliaNodeStubbingOperation#of(Node)}, stub the respective properties.</p>
 * <p><strong>Provided operations:</strong></p>
 * <ul>
 *   <li><code>stubTitle</code>: Stubs textual role title property "title".</li>
 *   <li><code>stubDescription</code>: Stubs textual description property "description".</li>
 * </ul>
 * <p><strong>Behavior:</strong></p>
 * <ul>
 *   <li>No validation of supplied values (null / blank accepted and written verbatim).</li>
 *   <li>Fast failure via assertions for null target nodes inside delegated operations.</li>
 * </ul>
 * <p><strong>Example usage:</strong></p>
 * <pre>
 *   Node role = MagnoliaNodeMockUtils.mockRoleNode("editorRole",
 *       RoleNodeStubbingOperation.stubTitle("Editor"),
 *       RoleNodeStubbingOperation.stubDescription("Role granting editorial permissions")
 *   );
 * </pre>
 * <p><strong>Thread-safety:</strong> Operations are stateless; resulting mock graphs are not thread-safe and should be confined to a single test method.</p>
 * <p><strong>Error handling:</strong> Only assertion failures for null target nodes are triggered; repository exceptions propagate if configured elsewhere.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-10
 */
public abstract class RoleNodeStubbingOperation extends MagnoliaNodeStubbingOperation {

    /**
     * Stub the role's title (property name: "title").
     * <p>Null or blank values are allowed and written unchanged.</p>
     *
     * @param title title value (may be null or blank)
     * @return stubbing operation (returns UserNodeStubbingOperation for historic API reasons)
     */
    public static UserNodeStubbingOperation stubTitle(final String title) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                stubProperty("title", title).of(node);
            }
        };
    }

    /**
     * Stub the role's description (property name: "description").
     * <p>Null or blank values are allowed and written unchanged.</p>
     *
     * @param description description text (may be null or blank)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubDescription(final String description) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws javax.jcr.RepositoryException {
                stubProperty("description", description).of(node);
            }
        };
    }
}
