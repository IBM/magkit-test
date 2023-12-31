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

import de.ibmix.magkit.test.StubbingOperation;
import info.magnolia.cms.beans.config.ServerConfiguration;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Utility class that provides factory methods for ServerConfigurationStubbingOperation.
 * Stubbing operations to be used as parameters in ServerConfigurationMockUtils.mockServerConfiguration(...).
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-01-11
 */
public abstract class ServerConfigurationStubbingOperation implements StubbingOperation<ServerConfiguration> {

    public static ServerConfigurationStubbingOperation stubDefaultExtension(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertThat(config, notNullValue());
                when(config.getDefaultExtension()).thenReturn(value);
            }
        };
    }

    public static ServerConfigurationStubbingOperation stubDefaultBaseUrl(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertThat(config, notNullValue());
                when(config.getDefaultBaseUrl()).thenReturn(value);
            }
        };
    }

    public static ServerConfigurationStubbingOperation stubIsAdmin(final boolean value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertThat(config, notNullValue());
                when(config.isAdmin()).thenReturn(value);
            }
        };
    }
}
