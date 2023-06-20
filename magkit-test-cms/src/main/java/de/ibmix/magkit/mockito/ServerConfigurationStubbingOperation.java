package de.ibmix.magkit.mockito;

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

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * TODO: comment.
 *
 * @author wolf.bubenik
 * @since 11.01.2011
 */
public abstract class ServerConfigurationStubbingOperation {

    public abstract void of(ServerConfiguration config);

    public static ServerConfigurationStubbingOperation stubDefaultExtension(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertNotNull(config);
                when(config.getDefaultExtension()).thenReturn(value);
            }
        };
    }

    public static ServerConfigurationStubbingOperation stubDefaultBaseUrl(final String value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertNotNull(config);
                when(config.getDefaultBaseUrl()).thenReturn(value);
            }
        };
    }

    public static ServerConfigurationStubbingOperation stubIsAdmin(final boolean value) {
        return new ServerConfigurationStubbingOperation() {
            public void of(ServerConfiguration config) {
                assertNotNull(config);
                when(config.isAdmin()).thenReturn(value);
            }
        };
    }
}
