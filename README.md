# Magkit Test

[![build-module](https://github.com/IBM/magkit-test/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-test/actions/workflows/build.yaml)

## Scope

Multi module project contains Java test libraries to provider test and Mockito mock helpers.
1. magkit-test-assert - for argument and state validation 
2. magkit-test-jcr - for JCR mocking
2. magkit-test-servlet - for servlet container mocking
3. magkit-test-cms - for Magnolia CMS mocking and testing
4. [magkit-test-server](./magkit-test-server/README.md) - for running JUnit tests within a Tomcat running our webapp

## Usage

This repository contains some example best practices for open source repositories:

* [LICENSE](LICENSE)
* [README.md](README.md)
* [CONTRIBUTING.md](CONTRIBUTING.md)
* [MAINTAINERS.md](MAINTAINERS.md)
<!-- A Changelog allows you to track major changes and things that happen, https://github.com/github-changelog-generator/github-changelog-generator can help automate the process -->
* [CHANGELOG.md](CHANGELOG.md)

### Issue tracking

Issues are tracked at [GitHub](https://github.com/IBM/magkit-test/issues).

Any bug reports, improvement or feature pull requests are very welcome!
Make sure your patches are well tested. Ideally create a topic branch for every separate change you make.
For example:

1. Fork the repo
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Added some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

### Maven artifacts in Magnolia's Nexus

The code is built by [GitHub actions](https://github.com/IBM/magkit-test/actions/workflows/build.yaml).
You can browse available artifacts through [Magnolia's Nexus](https://nexus.magnolia-cms.com/#nexus-search;quick~magkit-test)

### Versions, technology stack and Maven dependency

|        | Java | Magnolia | Tomcat | 
|--------|-------------|----------|---------------|
| 1.0.8  | 11          | 6.2.19   | 9.0.74       | 
| 1.0.10 | 11          | 6.2.45   | 9.0.74       | 
| 1.1.0  | 17          | 6.3.17   | 9.0.111       | 

To use the magkit-test-cms module in your Maven project, add the following dependency to your `pom.xml`:

```xml
    <dependency>
        <artifactId>magkit-test-cms</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>${module.version}</version>
    </dependency>
```

## Mock Helpers Overview

The project provides a consistent family of test utilities to build rich, internally consistent Mockito mocks for JCR, Servlet and Magnolia CMS environments. Each module follows the same core patterns:

### Core Concepts
* *MockUtils classes* (e.g. `NodeMockUtils`, `SessionMockUtils`, `ServletMockUtils`, Magnolia specific `ComponentsMockUtils`) expose static factory methods `mockXxx(...)` with optional varargs of stubbing operations.
* *StubbingOperation classes* (e.g. `NodeStubbingOperation`, `HttpServletRequestStubbingOperation`) encapsulate behavior changes. Chain them through varargs at creation time or apply later via `operation.of(existingMock)` to keep tests DRY and readable.
* *Get-or-create & ThreadLocal context*: Factories return an existing mock if already created in the current thread, ensuring hierarchical consistency (e.g. a mocked Node implies a Session, Workspace, Repository). ThreadLocal isolation supports parallel test execution.
* *Deterministic defaults*: Each mock ships with safe baseline behavior (no NPEs, minimal required relationships, sensible identifiers / types).
* *Explicit cleanup*: Call the appropriate cleanup between tests if state should not leak:
  * JCR: `SessionMockUtils.cleanSession()` (equivalent to `RepositoryMockUtils.cleanRepository()`).
  * Servlet: (if provided) context/session cleanup via dedicated utilities (see module README).
  * Magnolia CMS: `ContextMockUtils.cleanContext()` / component cleanup helpers.

### Why StubbingOperations instead of raw Mockito?
Direct Mockito stubbing (e.g. `when(node.getProperty("x"))...`) can desynchronize internal collections or related mocks. StubbingOperation implementations update all correlated aspects (lists, parent/child relations, session propagation, request ↔ session ↔ servlet context wiring). This preserves invariants your application code expects.

### Typical Usage Pattern
```java
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.*; // e.g. stubProperty
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubHeader;
import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;

// 1. JCR: Create / fetch a hierarchical node context with properties
var article = mockNode("website", "/content/articles/2025-10", stubProperty("title", "Hello"));

// 2. Servlet: Prepare request with header + consistent session/context
var request = mockHttpServletRequest(stubHeader("Accept", "application/json"));

// 3. Magnolia: Register a component mock so downstream code retrieves it via Magnolia Components
var i18n = mockComponentInstance(info.magnolia.cms.i18n.I18nContentSupport.class);

// 4. Enrich later while keeping invariants
stubProperty("summary", "Short intro").of(article);
```

### Quick Reference
| Module | Focus | Representative Factory | Cleanup |
|--------|-------|------------------------|---------|
| magkit-test-jcr | JCR Repository / Session / Node / Property | `mockNode(path, stubbings...)` / `mockSession(ws, ...)` | `SessionMockUtils.cleanSession()` |
| magkit-test-servlet | Servlet API (request, response, session, context) | `mockHttpServletRequest(stubbings...)` | (Module handles base; see README) |
| magkit-test-cms | Magnolia context, components, node types | `mockComponentInstance(MyType.class)` / `mockPageNode(path, ...)` | `ContextMockUtils.cleanContext()` |
| magkit-test-server | Embedded Tomcat + Magnolia for integration tests | JUnit5 `@ExtendWith(MagnoliaTomcatExtension.class)` | Standard JUnit lifecycle |

### Best Practices
* Prefer static imports of `*MockUtils` and `*StubbingOperation` for clarity.
* Keep each test self-contained: clean before (and optionally after) each method if order-independence matters.
* Avoid partial manual Mockito stubbing that bypasses provided operations—risk of inconsistent internal state.
* Layer only what you need: start with minimal mock + targeted stubbings to reduce cognitive overhead.

### Further Details
Each module README provides deeper examples:
* [JCR README](./magkit-test-jcr/README.md)
* [Servlet README](./magkit-test-servlet/README.md)
* [CMS README](./magkit-test-cms/README.md)
* [Server README](./magkit-test-server/README.md)

If you miss a helper or stubbing operation, open an issue or PR with a focused, tested proposal.

## License

All source files must include a Copyright and License header. The SPDX license header is
preferred because it can be easily scanned.

If you would like to see the detailed LICENSE click [here](LICENSE).

```text
#
# Copyright 2023- IBM Inc. All rights reserved
# SPDX-License-Identifier: Apache2.0
#
```
## Authors

Optionally, you may include a list of authors, though this is redundant with the built-in
GitHub list of contributors.

- Author: Wolf Bubenik - wolf.bubenik@ibm.com
