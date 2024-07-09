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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import io.restassured.config.HttpClientConfig;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.specification.RequestSpecification;

/**
 * Starts up Tomcat with a given Magnolia webapp. This allows to execute tests
 * that verify requests using RestAssured and at the same time inspect Magnolia
 * configuration inside the running Tomcat, in order to test e.g. custom
 * {@link info.magnolia.module.delta.Task}s.
 * <p>
 * The example webapp POM in magkit-testwebapp shows how to run it using "mvn
 * integration-test -DtomcatTest". Test must be run using special
 * maven-surefire-plugin configuration, please see pom.xml of the example
 * magkit-test-webapp.
 * <p>
 * Expects the WAR file or folder name given as system property
 * {@link MagnoliaTomcatExtension#WAR_NAME_SYSTEM_PROPERTY}, or as argument to
 * {@link Builder#setWarName(String)}, and expects that file or folder to exist
 * in the "target" folder of the current directory.
 * 
 * @author joerg.frantzius
 */
public class MagnoliaTomcatExtension implements BeforeAllCallback, AfterAllCallback {

    public static final String WAR_NAME_SYSTEM_PROPERTY = "project.build.finalName";
    public static final String LOCAL_AUTHOR_URL = "http://127.0.0.1:8080";

    // system property that tells us to set RestAssured request timeouts to
    // unlimited,
    // so tests can be debugged
    public static final String NO_TEST_REQUEST_TIMEOUT_SYSTEM_PROPERTY = "de.ibmix.magkit.test.server.noTestRequestTimeout";

    private static final String MAGNOLIA_AUTOUPDATE_CONFIG_PROP = "magnolia.update.auto";
    private static final String MAGNOLIA_JACKRABBIT_CONFIG_PROP = "magnolia.repositories.jackrabbit.config";
    private static final String MAGNOLIA_LOGS_DIR_CONFIG_PROP = "magnolia.logs.dir";
    private static final String MAGNOLIA_REPOSITORIES_HOME_CONFIG_PROP = "magnolia.repositories.home";

    private final Tomcat _tomcat = new Tomcat();
    private StandardContext _webAppContext;
    private String _warName = System.getProperty(WAR_NAME_SYSTEM_PROPERTY);

    /**
     * Use this instead of RestAssured.given() in order to be able to debug
     * RestAssured tests without timeout, by setting system property
     * {@link #NO_TEST_REQUEST_TIMEOUT_SYSTEM_PROPERTY}.
     */
    public static RequestSpecification given() {
        RequestSpecification given = io.restassured.RestAssured.given();
        // conditionally set request timeout to unlimited to allow for debugging
        if (Boolean.getBoolean(NO_TEST_REQUEST_TIMEOUT_SYSTEM_PROPERTY)) {
            // if you find a less hacky way of setting the timeout, please apply it,
            // https://stackoverflow.com/a/46913204 simply didn't work for me
            HttpClientConfig noTimeoutConfig = HttpClientConfig.httpClientConfig().setParam("http.socket.timeout", 0);
            ((RequestSpecificationImpl) given).applyHttpClientConfig(noTimeoutConfig);
        }
        return given;
    }

    /**
     * Configure Magnolia for proper logging and with H2 in-memory DB.
     *
     * @throws IOException
     */
    private void configureMagnolia() throws IOException {
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

    @Override
    public void beforeAll(ExtensionContext context) throws IOException, LifecycleException {
        if (StringUtils.isEmpty(_warName)) {
            throw new RuntimeException(
                    "TomcatTest expectation failed: given warName is empty, and system property project.build.finalName is also empty, please configure maven-surefire-plugin to pass this as a system property as shown here: https://maven.apache.org/surefire/maven-surefire-plugin/examples/system-properties.html");
        }
        configureMagnolia();

        // create a new directory for Tomcat
        Path currentDir = Paths.get("").toAbsolutePath();
        Path tomcatDir = currentDir.resolve("target/TomcatTest");
        Files.createDirectories(tomcatDir);

        _tomcat.setPort(8080);
        _tomcat.setBaseDir("./target/TomcatTest");
        _tomcat.getHost().setAutoDeploy(true);
        Path webAppDir = currentDir.resolve("target/" + _warName);
        _webAppContext = (StandardContext) _tomcat.addWebapp("", webAppDir.toFile().getAbsolutePath());
        // prevent Tomcat from scanning for web fragments in the JVM classpath (i.e. not
        // in the web app classpath),
        // as StandardJarScanner.processManifest() otherwise produces bogus classpath
        // entries for non-existant
        // JARs found in the manifest of vaadin-server
        ((StandardJarScanner) _webAppContext.getJarScanner()).setScanClassPath(false);
        // make Tomcat use the Surefire classloader, or we get ClassCastExceptions
        // because Magnolia classes
        // loaded by our test use the Surefire classloader, while Magnolia in the webapp
        // uses the
        // Tomcat webapp classloader
        _webAppContext.setParentClassLoader(MagnoliaTomcatExtension.class.getClassLoader());
        _webAppContext.setDelegate(true);

        _tomcat.init();
        // must call getConnector(), see https://stackoverflow.com/a/59282431/1245428
        _tomcat.getConnector();
        _tomcat.start();

        // run server loop that waits for shutdown command
        CompletableFuture.supplyAsync(() -> {
            _tomcat.getServer().await();
            return true;
        });
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        _tomcat.stop();
    }

    public String getServerUrl() {
        return LOCAL_AUTHOR_URL;
    }

    /**
     * Provides Builder pattern as in
     * https://github.com/junit-team/junit5/blob/main/documentation/src/main/java/example/registration/WebServerExtension.java
     * .
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String _warName;

        /**
         * Set the name of the WAR file / folder to be deployed, which is expected to
         * exist within the "target" folder of the current directory. If it is not set
         * here, {@link MagnoliaTomcatExtension} expects a system propery
         */
        public Builder setWarName(String warNameArg) {
            if (StringUtils.isEmpty(warNameArg)) {
                throw new IllegalArgumentException("warName must not be empty");
            }
            _warName = warNameArg;
            return this;
        }

        public MagnoliaTomcatExtension build() {
            MagnoliaTomcatExtension tomcatExtension = new MagnoliaTomcatExtension();
            if (_warName != null) {
                tomcatExtension._warName = _warName;
            }
            return tomcatExtension;
        }

    }

}
