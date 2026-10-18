<!--
  Copyright (c) 2026 Michael Federczuk
  SPDX-License-Identifier: CC-BY-SA-4.0
-->

<!-- markdownlint-disable no-duplicate-heading -->

# Changelog #

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v0.1.0-indev03] - 2026-10-18 ##

[v0.1.0-indev03]: <https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev03>

### Added ###

* All generated files now `@Suppress` the Kotlin compiler warning `REDUNDANT_VISIBILITY_MODIFIER` (instead of
  the IntelliJ warning `RedundantVisibilityModifier`) so that the files can be used with
  `allWarningsAsErrors` (`-Werror`) turned on ([`4984004`])
* All generated files now `@Suppress` all [ktlint](https://github.com/ktlint/ktlint) errors ([`f2eedaa`])
* Generated objects can be modified with the `data` modifier ([`9aa687e`])
* The visibility of generated properties can be set to **internal** ([`357d78e`])
* Nested objects can be generated ([`39f4f61`])

### Changed ###

* Complete rewrite of the data model, API and DSL.  
  Instead of having a task for each generated object, there is now only one task that generates *all* objects ([`1caf071`])
* Java compatibility version changed from 1.8 to 17 ([`be148d2`])
* The license was changed from **Mozilla Public License 2.0** AND **Apache License 2.0** to
  **GNU General Public License v3.0 or later** ([`85c56c0`])

[`4984004`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/49840042b435d58fdaa7d19f8e00404865a13746>
[`f2eedaa`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/f2eedaa76121d9ddf8fe1a667cbfe6031591eb17>
[`9aa687e`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/9aa687ee3a4b7ae8fd0694f8c6db1356b3815ae6>
[`357d78e`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/357d78e2a9b7a0a24bae7e9f1c99dc04b9c6f317>
[`39f4f61`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/39f4f61c66948d4dc6274cbd6570a8ac2b77be67>
[`1caf071`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/1caf0716f02bbd9a2d3382f639af084800d57823>
[`be148d2`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/be148d276b00aef5ed722002587c0fd9d89416a9>
[`85c56c0`]: <https://github.com/mfederczuk/build-constants-gradle-plugin/commit/85c56c068929978122745e9061f2aa31b07bbfa6>

## [v0.1.0-indev02] - 2024-10-06 ##

[v0.1.0-indev02]: <https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev02>

### Added ###

* The generated constants objects are now part of the sources JAR assembled by the task `kotlinSourcesJar`

### Fixed ###

* Fixed an error when trying to run the task `sourcesJar`

## [v0.1.0-indev01] - 2024-08-31 ##

[v0.1.0-indev01]: <https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev01>

Initial Release
