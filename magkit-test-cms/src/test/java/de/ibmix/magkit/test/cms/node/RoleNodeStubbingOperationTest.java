package de.ibmix.magkit.test.cms.node;

/*-
 * #%L
 * magkit-test-cms Magnolia Module
 * %%
 * Copyright (C) 2025 IBM iX
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockRoleNode;
import static de.ibmix.magkit.test.cms.node.RoleNodeStubbingOperation.stubDescription;
import static de.ibmix.magkit.test.cms.node.RoleNodeStubbingOperation.stubTitle;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link RoleNodeStubbingOperation} covering stubbing of title and description including null / overwrite scenarios.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-13
 */
public class RoleNodeStubbingOperationTest {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubTitleAndDescription() throws RepositoryException {
        Node role = mockRoleNode("editor-role", stubTitle("Editor"), stubDescription("Grants editorial permissions"));
        assertNotNull(role);
        assertTrue(role.hasProperty("title"));
        assertEquals("Editor", role.getProperty("title").getString());
        assertTrue(role.hasProperty("description"));
        assertEquals("Grants editorial permissions", role.getProperty("description").getString());
    }

    @Test
    public void stubTitleNullValue() throws RepositoryException {
        Node role = mockRoleNode("null-title-role", stubTitle(null));
        assertTrue(role.hasProperty("title"));
        assertNull(role.getProperty("title").getString(), "Null title should produce a property with null string value");
    }

    @Test
    public void overwriteTitleWithNull() throws RepositoryException {
        Node role = mockRoleNode("overwrite-role", stubTitle("Initial"), stubTitle(null));
        assertTrue(role.hasProperty("title"));
        assertNull(role.getProperty("title").getString());
    }

    @Test
    public void stubDescriptionNullBlankAndOverwrite() throws RepositoryException {
        Node role = mockRoleNode("desc-role", stubDescription("  "), stubDescription(null));
        assertTrue(role.hasProperty("description"));
        assertNull(role.getProperty("description").getString());
    }
}
