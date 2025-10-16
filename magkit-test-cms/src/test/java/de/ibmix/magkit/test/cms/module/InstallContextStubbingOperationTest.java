package de.ibmix.magkit.test.cms.module;

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
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
import de.ibmix.magkit.test.jcr.SessionStubbingOperation;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallStatus;
import info.magnolia.module.model.ModuleDefinition;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test InstallContextStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-11-10
 */
public class InstallContextStubbingOperationTest {

    private InstallContext _installContext;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _installContext = mock(InstallContext.class);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubCurrentModuleDefinition() throws RepositoryException {
        assertNull(_installContext.getCurrentModuleDefinition());

        ModuleDefinition definition = mock(ModuleDefinition.class);
        InstallContextStubbingOperation.stubCurrentModuleDefinition(definition).of(_installContext);
        assertEquals(definition, _installContext.getCurrentModuleDefinition());
    }

    @Test
    public void stubJcrSession() throws RepositoryException {
        assertNull(_installContext.getJCRSession("test"));
        assertNull(_installContext.getConfigJCRSession());

        SessionStubbingOperation op = mock(SessionStubbingOperation.class);
        InstallContextStubbingOperation.stubJcrSession("test", op).of(_installContext);
        Session testSession = SessionMockUtils.mockSession("test");
        assertNotNull(_installContext.getJCRSession("test"));
        assertEquals(testSession, _installContext.getJCRSession("test"));
        assertNull(_installContext.getConfigJCRSession());
        verify(op, times(1)).of(testSession);
    }

    @Test
    public void stubConfigJCRSession() throws RepositoryException {
        assertNull(_installContext.getJCRSession("config"));
        assertNull(_installContext.getConfigJCRSession());

        SessionStubbingOperation op = mock(SessionStubbingOperation.class);
        InstallContextStubbingOperation.stubConfigJCRSession(op).of(_installContext);
        Session configSession = SessionMockUtils.mockSession("config");
        assertNotNull(_installContext.getJCRSession("config"));
        assertEquals(configSession, _installContext.getJCRSession("config"));
        assertNotNull(_installContext.getConfigJCRSession());
        assertEquals(configSession, _installContext.getConfigJCRSession());
        verify(op, times(1)).of(configSession);
    }

    @Test
    public void stubStatus() throws RepositoryException {
        assertNull(_installContext.getStatus());
        InstallContextStubbingOperation.stubStatus(InstallStatus.installFailed).of(_installContext);
        assertEquals(InstallStatus.installFailed, _installContext.getStatus());
    }

    @Test
    public void stubModulesNode() throws RepositoryException {
        assertNull(_installContext.getModulesNode());
        assertFalse(_installContext.hasModulesNode());

        InstallContextStubbingOperation.stubModulesNode().of(_installContext);
        assertTrue(_installContext.hasModulesNode());
        assertEquals(NodeMockUtils.mockNode("config", "modules"), _installContext.getModulesNode());
        assertEquals(NodeTypes.Content.NAME, _installContext.getModulesNode().getPrimaryNodeType().getName());
    }

    @Test
    public void stubGetOrCreateCurrentModuleNode() throws RepositoryException {
        assertNull(_installContext.getOrCreateCurrentModuleNode());
        assertFalse(_installContext.isModuleRegistered("test"));

        InstallContextStubbingOperation.stubGetOrCreateCurrentModuleNode("test").of(_installContext);
        assertEquals(NodeMockUtils.mockNode("config", "/modules/test"), _installContext.getOrCreateCurrentModuleNode());
        assertEquals(NodeTypes.Content.NAME, _installContext.getOrCreateCurrentModuleNode().getPrimaryNodeType().getName());
        assertTrue(_installContext.isModuleRegistered("test"));
    }

    @Test
    public void stubGetOrCreateCurrentModuleConfigNode() throws RepositoryException {
        assertNull(_installContext.getOrCreateCurrentModuleConfigNode());
        assertFalse(_installContext.isModuleRegistered("test"));

        InstallContextStubbingOperation.stubGetOrCreateCurrentModuleConfigNode("test").of(_installContext);
        assertEquals(NodeMockUtils.mockNode("config", "/modules/test/config"), _installContext.getOrCreateCurrentModuleConfigNode());
        assertEquals(NodeTypes.Content.NAME, _installContext.getOrCreateCurrentModuleConfigNode().getPrimaryNodeType().getName());
        assertTrue(_installContext.isModuleRegistered("test"));
    }

    @Test
    public void stubMessage() throws RepositoryException {
        assertTrue(_installContext.getMessages().isEmpty());
        assertNull(_installContext.getMessages().get("test"));

        Date now = Calendar.getInstance().getTime();
        InstallContextStubbingOperation.stubMessage("test", "message-1", "detail", now, InstallContext.MessagePriority.info).of(_installContext);
        InstallContextStubbingOperation.stubMessage("test", "message-2", null, null, InstallContext.MessagePriority.info).of(_installContext);
        InstallContextStubbingOperation.stubMessage("other", "other message", null, null, InstallContext.MessagePriority.info).of(_installContext);
        assertEquals(2, _installContext.getMessages().size());
        assertEquals(2, _installContext.getMessages().get("test").size());
        assertEquals("message-1", _installContext.getMessages().get("test").get(0).getMessage());
        assertEquals("detail", _installContext.getMessages().get("test").get(0).getDetails());
        assertEquals(now, _installContext.getMessages().get("test").get(0).getTimestamp());
        assertEquals(InstallContext.MessagePriority.info, _installContext.getMessages().get("test").get(0).getPriority());
        assertEquals("message-2", _installContext.getMessages().get("test").get(1).getMessage());
        assertEquals("other message", _installContext.getMessages().get("other").get(0).getMessage());
    }
}
