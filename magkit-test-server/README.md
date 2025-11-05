# Magkit Test Server

This Maven module contains the JUnit5 extension `MagnoliaTomcatExtension`, which allows you to run JUnit tests that can access your running Magnolia webapp both from "outside" e.g. using RestAssured, as well as "from inside" by accessing the Magnolia object registry to verify the configuration is as expected (e.g. for testing ModuleVersionHandlers).

## Usage
### Maven dependency
```xml
    <dependency>
        <artifactId>magkit-test-server</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.0.8</version>
    </dependency>
```
### Using the JUnit5 extension
Add the extension as an annotation to your test like this:

```Java
@MagnoliaConfigSelector(magnoliaProfile = "testprofile", magnoliaInstanceType = "public")
@ExtendWith(MagnoliaTomcatExtension.class)
public class YourPublicTomcatTest {
```

The `MagnoliaTomcatExtension` will examine your test class for a `@MagnoliaConfigSelector` annotation, allowing you to determine whch `MAGNOLIA_(PROFILE|INSTANCE_TYPE|STAGE)` Magnolia will use to configure your webapplication. This allows you to e.g. write separate tests for Author and Public, making use of the particular magnolia.properties files you have in your webapp for this combination, e.g. as in [../magkit-test-webapp/src/main/webapp/WEB-INF/config/testprofile/magnolia_author.properties](../magkit-test-webapp/src/main/webapp/WEB-INF/config/testprofile/magnolia_author.properties)

You can find two examples in the `magkit-test-webapp` module:
* [(../magkit-test-webapp/src/test/java/de/ibmix/magkit/test/webapp/TomcatExtensionPublicTest.java)](../magkit-test-webapp/src/test/java/de/ibmix/magkit/test/webapp/TomcatExtensionPublicTest.java)
* [(../magkit-test-webapp/src/test/java/de/ibmix/magkit/test/webapp/TomcatExtensionAuthorTest.java)](../magkit-test-webapp/src/test/java/de/ibmix/magkit/test/webapp/TomcatExtensionAuthorTest.java)
