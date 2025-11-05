
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
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.ibmix.magkit.test.server.MagnoliaConfigSelector;
import de.ibmix.magkit.test.server.MagnoliaTomcatExtension;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.filters.CompositeFilter;
import info.magnolia.cms.filters.ContentTypeFilter;
import info.magnolia.cms.filters.MgnlFilter;
import info.magnolia.init.MagnoliaConfigurationProperties;
import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.CachePolicy;
import info.magnolia.module.cache.ContentCachingConfiguration;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.objectfactory.Components;
import info.magnolia.voting.Voter;
import info.magnolia.voting.voters.URIRegexVoter;
import io.restassured.response.Response;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


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
     * Simple example of a test that accesses the running Magnolia's object registry to
     * verify some configuration is as expected, e.g. as written by some UpdateTask
     */
    @Test
    public void testMagnoliaConfigInComponentRegistry() {
        // as a simple example, we are testing something here that Magnolia configures itself,
        // so this would actually be a test that makes sense for Magnolia itself
        CacheModule cacheModule = Components.getComponent(CacheModule.class);
        ContentCachingConfiguration contentCaching = cacheModule.getContentCaching("defaultPageCache");
        assertNotNull(contentCaching, "Expected 'defaultPageCache' ContentCachingConfiguration to exist");
        CachePolicy cachePolicy = contentCaching.getCachePolicy();
        assertEquals(Default.class, cachePolicy.getClass(),
            "Expected 'defaultCachePolicy' to be have class " + Default.class);
    }

    /**
     * Test for a filter config, including ordering / position in filter chain,
     * and functionality of a Voter.
     */
    @Test
    public void testFilterWithVoterConfig() {
        // do like info.magnolia.cms.filters.MgnlMainFilter.getRootFilter() to obtain filter chain,
        // looks like it is not directly accessible as a Component
        MgnlFilter rootFilter = Components.getComponent(info.magnolia.cms.filters.FilterManager.class).getFilterDispatcher().getTargetFilter();
        // we are making some assumptions on Magnolia's implementation here (not ours);
        // if one such assumption is wrong, make test fail with error that states this
        assertEquals(CompositeFilter.class, rootFilter.getClass(), "Magnolia implementation has changed");
        MgnlFilter[] filters = ((CompositeFilter) rootFilter).getFilters();
        MgnlFilter secondFilter = filters[1];
        assertEquals(ContentTypeFilter.class, secondFilter.getClass(), "Magnolia implementation has changed");
        Voter[] filterBypasses = ((ContentTypeFilter) secondFilter).getBypasses();

        // we can check for an expected voter to be present ...
        Voter firstVoter = filterBypasses[0];
        assertEquals(URIRegexVoter.class, firstVoter.getClass(), "Magnolia implementation has changed");
        // and we can also check whether the voter does what we expect it to do
        // (test with some Vaadin request URL seen in the browser)
        final String someVaadinPush = "http://localhost:8080/.magnolia/admincentral/PUSH"
            + "?v-uiId=0&v-pushId=5d9dd3e8-e6ed-4655-aa1a-3e2d24cd4895&X-Atmosphere-tracking-id=6927c2cc-93d8-4ae6-b302-ce69349c10ce&X-Atmosphere-Framework"
            + "=2.3.2.vaadin2-javascript&X-Atmosphere-Transport=long-polling&X-Atmosphere-TrackMessageSize=true"
            + "&Content-Type=application%2Fjson%3B%20charset%3DUTF-8&X-atmo-protocol=true&_=1748612171401";
        URIRegexVoter uriRegexVoter = (URIRegexVoter) firstVoter;
        assertNotEquals(0, uriRegexVoter.vote(someVaadinPush), "Expected Voter to vote true (!=0) for Vaadin PUSH request");
    }

    /**
     * Verify that our {@link MagnoliaConfigSelector} annotation works.
     */
    @Test
    public void testIsPublicWithProfile() {
        // verify we are a public instance as desired in our MagnoliaConfigSelector.magnoliaInstanceType annotation
        ServerConfiguration serverConfiguration = Components.getComponent(ServerConfiguration.class);
        assertFalse(serverConfiguration.isAdmin());
        assertTrue(isNotEmpty(serverConfiguration.getInstanceUuid()));

        // verify Magnolia read our test property from src/main/webapp/WEB-INF/config/testprofile/magnolia_public.properties
        // i.e. using the Magnolia profile we gave in our MagnoliaConfigSelector.magnoliaProfile annotation
        MagnoliaConfigurationProperties magnoliaConfigurationProperties = Components.getComponent(MagnoliaConfigurationProperties.class);
        assertEquals("true", magnoliaConfigurationProperties.getProperty("magkit.test-profile-for-public"));
    }
}
