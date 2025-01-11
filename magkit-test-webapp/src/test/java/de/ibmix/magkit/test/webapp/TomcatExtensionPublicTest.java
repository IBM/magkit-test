
package de.ibmix.magkit.test.webapp;
/*-
 * #%L
 * magkit-test-webapp Magnolia Webapp that runs a TomcatTest
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import static io.restassured.RestAssured.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import de.ibmix.magkit.test.server.MagnoliaConfigSelector;
import de.ibmix.magkit.test.server.MagnoliaTomcatExtension;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.objectfactory.Components;
import io.restassured.response.Response;


/**
 * Example test for using {@link MagnoliaTomcatExtension}, with one test using
 * RestAssured requests against running Magnolia, and another showing how to
 * query and test Magnolia configuration registry within the same JVM.
 * @author joerg.frantzius
 */
@MagnoliaConfigSelector(magnoliaProfile = "testprofile", magnoliaInstanceType = "public")
@ExtendWith(MagnoliaTomcatExtension.class)
public class TomcatExtensionPublicTest {

    /**
     * Example of a test using RestAssured against Magnolia running in Tomcat:
     * verify that local Magnolia superuser can login.
     */
    @Test
    public void testLocalSuperuserLogin() throws Exception {
        Response response = get(MagnoliaTomcatExtension.LOCAL_AUTHOR_URL + "/.magnolia/admincentral");
        assertEquals(401, response.getStatusCode());
        String csrfCookie = response.getCookie("csrf");
        assertNotNull(csrfCookie);

        loginLocalSuperuser(MagnoliaTomcatExtension.LOCAL_AUTHOR_URL, csrfCookie).then().statusCode(302);
    }

    private Response loginLocalSuperuser(String address, String csrfCookie) {
        return MagnoliaTomcatExtension.given().cookie("csrf", csrfCookie)
                .formParams(Map.of("mgnlUserId", "superuser", "mgnlUserPSWD", "superuser", "csrf", csrfCookie)).when()
                .post(address + "/.magnolia/admincentral");
    }

    /**
     * Example of a test that accesses the running Magnolia's object registry to
     * verify some configuration is as expected, e.g. as written by some UpdateTask
     */
    @Test
    public void testMagnoliaConfig() {
        CacheModule cacheModule = Components.getComponent(CacheModule.class);
        Default cachePolicy = (Default) cacheModule.getContentCaching("defaultPageCache").getCachePolicy();
        assertNotNull(cachePolicy, "Expected defaultPageCache config to exist!");
    }

    /**
     * Verify that our {@link MagnoliaConfigSelector} annotation works.
     */
    @Test
    public void testIsPublicWithProfile() {
        // verify we are a public instance as desired in our MagnoliaConfigSelector.magnoliaInstanceType annotation
        ServerConfiguration serverConfiguration = Components.getComponent(ServerConfiguration.class);
        assertFalse(serverConfiguration.isAdmin());

        // verify Magnolia read our test property from src/main/webapp/WEB-INF/config/testprofile/magnolia_public.properties
        // i.e. using the Magnolia profile we gave in our MagnoliaConfigSelector.magnoliaProfile annotation
        MagnoliaConfigurationProperties magnoliaConfigurationProperties = Components.getComponent(MagnoliaConfigurationProperties.class);
        assertEquals("true", magnoliaConfigurationProperties.getProperty("magkit.test-profile-for-public"));
    }
}