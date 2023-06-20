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
import org.junit.Before;
import org.junit.Test;

import static de.ibmix.magkit.mockito.ComponentsMockUtils.getComponentSingleton;
import static de.ibmix.magkit.mockito.ServerConfigurationMockUtils.cleanServerConfiguration;
import static de.ibmix.magkit.mockito.ServerConfigurationMockUtils.mockServerConfiguration;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing ServerConfigurationMockUtils.
 *
 * @author wolf.bubenik
 * @since 25.07.12
 */
public class ServerConfigurationMockUtilsTest {
    @Before
    public void setUp() throws Exception {
        cleanServerConfiguration();
    }

    @Test
    public void testMockServerConfiguration() {
        assertThat(getComponentSingleton(ServerConfiguration.class), nullValue());

        ServerConfigurationStubbingOperation op1 = mock(ServerConfigurationStubbingOperation.class);
        ServerConfigurationStubbingOperation op2 = mock(ServerConfigurationStubbingOperation.class);
        ServerConfiguration configuration = mockServerConfiguration(op1, op2);
        verify(op1, times(1)).of(configuration);
        verify(op2, times(1)).of(configuration);
        assertThat(getComponentSingleton(ServerConfiguration.class), notNullValue());
        assertThat(getComponentSingleton(ServerConfiguration.class), is(configuration));
    }

    @Test
    public void testCleanServerConfiguration() {
        assertThat(getComponentSingleton(ServerConfiguration.class), nullValue());

        ServerConfiguration configuration = mockServerConfiguration();
        assertThat(getComponentSingleton(ServerConfiguration.class), is(configuration));

        cleanServerConfiguration();
        assertThat(getComponentSingleton(ServerConfiguration.class), nullValue());
    }
}
