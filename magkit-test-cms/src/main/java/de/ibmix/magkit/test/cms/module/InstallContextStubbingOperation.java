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

import de.ibmix.magkit.assertations.Require;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Collection of static factory methods producing {@link InstallContextStubbingOperation} instances.
 * <p>
 * An {@code InstallContextStubbingOperation} encapsulates the stubbing of one focused aspect of an {@link InstallContext}
 * Mockito mock. These operations allow composing readable test setups by passing multiple operations into
 * {@link ModuleMockUtils#mockInstallContext(InstallContextStubbingOperation...)}.
 * </p>
 * <h3>Design Goals</h3>
 * <ul>
 *   <li>Provide single-responsibility stubbing operations (each method configures a distinct facet).</li>
 *   <li>Allow fluent and additive configuration without exposing Mockito details in test classes.</li>
 *   <li>Gracefully handle {@link RepositoryException} where relevant by declaring it in the operation contract.</li>
 * </ul>
 * <h3>Typical Usage</h3>
 * <pre>{@code
 * InstallContext ctx = ModuleMockUtils.mockInstallContext(
 *     InstallContextStubbingOperation.stubCurrentModuleDefinition(moduleDefinition),
 *     InstallContextStubbingOperation.stubStatus(InstallStatus.installStarted),
 *     InstallContextStubbingOperation.stubConfigJCRSession(SessionStubbingOperation.stubRootNode("/"))
 * );
 * }</pre>
 * <h3>Thread Safety</h3>
 * Instances returned are stateless lambda-like objects; they can be reused across tests but are not synchronized.
 * <h3>Error Handling</h3>
 * Methods creating sessions or nodes may throw {@link RepositoryException}. Callers passing such operations into the
 * higher-level mock factory should either catch or declare the exception. Operations that do not interact with the JCR
 * layer do not throw checked exceptions.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-25
 */
public abstract class InstallContextStubbingOperation implements ExceptionStubbingOperation<InstallContext, RepositoryException> {

    /**
     * Creates an operation stubbing {@link InstallContext#getCurrentModuleDefinition()}.
     * <p>
     * The provided {@link ModuleDefinition} will be returned whenever the mock's current module definition is requested.
     * Use this to simulate install/update steps for a specific module.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * InstallContext ctx = ModuleMockUtils.mockInstallContext(
     *     InstallContextStubbingOperation.stubCurrentModuleDefinition(myModuleDef)
     * );
     * }</pre>
     *
     * @param md the module definition to return (may be {@code null} if tests require that scenario)
     * @return stubbing operation configuring the current module definition
     */
    public static InstallContextStubbingOperation stubCurrentModuleDefinition(final ModuleDefinition md) {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext ic) {
                Require.Argument.notNull(ic, "installContext should not be null");
                when(ic.getCurrentModuleDefinition()).thenReturn(md);
            }
        };
    }

    /**
     * Creates an operation stubbing retrieval of a JCR {@link Session} for its workspace and optionally the config workspace shortcut.
     * <p>
     * If the provided session belongs to the {@link RepositoryConstants#CONFIG} workspace, the operation also stubs
     * {@link InstallContext#getConfigJCRSession()} to return the same session.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * Session session = SessionMockUtils.mockSession("website");
     * InstallContext ctx = ModuleMockUtils.mockInstallContext(
     *     InstallContextStubbingOperation.stubJcrSession(session)
     * );
     * }</pre>
     *
     * @param session a fully mocked Magnolia JCR session whose workspace name is used for stubbing
     * @return stubbing operation configuring session access
     */
    public static InstallContextStubbingOperation stubJcrSession(final Session session) {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext ic) throws RepositoryException {
                Require.Argument.notNull(ic, "installContext should not be null");
                Require.State.notNull(session, "session should not be null");
                Require.State.notNull(session.getWorkspace(), "session must have a workspace");
                String workspace = session.getWorkspace().getName();
                doReturn(session).when(ic).getJCRSession(workspace);
                if (RepositoryConstants.CONFIG.equals(workspace)) {
                    doReturn(session).when(ic).getConfigJCRSession();
                }
            }
        };
    }

    /**
     * Convenience operation for creating and stubbing a JCR {@link Session} by workspace name.
     * <p>
     * Internally delegates to {@link SessionMockUtils#mockSession(String, SessionStubbingOperation...)} and then
     * {@link #stubJcrSession(Session)}.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * InstallContext ctx = ModuleMockUtils.mockInstallContext(
     *     InstallContextStubbingOperation.stubJcrSession(
     *         "website",
     *         SessionStubbingOperation.stubRootNode("/")
     *     )
     * );
     * }</pre>
     *
     * @param workspace target workspace (e.g. {@code "website"}, {@code RepositoryConstants.CONFIG})
     * @param stubbings optional operations configuring the created session; may be empty
     * @return stubbing operation providing session access to the install context
     * @throws RepositoryException if session creation fails in underlying utilities
     */
    public static InstallContextStubbingOperation stubJcrSession(final String workspace, SessionStubbingOperation... stubbings)  throws RepositoryException {
        Session session = SessionMockUtils.mockSession(workspace, stubbings);
        return stubJcrSession(session);
    }

    /**
     * Convenience operation specifically targeting the {@link RepositoryConstants#CONFIG} workspace.
     * <p>
     * Equivalent to {@code stubJcrSession(RepositoryConstants.CONFIG, stubbings)} and additionally stubs
     * {@link InstallContext#getConfigJCRSession()}.
     * </p>
     *
     * @param stubbings optional operations configuring the config session; may be empty
     * @return stubbing operation providing config session access
     * @throws RepositoryException if session creation fails
     */
    public static InstallContextStubbingOperation stubConfigJCRSession(SessionStubbingOperation... stubbings)  throws RepositoryException {
        Session session = SessionMockUtils.mockSession(RepositoryConstants.CONFIG, stubbings);
        return stubJcrSession(session);
    }

    /**
     * Creates an operation stubbing the install status returned by {@link InstallContext#getStatus()}.
     * <p>
     * Useful for simulating various phases of an installation or update process.
     * </p>
     *
     * @param status the status to be returned (e.g. {@link InstallStatus#installFailed})
     * @return stubbing operation configuring install status
     */
    public static InstallContextStubbingOperation stubStatus(final InstallStatus status) {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext ic) {
                Require.Argument.notNull(ic, "installContext should not be null");
                doReturn(status).when(ic).getStatus();
            }
        };
    }

    /**
     * Creates an operation stubbing presence of the modules root node and its retrieval via {@link InstallContext#getModulesNode()}.
     * <p>
     * Also stubs {@link InstallContext#hasModulesNode()} to return {@code true}.
     * </p>
     *
     * @return stubbing operation configuring modules node access
     */
    public static InstallContextStubbingOperation stubModulesNode() {
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext installContext) throws RepositoryException {
                Require.Argument.notNull(installContext, "installContext should not be null");
                Node modules = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules");
                doReturn(modules).when(installContext).getModulesNode();
                doReturn(true).when(installContext).hasModulesNode();
            }
        };
    }

    /**
     * Creates an operation stubbing access to the current module node via {@link InstallContext#getOrCreateCurrentModuleNode()}.
     * <p>
     * Also marks the module as registered ({@link InstallContext#isModuleRegistered(String)}) and indicates modules node presence.
     * </p>
     *
     * @param moduleName name of the target module; must be non blank
     * @return stubbing operation configuring current module node access
     * @throws IllegalArgumentException if {@code moduleName} is blank
     */
    public static InstallContextStubbingOperation stubGetOrCreateCurrentModuleNode(final String moduleName) {
        Require.Argument.notNull(moduleName, "moduleName should not be null");
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext installContext) throws RepositoryException {
                Require.Argument.notNull(installContext, "installContext should not be null");
                Node module = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules" + '/' + moduleName);
                doReturn(module).when(installContext).getOrCreateCurrentModuleNode();
                doReturn(true).when(installContext).isModuleRegistered(moduleName);
                doReturn(true).when(installContext).hasModulesNode();
            }
        };
    }

    /**
     * Creates an operation stubbing access to the current module configuration node via
     * {@link InstallContext#getOrCreateCurrentModuleConfigNode()}.
     * <p>
     * Also marks the module as registered and the modules node as present.
     * </p>
     *
     * @param moduleName name of the target module; must be non blank
     * @return stubbing operation configuring current module config node access
     * @throws IllegalArgumentException if {@code moduleName} is blank
     */
    public static InstallContextStubbingOperation stubGetOrCreateCurrentModuleConfigNode(final String moduleName) {
        Require.Argument.notNull(moduleName, "moduleName should not be null");
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext installContext) throws RepositoryException {
                Require.Argument.notNull(installContext, "installContext should not be null");
                Node moduleConfig = MagnoliaNodeMockUtils.mockContentNode(RepositoryConstants.CONFIG, "modules" + '/' + moduleName + "/config");
                doReturn(moduleConfig).when(installContext).getOrCreateCurrentModuleConfigNode();
                doReturn(true).when(installContext).isModuleRegistered(moduleName);
                doReturn(true).when(installContext).hasModulesNode();
            }
        };
    }

    /**
     * Creates an operation adding a message to the {@link InstallContext#getMessages()} collection for the specified module.
     * <p>
     * If previous messages for the module exist they are preserved. A new mock {@link InstallContext.Message} is created and populated
     * with the given values.
     * </p>
     * <h4>Example</h4>
     * <pre>{@code
     * InstallContext ctx = ModuleMockUtils.mockInstallContext(
     *     InstallContextStubbingOperation.stubMessage(
     *         "my-module",
     *         "Installation started",
     *         "Initializing repository structures",
     *         new Date(),
     *         InstallContext.MessagePriority.info
     *     )
     * );
     * }</pre>
     *
     * @param moduleName the module key under which the message is stored
     * @param message human readable message text
     * @param details optional detailed description (may be {@code null})
     * @param timestamp timestamp associated with the message (use {@link Date#Date()} for now)
     * @param priority message priority classification
     * @return stubbing operation adding the message
     */
    public static InstallContextStubbingOperation stubMessage(final String moduleName, final String message, final String details, final Date timestamp, final InstallContext.MessagePriority priority) {
        Require.Argument.notNull(moduleName, "moduleName should not be null");
        return new InstallContextStubbingOperation() {
            @Override
            public void of(InstallContext installContext) {
                Require.Argument.notNull(installContext, "installContext should not be null");
                Map<String, List<InstallContext.Message>> messages = installContext.getMessages();
                Require.State.notNull(messages, "messages should not be null");
                List<InstallContext.Message> moduleMessages = messages.getOrDefault(moduleName, new ArrayList<>());
                InstallContext.Message newMessage = mock(InstallContext.Message.class);
                doReturn(message).when(newMessage).getMessage();
                doReturn(details).when(newMessage).getDetails();
                doReturn(timestamp).when(newMessage).getTimestamp();
                doReturn(priority).when(newMessage).getPriority();
                moduleMessages.add(newMessage);
                messages.put(moduleName, moduleMessages);
                doReturn(messages).when(installContext).getMessages();
            }
        };
    }
}
