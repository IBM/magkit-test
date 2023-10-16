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
 * Util class for creating ServerConfiguration mocks.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2011-01-11
 */
public final class ServerConfigurationMockUtils extends ComponentsMockUtils {

    public static ServerConfiguration mockServerConfiguration(ServerConfigurationStubbingOperation... stubbings) {
        ServerConfiguration config = mockComponentInstance(ServerConfiguration.class);
        for (ServerConfigurationStubbingOperation stubbing : stubbings) {
            stubbing.of(config);
        }
        return config;
    }

    public static void cleanServerConfiguration() {
        clearComponentProvider(ServerConfiguration.class);
    }

    private ServerConfigurationMockUtils() {
    }
}
