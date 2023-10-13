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
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.cleanServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubIsAdmin;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Testing ServerConfigurationStubbingOperation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-25
 */
public class ServerConfigurationStubbingOperationTest {

    private ServerConfiguration _serverConfiguration;

    @Before
    public void setUp() throws Exception {
        cleanServerConfiguration();
        _serverConfiguration = mockServerConfiguration();
    }

    @Test
    public void testStubbDefaultExtension() {
        assertThat(_serverConfiguration.getDefaultExtension(), nullValue());

        stubDefaultExtension("test").of(_serverConfiguration);
        assertThat(_serverConfiguration.getDefaultExtension(), is("test"));
    }

    @Test
    public void testStubbDefaultBaseUrl() {
        assertThat(_serverConfiguration.getDefaultBaseUrl(), nullValue());

        stubDefaultBaseUrl("test.aperto.de").of(_serverConfiguration);
        assertThat(_serverConfiguration.getDefaultBaseUrl(), is("test.aperto.de"));
    }

    @Test
    public void testStubbIsAdmin() {
        assertThat(_serverConfiguration.isAdmin(), is(false));

        stubIsAdmin(true).of(_serverConfiguration);
        assertThat(_serverConfiguration.isAdmin(), is(true));
    }
}
