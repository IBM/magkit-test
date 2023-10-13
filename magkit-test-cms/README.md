# Magkit Test CMS (Magnolia)

This project contains a builder API to create mockito mocks of info.magnolia classes and stub their behaviour. 
The mocks are always created with some basic stubbing of a default behaviour.

## Usage
### Maven dependency
```xml
    <dependency>
        <artifactId>magkit-test-cms</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.0.0</version>
    </dependency>
```
It requires magnolia 6.2.19 or later and uses magkit-test-servlet and magkit-test-jcr. 

### Mock a class:
XxxMockUtil classes provide static methods for each class to mock. These mock methods implement a get-or-create pattern: 
- If no Mock has been created before, a new mock will be created.
- If a mock has been created before and exists in the hierarchy of mocks, this existing mock will be returned.
- The complete hierarchy of mocks is stored in a ThreadLocal. This allows tests to be executes in parallel.
- To delete existing mocks within one thread (test) you have to explicitly call ```ContextMockUtils.cleanContext()```

Mocks are stored in the Magnolia MockComponentProvider. We support mocking of any Class as component in
- ComponentsMockUtils: Support injection of mocks by magnolia

Currently, we support mocking of
info.magnolia.context classes
- info.magnolia.context.WebContext
- info.magnolia.context.SystemContext

info.magnolia.cms.i18n classes:
- info.magnolia.cms.i18n.I18nContentSupport

info.magnolia.cms.core classes:
- info.magnolia.cms.core.AggregationState: Always an ExtendedAggregationState

info.magnolia.module classes
- info.magnolia.module.InstallContext
- info.magnolia.module.ModuleRegistry
- info.magnolia.module.model.ModuleDefinition

info.magnolia.cms.beans classes
- info.magnolia.cms.beans.config.ServerConfiguration

info.magnolia.dam classes
- info.magnolia.dam.api.Asset
- info.magnolia.dam.api.AssetProvider
- info.magnolia.dam.api.AssetProviderRegistry
- info.magnolia.dam.jcr.JcrAsset

info.magnolia.cms.security classes
- info.magnolia.cms.security.AccessManager
- info.magnolia.cms.security.SecuritySupport
- info.magnolia.cms.security.UserManager
- info.magnolia.cms.security.User
- info.magnolia.cms.security.Group
- info.magnolia.cms.security.Role

info.magnolia.module.site classes
- info.magnolia.module.site.SiteManager
- info.magnolia.module.site.Site
- info.magnolia.module.site.theme.ThemeReference
- info.magnolia.module.site.theme.Theme
- info.magnolia.module.site.CssResourceDefinition
- info.magnolia.module.site.ResourceDefinition

info.magnolia.rendering.template classes
- info.magnolia.rendering.template.registry.TemplateDefinitionRegistry
- info.magnolia.rendering.template.TemplateDefinition
- info.magnolia.rendering.template.AreaDefinition

Finally, we provide convenience methods for mocking jcr nodes with magnolia NodeTypes and register their JcrSession in the magnolia WebContext.

For each mocked class there is a XxxStubbingOperation class to stub its behaviour.

To keep single test methods independent you must clean up your test context before and / or after each test method:
```java
// Be paranoid and don't rely on others:
@Before
public void setUp() {
    ContextMockUtils.cleanContext();
}

// Be polite and cleanup your mess for others:
@After
public void setUp() {
    ContextMockUtils.cleanContext();
}
```

Usually you are not much interested in Repositories, Workspaces and Sessions, just in Nodes and their Properties. However, whenever you mock a Node for a workspace you get the complete hierarchy of context mocked as well. This makes your test independent of how your code will access the Node and keeps your mocks consistent.

### Mock a class:
We recommend using static imports to keep test code short and readable.

To mock any class that should be injected into another object by magnolia it must be registered at the Guice IoC container used by magnolia. This can be achieved by using the ComponentsMockUtils:
```java
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;

// 1. Create a mock instance of the DamTemplatingFunctions and register it as Magnolia Component:
DamTemplatingFunctions dtf = mockComponentInstance(DamTemplatingFunctions.class);

// Now we can access this DamTemplatingFunctions-mock directly from magnolia Components ...
assertThat(Components.getComponent(DamTemplatingFunctions.class), is(dtf));

// 2. ... or have it injected into another class that is managed by the Magnolia Components:
ServiceWithInjectedDamTemplatingFunctions service = Components.getComponentProvider().newInstance(ServiceWithInjectedDamTemplatingFunctions.class);
assertThat(service.getDtf(), is(dtf));
// Note, that injection only works using the class constructor.
// Field injection is not supported by the Magnolia MockComponentProvider - the common base of both MockUtils.
```

You may create a mock instance directly by using a XxxMockUtil class. These util classses often provide some stubbed default behaviour that may be changed completed by custom stubbing operations.
```java
import static de.ibmix.magkit.test.cms.context.I18nContentSupportMockUtils.mockI18nContentSupport;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;

// 1) Create a I18nContentSupport mock:
I18nContentSupport i18n = mockI18nContentSupport();
// This mock is registered as component:
assertThat(Components.getComponent(I18nContentSupport.class), is(i18n));
// It provides a simple "echo behaviour" ...
assertThat(i18n.isEnabled(), is(false));
assertThat(i18n.toI18NURI("test"), is("test"));
// ... for properties
Node node = mockNode("Paul", stubProperty("property", "test"), stubProperty("property_zh", "test-zh"));
assertThat(i18n.hasProperty(node, "property"), is(true));
assertThat(i18n.getProperty(node, "property"), is(node.getProperty("property")));
assertThat(i18n.getProperty(node, "property", Locale.CHINESE), is(node.getProperty("property_zh")));
```

### Stub methods of mock:
Changing the behaviour of the mocks can be done in two ways:

```java
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubCreated;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubTemplate;
import static org.apache.commons.collections4.IteratorUtils.toList;

// Pass a XxxStubbingOperation to the mockMethod:
Node node = mockPageNode("root/section", stubTemplate("myTemplage"));
assertThat(node.getProperty("mgnl:template").getString(), is("myTemplage"));
assertThat(node.getPrimaryNodeType().getName(), is(NT_PAGE));
assertThat(toList(node.getProperties()).size(), is(2));

// Invoke the XxxStubbingOperation for an existing mock:
Calendar now = Calendar.getInstance();
stubCreated(now).of(node);
assertThat(node.getProperty("mgnl:created").getDate(), is(now));
assertThat(toList(node.getProperties()).size(), is(3));

// (!) Do NOT use the standard Mockito way of stubbing, because this may result in inconsistent behaviour:
// This works...
Property p = mock(Property.class);
doReturn(p).when(node).getProperty("name-3");
assertThat(node.getProperty("name-3"), is(p));
// ...but the last property is missing in the list of node properties:
assertThat(toList(node.getProperties()).size(), is(4))
// fails, property list remains unchanged with only 3 entries.
```

For more examples and details please consult the test cases in the examples package.

## License

This code is published under the Apache2.0 license.

All source files must include a Copyright and License header. The SPDX license header is
preferred because it can be easily scanned.

If you would like to see the detailed LICENSE click [here](../LICENSE).

```text
 * #%L
 * Aperto Mockito Test-Utils - JCR
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
```
## Authors

- Author: Wolf Bubenik - wolf.bubenik@ibm.com

