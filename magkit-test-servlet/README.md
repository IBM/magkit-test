# Magkit Test Servlet

This project contains a builder API to create mockito mocks of javax.servlet classes and stub their behavior. 
The mocks are always created with some basic stubbing of a default behavior.

## Usage
### Maven dependency

```xml
    <dependency>
        <artifactId>magkit-test-servlet</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.0.0</version>
    </dependency>
```

### Mock a class:
XxxMockUtil classes provide static methods for each class to mock. 

For each mocked class there is a XxxStubbingOperation class to stub its behavior.

We recommend using static imports to keep test code short and readable.

Use the ServletMockUtils to mock a javax.servlet class:

```java
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
    
HttpServletRequest request = mockHttpServletRequest();

```

The stubbed default behavior is:
```java
// We do not run into a NullPointerException when accessing the attributes and parameters
assertThat(request.getAttributeNames().hasMoreElements(), is(false));
assertThat(request.getParameterMap().isEmpty(), is(true));
assertThat(request.getParameterNames().hasMoreElements(), is(false));
assertThat(request.getParameterValues("any"), nullValue());

// Each request mock has a session mock with the id "test"...
assertThat(request.getSession().getId(), is("test"));

// ...and each session mock has a servlet context mock:
assertThat(request.getSession().getServletContext(), notNullValue());
```

### Stub methods of mock:
Changing the behavior of the mocks can be done in three ways:

```java
// Pass a XxxStubbingOperation to the mock method:
import static de.ibmix.magkit.test.servlet.ServletMockUtils.mockHttpServletRequest;
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubHeader;
HttpServletRequest request = mockHttpServletRequest(stubHeader("name", "value"));
assertThat(request.getHeader("name"), is("value"));

// Invoke the XxxStubbingOperation for an existing mock:
import static de.ibmix.magkit.test.servlet.HttpServletRequestStubbingOperation.stubCookie;
import static de.ibmix.magkit.test.servlet.CookieStubbingOperation.stubMaxAge;
stubCookie("name", "value", stubMaxAge(42)).of(request);
assertThat(request.getCookies()[0].getName(), is("name"));
assertThat(request.getCookies()[0].getValue(), is("value"));
assertThat(request.getCookies()[0].getMaxAge(), is(42));

// (!) Do NOT use the standard Mockito way of stubbing, because this may result in inconsistent behavior:
// This works...
doReturn("POST").when(request).getMethod();
assertThat(request.getMethod(), is("POST"));
// ...but other stubbings may result in inconsistent behavior.

// Care has been taken that stubbing of related mocks is consistent:
stubContextPath("path").of(request);
assertThat(request.getContextPath(), is("path"));
assertThat(request.getSession().getServletContext().getContextPath(), is("path"));
```

For more examples and details please consult the test classes.

### Quick Reference: StubbingOperation Overview
Below is a concise overview of the available *StubbingOperation types. Pass any number of these operations 
(as varargs) straight into the corresponding mock factory method (e.g. `mockHttpServletRequest(...)`) or apply them later using `.of(mock)`. 
The operations are designed to keep related mocks (request / session / servlet context / page context) consistent.

Use your IDE auto-complete or browse the source for full signatures (kept intentionally DRY here).

| StubbingOperation Type | Categories / Purpose | Representative Stub Methods (selection) |
|------------------------|----------------------|------------------------------------------|
| HttpServletRequestStubbingOperation | Context & routing | `stubContextPath`, `stubRequestUri`, `stubRequestUrl`, `stubQueryString`, `stubIsSecure` |
|                        | HTTP meta            | `stubMethod`, `stubProtocol`, `stubServerName`, `stubServerPort`, `stubHeader` |
|                        | Parameters           | `stubParameter`, `stubParameterMap` |
|                        | Session              | `stubHttpSession(id, ...)`, `stubHttpSession(session)` |
| HttpServletResponseStubbingOperation | Output & encoding | `stubContentType`, `stubCharacterEncoding`, `stubLocale` |
|                        | Streams / writer     | `stubOutputStream`, `stubWriter` |
| ServletContextStubbingOperation | Core context data | `stubContextPath`, `stubAttribute`, `stubInitParameter` |
| HttpSessionStubbingOperation | Composition & environment | `stubServletContext(context | contextOps...)` |
|                        | Attributes & lifecycle | `stubAttribute`, `stubCreationTime`, `stubLastAccessedTime`, `stubIsNew` |
| CookieStubbingOperation | Cookie properties   | `stubDomain`, `stubComment`, `stubPath`, `stubMaxAge`, `stubSecure`, `stubVersion` |
| PageContextStubbingOperation | Composition         | `stubHttpServletRequest(...)`, `stubHttpServletResponse(...)` |
|                        | Environment          | `stubServletContext(...)`, `stubHttpSession(session)` |

Usage patterns:
```java
// Direct creation with stubbings
HttpServletRequest request = mockHttpServletRequest(
    stubContextPath("/app"),
    stubParameter("q", "search"),
    stubHeader("Accept", "application/json")
);

// Post creation enrichment
stubParameter("extra", "x").of(request);

// Consistent propagation of context path through request -> session -> servlet context
stubContextPath("/newRoot").of(request);
```

Tip: If you need only the default baseline, just call the factory method without arguments. Passing null instead of an empty vararg is prevented intentionally (assertion).

## License

This code is published under the Apache2.0 license.

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

- Author: Wolf Bubenik - wolf.bubenik@ibm.com
