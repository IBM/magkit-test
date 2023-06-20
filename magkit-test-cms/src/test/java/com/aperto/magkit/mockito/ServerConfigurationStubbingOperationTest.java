package com.aperto.magkit.mockito;

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

import static com.aperto.magkit.mockito.ServerConfigurationMockUtils.cleanServerConfiguration;
import static com.aperto.magkit.mockito.ServerConfigurationMockUtils.mockServerConfiguration;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingOperation.stubDefaultExtension;
import static com.aperto.magkit.mockito.ServerConfigurationStubbingOperation.stubIsAdmin;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Testing ServerConfigurationStubbingOperation.
 *
 * @author wolf.bubenik
 * @since 25.07.12
 */
public class ServerConfigurationStubbingOperationTest {

    private ServerConfiguration _serverConfiguration;

    @Before
    public void setUp() throws Exception {
        cleanServerConfiguration();
        _serverConfiguration = mockServerConfiguration();
    }

    @Test
    public void testStubbDefaultExtension() throws Exception {
        assertThat(_serverConfiguration.getDefaultExtension(), nullValue());

        stubDefaultExtension("test").of(_serverConfiguration);
        assertThat(_serverConfiguration.getDefaultExtension(), is("test"));
    }

    @Test
    public void testStubbDefaultBaseUrl() throws Exception {
        assertThat(_serverConfiguration.getDefaultBaseUrl(), nullValue());

        stubDefaultBaseUrl("test.aperto.de").of(_serverConfiguration);
        assertThat(_serverConfiguration.getDefaultBaseUrl(), is("test.aperto.de"));
    }

    @Test
    public void testStubbIsAdmin() throws Exception {
        assertThat(_serverConfiguration.isAdmin(), is(false));

        stubIsAdmin(true).of(_serverConfiguration);
        assertThat(_serverConfiguration.isAdmin(), is(true));
    }
}
