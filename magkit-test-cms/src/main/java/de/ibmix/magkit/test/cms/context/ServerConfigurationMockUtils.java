package de.ibmix.magkit.test.cms.context;

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

import info.magnolia.cms.beans.config.ServerConfiguration;

/**
 * Utility class providing factory and cleanup methods for creating and managing mocked {@link ServerConfiguration} instances
 * within Magnolia tests. The methods abstract common boilerplate for registering mocked components in the underlying
 * Magnolia {@code ComponentProvider} and ensure easy stubbing of desired behavior via {@link ServerConfigurationStubbingOperation}.
 * <p>
 * Typical usage pattern in a unit test:
 * </p>
 * <pre>{@code
 * import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperations.stubServerName;
 * import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperations.stubServerVersion;
 *
 * @Test
 * void myTest() {
 *     ServerConfiguration serverConfiguration = ServerConfigurationMockUtils.mockServerConfiguration(
 *         stubServerName("TEST"),
 *         stubServerVersion("1.0.0-test")
 *     );
 *     // exercise code under test that relies on ServerConfiguration
 *     // assertions ...
 *     ServerConfigurationMockUtils.cleanServerConfiguration(); // optional cleanup if test framework does not isolate components
 * }
 * }</pre>
 * <p>
 * The factory method applies all provided stubbing operations in the order they are passed. Cleanup removes the mocked
 * component so that subsequent tests can register a fresh instance, preventing cross-test interference.
 * </p>
 * <p>
 * Thread-safety: Creating of mocks by {@link ComponentsMockUtils} is backed by ThreadLocal and is thread save.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-01-11
 */
public final class ServerConfigurationMockUtils extends ComponentsMockUtils {

    /**
     * Creates (or retrieves if already mocked) a {@link ServerConfiguration} mock registered in the Magnolia component provider
     * and applies the given stubbing operations to configure its behavior.
     * <p>
     * If multiple stubbing operations are supplied they are executed sequentially, allowing incremental configuration.
     * </p>
     *
     * @param stubbings zero or more {@link ServerConfigurationStubbingOperation} instances defining mock behavior; may be empty
     * @return the mocked and configured {@link ServerConfiguration} instance registered for global lookup
     * @see ServerConfigurationStubbingOperation
     * @see #cleanServerConfiguration()
     * @implNote The underlying mock is created via {@code mockComponentInstance(ServerConfiguration.class)} inherited from {@link ComponentsMockUtils}.
     */
    public static ServerConfiguration mockServerConfiguration(ServerConfigurationStubbingOperation... stubbings) {
        ServerConfiguration config = mockComponentInstance(ServerConfiguration.class);
        for (ServerConfigurationStubbingOperation stubbing : stubbings) {
            stubbing.of(config);
        }
        return config;
    }

    /**
     * Removes any registered mocked {@link ServerConfiguration} from the Magnolia component provider so subsequent tests can
     * start with a clean state. Invoke this in test teardown when component registry state must not leak between tests.
     * <p>
     * Calling this method when no mock is registered is a no-op.
     * </p>
     */
    public static void cleanServerConfiguration() {
        clearComponentProvider(ServerConfiguration.class);
    }

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ServerConfigurationMockUtils() {
    }
}
