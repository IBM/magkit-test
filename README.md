# Magkit Test

[![build-module](https://github.com/IBM/magkit-test/actions/workflows/build.yaml/badge.svg)](https://github.com/IBM/magkit-test/actions/workflows/build.yaml)

## Scope

Multi module project contains Java test libraries to provider test and Mockito mock helpers.
1. magkit-test-jcr - for JCR mocking
2. magkit-test-servlet - for servlet container mocking
3. magkit-test-cms - for Magnolia CMS mocking and testing

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

### Maven dependency

```xml
    <dependency>
        <artifactId>magkit-test-cms</artifactId>
        <groupId>de.ibmix.magkit</groupId>
        <version>1.0.0</version>
    </dependency>
```

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
