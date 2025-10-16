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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.getComponentSingleton;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.cleanServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testing ServerConfigurationMockUtils.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2012-07-25
 */
public class ServerConfigurationMockUtilsTest {
    @BeforeEach
    public void setUp() throws Exception {
        cleanServerConfiguration();
    }

    @Test
    public void testMockServerConfiguration() {
        assertNull(getComponentSingleton(ServerConfiguration.class));
        ServerConfigurationStubbingOperation op1 = mock(ServerConfigurationStubbingOperation.class);
        ServerConfigurationStubbingOperation op2 = mock(ServerConfigurationStubbingOperation.class);
        ServerConfiguration configuration = mockServerConfiguration(op1, op2);
        verify(op1, times(1)).of(configuration);
        verify(op2, times(1)).of(configuration);
        assertNotNull(getComponentSingleton(ServerConfiguration.class));
        assertEquals(configuration, getComponentSingleton(ServerConfiguration.class));
    }

    @Test
    public void testCleanServerConfiguration() {
        assertNull(getComponentSingleton(ServerConfiguration.class));
        ServerConfiguration configuration = mockServerConfiguration();
        assertEquals(configuration, getComponentSingleton(ServerConfiguration.class));
        cleanServerConfiguration();
        assertNull(getComponentSingleton(ServerConfiguration.class));
    }
}
