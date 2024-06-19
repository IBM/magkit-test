package de.ibmix.magkit.test.server;

/*-
 * #%L
 * magkit-test-server Maven Module
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import info.magnolia.module.cache.CacheModule;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.objectfactory.Components;

import io.restassured.config.HttpClientConfig;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/**
 * Starts up Tomcat and executes tests that can both verify requests using
 * RestAssured and inspect Magnolia configuration in the running Tomcat. Expects
 * the environment variables MAGNOLIA_LICENSE_KEY and MAGNOLIA_LICENSE_OWNER to
 * contain a valid Magnolia license. EU-Lisa POM allows to run it using "mvn
 * clean integration-test -DtomcatTest=local" and "-DtomcatTest=jenkins".
 * @author Jörg von Frantzius
 */
public class TomcatTest {

    private static final String MAGNOLIA_AUTOUPDATE_CONFIG_PROP = "magnolia.update.auto";
    private static final String MAGNOLIA_JACKRABBIT_CONFIG_PROP = "magnolia.repositories.jackrabbit.config";
    private static final String MAGNOLIA_RESOURCES_DIR_CONFIG_PROP = "magnolia.resources.dir";
    private static final String MAGNOLIA_LOGS_DIR_CONFIG_PROP = "magnolia.logs.dir";
    private static final String MAGNOLIA_REPOSITORIES_HOME_CONFIG_PROP = "magnolia.repositories.home";

    private static final String LOCAL_AUTHOR_URL = "http://127.0.0.1:8080";

    // system property that tells us to set request timeouts to unlimited,
    // so tests can be debugged
    private static final String NO_TEST_REQUEST_TIMEOUT = "de.ibmix.magkit.test.server.noTestRequestTimeout";

    private static final Tomcat TOMCAT = new Tomcat();
    private static StandardContext c_webAppContext;

    public static RequestSpecification given() {
        RequestSpecification given = io.restassured.RestAssured.given();
        // conditionally set request timeout to unlimited to allow for debugging
        if (Boolean.getBoolean(NO_TEST_REQUEST_TIMEOUT)) {
            // if you find a less hacky way of setting the timeout, please apply it,
            // https://stackoverflow.com/a/46913204 simply didn't work for me
            HttpClientConfig noTimeoutConfig = HttpClientConfig.httpClientConfig().setParam("http.socket.timeout", 0);
            ((RequestSpecificationImpl) given).applyHttpClientConfig(noTimeoutConfig);
        }
        return given;
    }

    /**
     * Configure Magnolia as OCP public instance with in-memory H2 DB, log dir in
     * target/magnolia_logs, and make the Atos Lightmodules available.
     *
     * @throws IOException
     */
    private static void configureMagnolia() throws IOException {
        Path currentDir = Paths.get("").toAbsolutePath();
        Path magnoliaDir = currentDir.resolve("target/magnolia");

        // must override log directory
        Path magnoliaLogsDir = magnoliaDir.resolve("logs");
        Files.createDirectories(magnoliaLogsDir);
        System.setProperty(MAGNOLIA_LOGS_DIR_CONFIG_PROP, magnoliaDir.toString());

        // must override repositories directory
        Path magnoliaRepositoriesDir = magnoliaDir.resolve("repositories");
        Files.createDirectories(magnoliaRepositoriesDir);
        System.setProperty(MAGNOLIA_REPOSITORIES_HOME_CONFIG_PROP, magnoliaRepositoriesDir.toString());

        // use in-memory H2 DB, config comes with magnolia-dx-core-webapp overlay
        System.setProperty(MAGNOLIA_JACKRABBIT_CONFIG_PROP, "WEB-INF/config/repo-conf/jackrabbit-memory-search.xml");

        // let Magnolia start up without waiting for user input
        System.setProperty(MAGNOLIA_AUTOUPDATE_CONFIG_PROP, "true");
    }

    @BeforeAll
    public static void startTomcat() throws LifecycleException, IOException {
        String warName = System.getProperty("project.build.finalName");
        if (StringUtils.isEmpty(warName)) {
            throw new RuntimeException("TomcatTest expectation failed: could not find system property project.build.finalName, please configure maven-surefire-plugin to pass this as a system property as shown here: https://maven.apache.org/surefire/maven-surefire-plugin/examples/system-properties.html");
        }
        configureMagnolia();

        // create a new directory for Tomcat
        Path currentDir = Paths.get("").toAbsolutePath();
        Path tomcatDir = currentDir.resolve("target/TomcatTest");
        Files.createDirectories(tomcatDir);

        TOMCAT.setPort(8080);
        TOMCAT.setBaseDir("./target/TomcatTest");
        TOMCAT.getHost().setAutoDeploy(true);
        Path webAppDir = currentDir.resolve("target/" + warName);
        c_webAppContext = (StandardContext) TOMCAT.addWebapp("", webAppDir.toFile().getAbsolutePath());
        // prevent Tomcat from scanning for web fragments in the JVM classpath (i.e. not
        // in the web app classpath),
        // as StandardJarScanner.processManifest() otherwise produces bogus classpath
        // entries for non-existant
        // JARs found in the manifest of vaadin-server
        ((StandardJarScanner) c_webAppContext.getJarScanner()).setScanClassPath(false);
        // make Tomcat use the Surefire classloader, or we get ClassCastExceptions
        // because Magnolia classes
        // loaded by our test use the Surefire classloader, while Magnolia in the webapp
        // uses the
        // Tomcat webapp classloader
        c_webAppContext.setParentClassLoader(TomcatTest.class.getClassLoader());
        c_webAppContext.setDelegate(true);

        TOMCAT.init();
        // must call getConnector(), see https://stackoverflow.com/a/59282431/1245428
        TOMCAT.getConnector();
        TOMCAT.start();

        // run server loop that waits for shutdown command
        CompletableFuture.supplyAsync(() -> {
            TOMCAT.getServer().await();
            return true;
        });
    }

    @AfterAll
    public static void shutdownTomcat() throws LifecycleException {
        TOMCAT.stop();
    }

    /**
     * Example of a test using RestAssured against Magnolia running in Tomcat:
     * verify that local Magnolia superuser can login.
     */
    @Test
    public void testLocalSuperuserLogin() throws Exception {

        Response response = get(LOCAL_AUTHOR_URL + "/.magnolia/admincentral");
        assertEquals(401, response.getStatusCode());
        String csrfCookie = response.getCookie("csrf");
        assertNotNull(csrfCookie);

        loginLocalSuperuser(LOCAL_AUTHOR_URL, csrfCookie).then().statusCode(302);

    }

    private Response loginLocalSuperuser(String address, String csrfCookie) {
        return given().cookie("csrf", csrfCookie)
                .formParams(Map.of("mgnlUserId", "superuser", "mgnlUserPSWD", "superuser", "csrf", csrfCookie)).when()
                .post(address + "/.magnolia/admincentral");
    }

    /**
     * Example of a test that accesses the running Magnolia's object registry to verify some configuration
     * is as expected, e.g. as written by some UpdateTask
     */
    @Test
    public void testMagnoliaConfig() {
        CacheModule cacheModule = Components.getComponent(CacheModule.class);
        Default cachePolicy = (Default) cacheModule.getContentCaching("defaultPageCache").getCachePolicy();
        assertNotNull(cachePolicy, "Expected defaultPageCache config to exist!");
    }


}
