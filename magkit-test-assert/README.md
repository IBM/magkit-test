# Magkit Test Assert

Lightweight helper library for concise and readable preconditions / invariants in production and test code. The module provides the utility class `Require` with two nested namespaces for different failure semantics:

- `Require.Argument` throws an `IllegalArgumentException` (input validation / public API parameters).
- `Require.State` throws an `IllegalStateException` (object / system state, internal consistency).

The implementation centralizes recurring checks (null, empty, blank, type) and reduces duplication. Via the internal `ExceptionFactory` interface you can plug in custom runtime exception types if needed.

## Features
- Central, DRY preconditions in a single class
- Separate exception types for argument vs. state validation
- Extensible through arbitrary predicates via `reject(...)`
- Supported data types: `Object`, `CharSequence`, `Iterable<?>`, `Object[]`, type checking through `Class<?>`
- Ready-made predicates: `IS_NULL`, `IS_EMPTY_STRING`, `IS_BLANK_STRING`, `IS_EMPTY_ARRAY`, `IS_EMPTY_ITERABLE`

## Maven Dependency
Add the released artifact as a dependency:
```xml
<dependency>
    <groupId>de.ibmix.magkit</groupId>
    <artifactId>magkit-test-assert</artifactId>
    <version>1.1.0</version>
</dependency>
```
The aggregate POM currently uses `1.1.0-SNAPSHOT` internally – for production use a published release version.

## Quick Examples
```java
import de.ibmix.magkit.assertions.Require;

// Argument validation (IllegalArgumentException on failure)
public void createUser(String name, String email) {
    Require.Argument.notBlank(name, "name must not be blank");
    Require.Argument.notBlank(email, "email must not be blank");
    Require.Argument.reject(e -> !email.contains("@"), email, "email must contain '@'");
    // ... business logic
}

// State validation (IllegalStateException on failure)
public void start() {
    Require.State.notNull(config, "config not initialized");
    Require.State.reject(c -> !c.isValid(), config, "config invalid");
    // ... further processing
}

// Type check
Object value = "text";
Require.Argument.isInstanceof(value, String.class, "value must be String");

// Using a custom exception type
Require.ExceptionFactory factory = msg -> new RuntimeException("E102:" + msg);
Require.requireNotEmpty("abc", "string empty", factory); // throws RuntimeException with prefix on failure

// Composite custom validation
Require.Argument.reject(o -> o == null || ((String) o).length() < 3, "ab", "value too short");
```

## Extended Example with Iterable & Array
```java
List<String> items = List.of("one", "two");
Require.Argument.notEmpty(items, "items must not be empty");

String[] parts = {"a", "b"};
Require.State.notEmpty(parts, "parts must not be empty");
```

## Why not just `Objects.requireNonNull(...)`?
`Require` consolidates multiple recurring checks and provides:
- Consistent error messages
- Extensibility with arbitrary predicates
- Clear semantics (argument vs. state) without boilerplate
- Validation of complex structures without additional helpers

## Design Notes
- All methods are intentionally small and delegate to shared internal primitives (`reject`, `requireNotX`).
- No dependency on Magnolia – usable in any module.
- Use of `Predicate` enables composition and reuse.

## Test Coverage
The class is covered by `RequireTest` (JUnit 5): all methods, paths and error messages verified. Examples from the tests can serve as additional guidance.

## License
Published under the Apache License 2.0. See [LICENSE](../LICENSE) for details.

SPDX License Identifier: Apache-2.0

```
 * #%L
 * magkit-test-assert
 * %%
 * Copyright (C) 2023 - 2025 IBM iX
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

## Author
Wolf Bubenik - wolf.bubenik@ibm.com

## Further Ideas
- Additional factory for checked exceptions
- Optional message formatter (parameter interpolation instead of manual concatenation)
- Integration with logging for structured precondition reporting
