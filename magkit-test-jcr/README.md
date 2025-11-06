# Magkit Test JCR

**A comprehensive JCR mocking framework for unit testing with Mockito.**

This module provides a powerful builder API for creating Mockito mocks of `javax.jcr` classes with pre-configured, realistic behaviors. It simplifies JCR unit testing by offering factory methods that create interconnected mock hierarchies, eliminating the boilerplate code typically required for JCR testing setup.

## Key Features

- **Complete JCR Mock Coverage**: Support for Repository, Session, Workspace, Node, Property, Value, Query, QueryManager, and QueryResult
- **Hierarchical Mock Management**: Automatically creates and manages parent-child relationships between JCR objects
- **Thread-Safe Testing**: Uses ThreadLocal storage to enable parallel test execution
- **Stubbing Operations**: Flexible configuration through dedicated stubbing operation classes
- **Get-or-Create Pattern**: Reuses existing mocks within the same test thread to maintain consistency

## Overview

This project contains a builder API to create mockito mocks of javax.jcr classes and stub their behaviour. 
The mocks are always created with some basic subbing of a default behaviour.

## Usage
### Maven dependency
```xml
    <dependency>
        <artifactId>magkit-test-jcr</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.1.0</version>
    </dependency>
```
It requires javax.jcr 2.0. 

### Mock a class:
XxxMockUtil classes provide static methods for each class to mock. These mock methods implement a get-or-create pattern: 
- If no Mock has been created before, a new mock will be created.
- If a mock has been created before and exists in the hierarchy of mocks, this existing mock will be returned.
- The complete hierarchy of mocks is stored in a ThreadLocal. This allows tests to be executes in parallel.
- To delete existing mocks within one thread (test) you have to explicitly call ```RepositoryMockUtils.cleanRepository()```

We support mocking of

- javax.jcr.Repository: Always mocked once per java session
- javax.jcr.Workspace
- javax.jcr.Query, QueryManager and QueryResult
- javax.jcr.Session: When mocked for a workspace, a Workspace and a Repository will be mocked as well
- javax.jcr.Node: When mocked for a workspace, a Session, Workspace and Repository is mocked as well
- javax.jcr.Property
- javax.jcr.Value


For each mocked class there is a XxxStubbingOperation class to stub its behaviour.

To keep single test methods independent you must clean up your test context before and / or after each test method:
```java
// Be paranoid and don't rely on others:

public void setUp() {
    SessionMockUtils.cleanSession();
    // Note, that this is equivalent to calling RepositoryMockUtils.cleanRepository()
}

// Be polite and cleanup your mess for others:
@After
public void setTearDown() {
    SessionMockUtils.cleanSession();
}
```

Usually you are not much interested in Repositories, Workspaces and Sessions, just in Nodes and their Properties. However, whenever you mock a Node for a workspace you get the complete hierarchy of context mocked as well. This makes your test independent of how your code will access the Node and keeps your mocks consistent.

We recommend using static imports to keep test code short and readable.
```java
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
    
Node section = mockNode("root/section", stubProperty("propName", "value"));
// Note that this is equivalent to:
section = mockNode("website", "/root/section", stubProperty("propName", "value")); 

// What you get is a hierarchy of nodes...
assertThat(section.getPath(), is("/root/section"));
assertThat(section.getDepth(), is(2));
assertThat(section.parent().getDepth(), is(1));
assertThat(section.parent().getParent().getDepth(), is(0));
// ... with default NodeType
assertThat(section.isNodeType(), is(NodeType.NT_BASE))
// ... and the stubbed property value
assertThat(section.hasProperty("propName"), is(true));
assertThat(section.getProperty("propName").getString(), is("value"));
assertThat(section.getProperty("propName").getValue().getString(), is("value"));
assertThat(section.getProperty("propName").isMultiple(), is(false));
// ...in a session workspace
assertThat(section.getSession(), notNullValue());
assertThat(section.getSession().getWorkspace().getName(), is("website"));
// and that can be accessed via the repository or session:
assertThat(RepositoryMockUtils.mockRepository().login().getNode("/root/section"), is(section));
```

### Stub methods of mock:
Changing the behaviour of the mocks can be done in two ways:

```java
// Pass a XxxStubbingOperation to the mockMethod:
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.apache.commons.collections4.IteratorUtils.toList;

Node node = mockNode("root/section", stubProperty("propName", "value"));
assertThat(node.getProperty("name").getString(), is("value"));
assertThat(toList(node.getProperties()).size(), is(2));

// Invoke the XxxStubbingOperation for an existing mock:
stubProperty("name-2", "value-2").of(node);
assertThat(node.getProperty("name-2").getString(), is("value-2"));
assertThat(toList(node.getProperties()).size(), is(3));

// (!) Do NOT use the standard Mockito way of stubbing, because this may result in inconsistent behaviour:
// This works...
Property p = mock(Property.class);
doReturn(p).when(node).getProperty("name-3");
assertThat(node.getProperty("name-3"), is(p));
// ...but the last property is missing in the list of node properties:
assertThat(toList(node.getProperties()).size(), is(3))
// (should be 4)
```

For more examples and details please consult the test cases.

## NodeMockUtils

`NodeMockUtils` provides concise factory methods to obtain Mockito-based JCR `Node` mocks including their surrounding session/workspace context.

Key features:
- Get-or-create semantics per absolute path (idempotent mocking)
- Automatic creation of intermediate path segments
- Fluent stubbing via `NodeStubbingOperation` (e.g. `stubProperty`, `stubType`, `stubNode`)
- Simple XML import support (`mockNodeFromXml`) to bootstrap larger hierarchies

Basic examples:
```java
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;

Node section = mockNode("root/section", stubProperty("title", "Section Title"));
Node page = mockNode("root/section/page", stubProperty("title", "Page Title"));
```
XML import:
```java
try (InputStream in = getClass().getResourceAsStream("/export.xml")) {
    Node imported = NodeMockUtils.mockNodeFromXml("website", in);
    // assertions against imported hierarchy
}
```
Handle normalization ensures both `"root/section"` and `"/root/section"` resolve to the same mock. Use `SessionMockUtils.cleanSession()` between tests to isolate state.

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
