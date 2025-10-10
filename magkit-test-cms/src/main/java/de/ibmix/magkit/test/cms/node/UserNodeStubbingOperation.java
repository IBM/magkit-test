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
import javax.jcr.RepositoryException;
import java.util.Calendar;

/**
 * Factory helpers for stubbing Magnolia user node properties and relations in unit tests.
 * <p>
 * A Magnolia user node commonly stores authentication and profile metadata (email, enabled state, password, language,
 * last access timestamps) plus references to groups and roles. The static methods provided here return
 * {@code UserNodeStubbingOperation} instances (subclass of {@link MagnoliaNodeStubbingOperation}) which, when executed via
 * {@link MagnoliaNodeStubbingOperation#of(Node)}, stub the corresponding single property or create reference list child nodes.
 * </p>
 * <p><strong>Provided operations:</strong>
 * <ul>
 *   <li><code>stubEmail</code>: Stubs textual email property <em>email</em> (no syntax validation).</li>
 *   <li><code>stubEnabled</code>: Stubs boolean enabled state <em>enabled</em>.</li>
 *   <li><code>stubFailedLoginAttempts</code>: Stubs numeric counter <em>failedLoginAttempts</em>.</li>
 *   <li><code>stubLanguage</code>: Stubs user language code <em>language</em>.</li>
 *   <li><code>stubLastAccess</code>: Stubs timestamp <em>lastAccess</em>.</li>
 *   <li><code>stubName</code>: Stubs display name <em>name</em>.</li>
 *   <li><code>stubPassword</code>: Stubs password <em>pswd</em> (stored as plain test value).</li>
 *   <li><code>stubTitle</code>: Stubs title <em>title</em>.</li>
 *   <li><code>stubGroups</code>: Creates/overwrites child node <em>groups</em> with numbered identifier properties ("00", "01", ...).</li>
 *   <li><code>stubRoles</code>: Creates/overwrites child node <em>roles</em> with numbered identifier properties.</li>
 * </ul>
 * </p>
 * <p><strong>Reference list semantics:</strong> Group and role relations are represented by a simple child node containing one property per referenced node holding its identifier. No JCR REFERENCE type or referential integrity is enforced.</p>
 * <p><strong>Behavior & validation:</strong>
 * <ul>
 *   <li>No semantic validation (e.g. password policy, email format) is performed; null/blank values are written verbatim.</li>
 *   <li>Target node null checks are performed inside delegated operations (Hamcrest assertions) to fail fast for erroneous test setup.</li>
 * </ul>
 * </p>
 * <p><strong>Example usage:</strong></p>
 * <pre>
 *   Node user = MagnoliaNodeMockUtils.mockUserNode("john",
 *       UserNodeStubbingOperation.stubEmail("john@example.org"),
 *       UserNodeStubbingOperation.stubEnabled(true),
 *       UserNodeStubbingOperation.stubLanguage("en"),
 *       UserNodeStubbingOperation.stubTitle("Editor")
 *   );
 *   // Later add role references
 *   UserNodeStubbingOperation.stubRoles(roleNode1, roleNode2).of(user);
 * </pre>
 * <p><strong>Thread-safety:</strong> Operations are stateless; created mock graphs are not thread-safe and should be isolated per test.</p>
 * <p><strong>Error handling:</strong> Only assertion failures for null target nodes; any {@link RepositoryException} originates from nested stubbing operations if configured to throw.</p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-10
 */
public abstract class UserNodeStubbingOperation extends MagnoliaNodeStubbingOperation {

    /**
     * Stub the user's email address property ("email"). No validation of email syntax is performed.
     * @param email email value (may be null or blank)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubEmail(final String email) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("email", email).of(node);
            }
        };
    }

    /**
     * Stub whether the user is enabled ("enabled").
     * @param enabled true if enabled
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubEnabled(final boolean enabled) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("enabled", enabled).of(node);
            }
        };
    }

    /**
     * Stub the number of failed login attempts ("failedLoginAttempts").
     * @param failedLoginAttempts count value
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubFailedLoginAttempts(final long failedLoginAttempts) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("failedLoginAttempts", failedLoginAttempts).of(node);
            }
        };
    }

    /**
     * Stub the user's preferred language ("language").
     * @param language language code (may be null or blank)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubLanguage(final String language) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("language", language).of(node);
            }
        };
    }

    /**
     * Stub the last access timestamp ("lastAccess").
     * @param lastAccess calendar value (may be null)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubLastAccess(final Calendar lastAccess) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("lastAccess", lastAccess).of(node);
            }
        };
    }

    /**
     * Stub the display name ("name").
     * @param name name value (may be null or blank)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubName(final String name) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("name", name).of(node);
            }
        };
    }

    /**
     * Stub the user's password ("pswd"). The value is stored as plain text; tests should avoid real secrets.
     * @param password password value
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubPassword(final String password) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("pswd", password).of(node);
            }
        };
    }

    /**
     * Stub the user's title ("title").
     * @param title title value (may be null or blank)
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubTitle(final String title) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubProperty("title", title).of(node);
            }
        };
    }

    /**
     * Create (or overwrite) a child node named "groups" listing group references by identifier properties ("00", "01", ...).
     * @param groupNodes group nodes referenced
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubGroups(final Node... groupNodes) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubNodeReferenceList(node, "groups", groupNodes);
            }
        };
    }

    /**
     * Create (or overwrite) a child node named "roles" listing role references by identifier properties ("00", "01", ...).
     * @param roleNodes role nodes referenced
     * @return stubbing operation
     */
    public static UserNodeStubbingOperation stubRoles(final Node... roleNodes) {
        return new UserNodeStubbingOperation() {
            @Override
            public void of(Node node) throws RepositoryException {
                stubNodeReferenceList(node, "roles", roleNodes);
            }
        };
    }
}
