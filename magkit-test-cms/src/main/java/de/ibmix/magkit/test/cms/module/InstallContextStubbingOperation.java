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

import de.ibmix.magkit.test.ExceptionStubbingOperation;
import de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils;
import de.ibmix.magkit.test.jcr.SessionMockUtils;
import de.ibmix.magkit.test.jcr.SessionStubbingOperation;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallStatus;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.repository.RepositoryConstants;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for InstallContextStubbingOperation.
 * Stubbing operations to be used as parameters in InstallContextMockUtils.mockInstallContext(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-25
 */
public abstract class InstallContextStubbingOperation implements ExceptionStubbingOperation<InstallContext, RepositoryException> {

    public static InstallContextStubbingOperation stubCurrentModuleDefinition(final ModuleDefinition md) {
        return new InstallContextStubbingOperation() {

            @Override
            public void of(InstallContext ic) {
                assertThat(ic, notNullValue());
                when(ic.getCurrentModuleDefinition()).thenReturn(md);
            }
        };
    }

    public static InstallContextStubbingOperation stubJcrSession(final Session session) {
        return new InstallContextStubbingOperation() {

            @Override
            public void of(InstallContext ic) throws RepositoryException {
                assertThat(ic, notNullValue());
                assertThat(session, notNullValue());
                assertThat(session.getWorkspace(), notNullValue());
                String workspace = session.getWorkspace().getName();
                doReturn(session).when(ic).getJCRSession(workspace);
                if (RepositoryConstants.CONFIG.equals(workspace)) {
                    doReturn(session).when(ic).getConfigJCRSession();
                }
            }
        };
    }

    public static InstallContextStubbingOperation stubJcrSession(final String workspace, SessionStubbingOperation... stubbings)  throws RepositoryException {
        Session session = SessionMockUtils.mockSession(workspace, stubbings);
        return stubJcrSession(session);
    }

    public static InstallContextStubbingOperation stubConfigJCRSession(SessionStubbingOperation... stubbings)  throws RepositoryException {
        Session session = SessionMockUtils.mockSession(RepositoryConstants.CONFIG, stubbings);
        return stubJcrSession(session);
    }

    public static InstallContextStubbingOperation stubStatus(final InstallStatus status) {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                doReturn(status).when(mock).getStatus();
            }
        };
    }

    public static InstallContextStubbingOperation stubModulesNode() {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                Node modules = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules");
                doReturn(modules).when(mock).getModulesNode();
                doReturn(true).when(mock).hasModulesNode();
            }
        };
    }

    public static InstallContextStubbingOperation stubGetOrCreateCurrentModuleNode(final String moduleName) {
        assertTrue(isNotBlank(moduleName));
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                Node module = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules" + '/' + moduleName);
                doReturn(module).when(mock).getOrCreateCurrentModuleNode();
                doReturn(true).when(mock).isModuleRegistered(moduleName);
                doReturn(true).when(mock).hasModulesNode();
            }
        };
    }

    public static InstallContextStubbingOperation stubGetOrCreateCurrentModuleConfigNode(final String moduleName) {
        assertTrue(isNotBlank(moduleName));
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext mock) throws RepositoryException {
                assertThat(mock, notNullValue());
                Node moduleConfig = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules" + '/' + moduleName + "/config");
                doReturn(moduleConfig).when(mock).getOrCreateCurrentModuleConfigNode();
                doReturn(true).when(mock).isModuleRegistered(moduleName);
                doReturn(true).when(mock).hasModulesNode();
            }
        };
    }

    public static InstallContextStubbingOperation stubMessage(final String moduleName, final String message, final String details, final Date timestamp, final InstallContext.MessagePriority priority) {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext mock) throws RepositoryException {
                Map<String, List<InstallContext.Message>> messages = mock.getMessages();
                List<InstallContext.Message> moduleMessages = messages.get(moduleName);
                InstallContext.Message newMessage = mock(InstallContext.Message.class);
                doReturn(message).when(newMessage).getMessage();
                doReturn(details).when(newMessage).getDetails();
                doReturn(timestamp).when(newMessage).getTimestamp();
                doReturn(priority).when(newMessage).getPriority();
                moduleMessages.add(newMessage);
                messages.put(moduleName, moduleMessages);
                doReturn(messages).when(mock).getMessages();
            }
        };
    }
}
