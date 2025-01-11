package de.ibmix.magkit.test.server;

import info.magnolia.init.MagnoliaConfigurationProperties;
import io.restassured.config.HttpClientConfig;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.specification.RequestSpecification;

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
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.groovy.parser.antlr4.util.StringUtils;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

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
public class MagnoliaTomcatExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

    private static final String EXTENSION_STORE_KEY = MagnoliaTomcatExtension.class.getName();
    public static final String WAR_NAME_SYSTEM_PROPERTY = "project.build.finalName";
    public static final String LOCAL_AUTHOR_URL = "http://127.0.0.1:8080";

    // system property that tells us to set RestAssured request timeouts to
    // unlimited,
    // so tests can be debugged
    public static final String NO_TEST_REQUEST_TIMEOUT_SYSTEM_PROPERTY = "de.ibmix.magkit.test.server.noTestRequestTimeout";

    public enum MagnoliaConfigSelectors {
        MAGNOLIA_PROFILE, MAGNOLIA_INSTANCE_TYPE, MAGNOLIA_STAGE
    }

    /**
     * Some particular Magnolia config properties we care about.
     */
    public enum MagnoliaConfigProps {
        MAGNOLIA_HOME("magnolia.home"),
        MAGNOLIA_UPDATE_AUTO("magnolia.update.auto"),
        MAGNOLIA_REPOSITORIES_JACKRABBIT_CONFIG("magnolia.repositories.jackrabbit.config"),
        MAGNOLIA_LOGS_DIR("magnolia.logs.dir"),
        MAGNOLIA_REPOSITORIES_HOME(MagnoliaConfigurationProperties.MAGNOLIA_REPOSITORIES_HOME);

        private final String _configKey;
        MagnoliaConfigProps(String configKey) {
            _configKey = configKey;
        }

        public String getConfigKey() {
            return _configKey;
        }
    }

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
    protected void configureMagnolia(ExtensionContext context, Path testRunDir) throws IOException {
        setMagnoliaConfigSelector(context);

        // make sure we have a fresh directory with no repo data with each run
        Path magnoliaDir = testRunDir.resolve("magnolia");

        // this determines e.g. the magnolia.cache.startdir , where ehcache keeps
        // serialized cache entries, so we make sure cache will be in an empty directory
        System.setProperty(MagnoliaConfigProps.MAGNOLIA_HOME._configKey, magnoliaDir.toString());

        // must override log directory
        Path magnoliaLogsDir = magnoliaDir.resolve("logs");
        Files.createDirectories(magnoliaLogsDir);
        System.setProperty(MagnoliaConfigProps.MAGNOLIA_LOGS_DIR._configKey, magnoliaDir.toString());

        // must override repositories directory
        Path magnoliaRepositoriesDir = magnoliaDir.resolve("repositories");
        Files.createDirectories(magnoliaRepositoriesDir);
        System.setProperty(MagnoliaConfigProps.MAGNOLIA_REPOSITORIES_HOME._configKey, magnoliaRepositoriesDir.toString());

        // use in-memory H2 DB, config comes with magnolia-dx-core-webapp overlay
        System.setProperty(MagnoliaConfigProps.MAGNOLIA_REPOSITORIES_JACKRABBIT_CONFIG._configKey, "WEB-INF/config/repo-conf/jackrabbit-memory-search.xml");

        // let Magnolia start up without waiting for user input
        System.setProperty(MagnoliaConfigProps.MAGNOLIA_UPDATE_AUTO._configKey, "true");

        // allow test class to override with its own system properties
        setOtherSystemProperties(context);
    }

    protected void setMagnoliaConfigSelector(ExtensionContext context) {
        MagnoliaConfigSelector annotation = context.getTestClass()
            .orElseThrow(() -> new IllegalStateException("Test class not found"))
            .getAnnotation(MagnoliaConfigSelector.class);

        if (annotation != null) {
            if (!StringUtils.isEmpty(annotation.magnoliaProfile())) {
                System.setProperty(MagnoliaConfigSelectors.MAGNOLIA_PROFILE.name(), annotation.magnoliaProfile());
            }
            if (!StringUtils.isEmpty(annotation.magnoliaInstanceType())) {
                System.setProperty(MagnoliaConfigSelectors.MAGNOLIA_INSTANCE_TYPE.name(), annotation.magnoliaInstanceType());
            }
            if (!StringUtils.isEmpty(annotation.magnoliaStage())) {
                System.setProperty(MagnoliaConfigSelectors.MAGNOLIA_STAGE.name(), annotation.magnoliaStage());
            }
        }
    }

    protected void setOtherSystemProperties(ExtensionContext context) {
        Optional<Object> testInstanceOptional = context.getTestInstance();
        if (testInstanceOptional.isPresent()) {
            Object testInstance = testInstanceOptional.get();
            if (testInstance instanceof MagnoliaConfigurer) {
                Map<String, String> systemPropsToSet = ((MagnoliaConfigurer) testInstance).getSystemPropsToSet();
                systemPropsToSet.keySet().stream()
                    .forEachOrdered(k -> System.setProperty(k, systemPropsToSet.get(k)));
            }
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws IOException, LifecycleException, InterruptedException {
        // make ourselves accessible for injection as a parameter to a test method
        getStore(context).put(EXTENSION_STORE_KEY, this);

        if (StringUtils.isEmpty(_warName)) {
            throw new RuntimeException(
                    "TomcatTest expectation failed: given warName is empty, and system property project.build.finalName is also empty, please configure maven-surefire-plugin to pass this as a system property as shown here: https://maven.apache.org/surefire/maven-surefire-plugin/examples/system-properties.html");
        }

        Path currentDir = Paths.get("").toAbsolutePath();
        Path testRunDir = currentDir.resolve("target/" + MagnoliaTomcatExtension.class.getSimpleName() + "-" + System.currentTimeMillis());

        configureMagnolia(context, testRunDir);

        // create a new directory for Tomcat
        Path tomcatDir = testRunDir.resolve("Tomcat");
        Files.createDirectories(tomcatDir);

        _tomcat.setPort(8080);
        // make sure we have a fresh directory for Tomcat (avoid any existing SESSIONS.ser file in there)
        _tomcat.setBaseDir(tomcatDir.toAbsolutePath().toString());
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
        // have the session reaper Thread execute once per second, see ContainerBase.threadStart()
        _tomcat.getEngine().setBackgroundProcessorDelay(1);

        // prepare LifecycleListener that will tell us when Tomcat has come up
        // (i.e. at least has come up to the point where its session reaper thread has started)
        final Boolean[] started = {Boolean.FALSE};
        _tomcat.getServer().addLifecycleListener(new LifecycleListener() {

            @Override
            public void lifecycleEvent(LifecycleEvent event) {
                if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
                    started[0] = true;
                }
            }
        });

        _tomcat.start();
        // asynchronously run server loop that waits for shutdown command
        CompletableFuture.supplyAsync(() -> {
            _tomcat.getServer().await();
            return true;
        });

        // wait for Tomcat to come up
        int waitedMillis = 0;
        int maxWaitMillis = 10000;
        while (!started[0]) {
            Thread.sleep(100);
            waitedMillis = waitedMillis + 100;
            if (waitedMillis > maxWaitMillis) {
                throw new RuntimeException("Tomcat didn't come up after " + waitedMillis + "ms?!");
            }
        }
    }

    private Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.GLOBAL);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(MagnoliaTomcatExtension.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return getStore(extensionContext).get(EXTENSION_STORE_KEY, MagnoliaTomcatExtension.class);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        _tomcat.stop();
        _tomcat.destroy();
    }

    public String getServerUrl() {
        return LOCAL_AUTHOR_URL;
    }

    /**
     * Force all existing sessions to expire after given number of seconds from now.
     */
    public void forceSessionMaxInactiveInterval(int sessionTimeoutSecs) {
        Context context = (Context) _tomcat.getHost().findChildren()[0];
        // see https://github.com/apache/tomcat/blob/main/test/org/apache/catalina/startup/TomcatBaseTest.java#L922
        Session[] sessions = context.getManager().findSessions();
        for (Session session : sessions) {
            session.setMaxInactiveInterval(sessionTimeoutSecs);
        }
    }

    public void awaitSessionTimeout(int sessionTimeoutSecs) {
        try {
            int timeout = sessionTimeoutSecs + _tomcat.getEngine().getBackgroundProcessorDelay() * 2 + /* allow for some weird jitter */ 4;
            TimeUnit.SECONDS.sleep(timeout);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
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
