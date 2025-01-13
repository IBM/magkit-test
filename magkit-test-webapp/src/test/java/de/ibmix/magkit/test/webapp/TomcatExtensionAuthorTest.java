package de.ibmix.magkit.test.webapp;

/*-
 * #%L
 * magkit-test-webapp Magnolia Webapp that runs a TomcatTest
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.ibmix.magkit.test.server.MagnoliaConfigSelector;
import de.ibmix.magkit.test.server.MagnoliaConfigurer;
import de.ibmix.magkit.test.server.MagnoliaTomcatExtension;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.objectfactory.Components;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(Lifecycle.PER_CLASS)
@MagnoliaConfigSelector(magnoliaProfile = "testprofile", magnoliaInstanceType = "author")
@ExtendWith(MagnoliaTomcatExtension.class)
class TomcatExtensionAuthorTest implements MagnoliaConfigurer {

    private static final String TEST_VALUE = "test-value";
    private static final String TEST_PROPERTY = "test-property";

    /**
     * Verify that our {@link MagnoliaConfigSelector} annotation works.
     */
    @Test
    public void testIsAuthorWithProfile() {
        // verify we are an author instance as desired in our MagnoliaConfigSelector.magnoliaInstanceType annotation
        ServerConfiguration serverConfiguration = Components.getComponent(ServerConfiguration.class);
        assertTrue(serverConfiguration.isAdmin());

        // verify Magnolia read our test property from src/main/webapp/WEB-INF/config/testprofile/magnolia_author.properties
        // i.e. using the Magnolia profile we gave in our MagnoliaConfigSelector.magnoliaProfile annotation
        MagnoliaConfigurationProperties magnoliaConfigurationProperties = Components.getComponent(MagnoliaConfigurationProperties.class);
        assertEquals("true", magnoliaConfigurationProperties.getProperty("magkit.test-profile-for-author"));
    }

    @Override
    public Map<String, String> getSystemPropsToSet() {
        return Map.of(TEST_PROPERTY, TEST_VALUE);
    }

    @Test
    public void testCustomProps() {
        assertEquals(TEST_VALUE, System.getProperty(TEST_PROPERTY));
    }
}
