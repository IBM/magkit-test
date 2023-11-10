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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    @Before
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _installContext = mock(InstallContext.class);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void stubCurrentModuleDefinition() throws RepositoryException {
        assertThat(_installContext.getCurrentModuleDefinition(), nullValue());

        ModuleDefinition definition = mock(ModuleDefinition.class);
        InstallContextStubbingOperation.stubCurrentModuleDefinition(definition).of(_installContext);
        assertThat(_installContext.getCurrentModuleDefinition(), is(definition));
    }

    @Test
    public void stubJcrSession() throws RepositoryException {
        assertThat(_installContext.getJCRSession("test"), nullValue());
        assertThat(_installContext.getConfigJCRSession(), nullValue());

        SessionStubbingOperation op = mock(SessionStubbingOperation.class);
        InstallContextStubbingOperation.stubJcrSession("test", op).of(_installContext);
        Session testSession = SessionMockUtils.mockSession("test");
        assertThat(_installContext.getJCRSession("test"), notNullValue());
        assertThat(_installContext.getJCRSession("test"), is(testSession));
        assertThat(_installContext.getConfigJCRSession(), nullValue());
        verify(op, times(1)).of(testSession);
    }

    @Test
    public void stubConfigJCRSession() throws RepositoryException {
        assertThat(_installContext.getJCRSession("config"), nullValue());
        assertThat(_installContext.getConfigJCRSession(), nullValue());

        SessionStubbingOperation op = mock(SessionStubbingOperation.class);
        InstallContextStubbingOperation.stubConfigJCRSession(op).of(_installContext);
        Session configSession = SessionMockUtils.mockSession("config");
        assertThat(_installContext.getJCRSession("config"), notNullValue());
        assertThat(_installContext.getJCRSession("config"), is(configSession));
        assertThat(_installContext.getConfigJCRSession(), notNullValue());
        assertThat(_installContext.getConfigJCRSession(), is(configSession));
        verify(op, times(1)).of(configSession);
    }

    @Test
    public void stubStatus() throws RepositoryException {
        assertThat(_installContext.getStatus(), nullValue());
        InstallContextStubbingOperation.stubStatus(InstallStatus.installFailed).of(_installContext);
        assertThat(_installContext.getStatus(), is(InstallStatus.installFailed));
    }

    @Test
    public void stubModulesNode() throws RepositoryException {
        assertThat(_installContext.getModulesNode(), nullValue());
        assertFalse(_installContext.hasModulesNode());

        InstallContextStubbingOperation.stubModulesNode().of(_installContext);
        assertTrue(_installContext.hasModulesNode());
        assertThat(_installContext.getModulesNode(), is(NodeMockUtils.mockNode("config", "modules")));
        assertThat(_installContext.getModulesNode().getPrimaryNodeType().getName(), is(NodeTypes.Content.NAME));
    }

    @Test
    public void stubGetOrCreateCurrentModuleNode() throws RepositoryException {
        assertThat(_installContext.getOrCreateCurrentModuleNode(), nullValue());
        assertFalse(_installContext.isModuleRegistered("test"));

        InstallContextStubbingOperation.stubGetOrCreateCurrentModuleNode("test").of(_installContext);
        assertThat(_installContext.getOrCreateCurrentModuleNode(), is(NodeMockUtils.mockNode("config", "/modules/test")));
        assertThat(_installContext.getOrCreateCurrentModuleNode().getPrimaryNodeType().getName(), is(NodeTypes.Content.NAME));
        assertTrue(_installContext.isModuleRegistered("test"));
    }

    @Test
    public void stubGetOrCreateCurrentModuleConfigNode() throws RepositoryException {
        assertThat(_installContext.getOrCreateCurrentModuleConfigNode(), nullValue());
        assertFalse(_installContext.isModuleRegistered("test"));

        InstallContextStubbingOperation.stubGetOrCreateCurrentModuleConfigNode("test").of(_installContext);
        assertThat(_installContext.getOrCreateCurrentModuleConfigNode(), is(NodeMockUtils.mockNode("config", "/modules/test/config")));
        assertThat(_installContext.getOrCreateCurrentModuleConfigNode().getPrimaryNodeType().getName(), is(NodeTypes.Content.NAME));
        assertTrue(_installContext.isModuleRegistered("test"));
    }
}
