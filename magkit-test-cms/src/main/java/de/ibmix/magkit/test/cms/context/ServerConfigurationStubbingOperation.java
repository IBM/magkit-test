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

import de.ibmix.magkit.assertations.Require;
import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.beans.config.ServerConfiguration;

import static org.mockito.Mockito.when;

/**
 * Factory holder for {@link StubbingOperation} implementations that configure a mocked {@link ServerConfiguration} instance.
 * <p>
 * Each static method returns a lightweight {@link ServerConfigurationStubbingOperation} that applies one specific piece of
 * behavior to a mock created via {@link ServerConfigurationMockUtils#mockServerConfiguration(ServerConfigurationStubbingOperation...)}.
 * Stubbing operations are executed in the order they are passed to the factory method allowing composable configuration.
 * </p>
 * <p>
 * Typical usage in a unit test:
 * </p>
 * <pre>{@code
 * import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
 * import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubIsAdmin;
 *
 * @Test
 * void configureServerConfiguration() {
 *     ServerConfiguration config = ServerConfigurationMockUtils.mockServerConfiguration(
 *         stubDefaultExtension("html"),
 *         stubIsAdmin(false)
 *     );
 *     // exercise code relying on config
 * }
 * }</pre>
 * <p>
 * Thread-safety: These operations are intended for single-threaded test execution manipulating a shared mock instance; they
 * do not provide synchronization. Avoid concurrent invocation on the same mock.
 * </p>
 * <p>
 * Null handling: Unless otherwise stated, string parameters may be {@code null} to simulate an absent value returned from
 * the Magnolia {@link ServerConfiguration} API.
 * </p>
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-01-11
 * @see ServerConfigurationMockUtils
 */
public abstract class ServerConfigurationStubbingOperation implements StubbingOperation<ServerConfiguration> {

    /**
     * Creates a stubbing operation that defines the value returned by {@link ServerConfiguration#getDefaultExtension()}.
     * <p>
     * Use this to simulate different default file extensions configured for the server runtime.
     * </p>
     * <pre>{@code
     * ServerConfigurationMockUtils.mockServerConfiguration(
     *     stubDefaultExtension("html")
     * );
     * }</pre>
     *
     * @param value the extension string to return; may be {@code null} to simulate missing configuration
     * @return a stubbing operation applying the desired default extension
     * @see ServerConfiguration#getDefaultExtension()
     */
    public static ServerConfigurationStubbingOperation stubDefaultExtension(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                Require.Argument.notNull(config, "config should not be null");
                when(config.getDefaultExtension()).thenReturn(value);
            }
        };
    }

    /**
     * Creates a stubbing operation that defines the value returned by {@link ServerConfiguration#getDefaultBaseUrl()}.
     * <p>
     * Use this to emulate different public base URLs for link generation logic.
     * </p>
     * <pre>{@code
     * ServerConfigurationMockUtils.mockServerConfiguration(
     *     stubDefaultBaseUrl("https://example.test")
     * );
     * }</pre>
     *
     * @param value the base URL to return; may be {@code null} for scenarios where no base URL is configured
     * @return a stubbing operation applying the desired default base URL
     * @see ServerConfiguration#getDefaultBaseUrl()
     */
    public static ServerConfigurationStubbingOperation stubDefaultBaseUrl(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                Require.Argument.notNull(config, "config should not be null");
                when(config.getDefaultBaseUrl()).thenReturn(value);
            }
        };
    }

    /**
     * Creates a stubbing operation that defines the value returned by {@link ServerConfiguration#isAdmin()}.
     * <p>
     * Use this to simulate execution context differences between author (admin) and public instances in Magnolia.
     * </p>
     * <pre>{@code
     * ServerConfigurationMockUtils.mockServerConfiguration(
     *     stubIsAdmin(true)
     * );
     * }</pre>
     *
     * @param value {@code true} if the server should be treated as an admin/author instance; {@code false} otherwise
     * @return a stubbing operation applying the desired admin flag
     * @see ServerConfiguration#isAdmin()
     */
    public static ServerConfigurationStubbingOperation stubIsAdmin(final boolean value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                Require.Argument.notNull(config, "config should not be null");
                when(config.isAdmin()).thenReturn(value);
            }
        };
    }
}
