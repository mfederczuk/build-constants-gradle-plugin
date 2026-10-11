<!--
  Copyright (c) 2026 Michael Federczuk
  SPDX-License-Identifier: CC-BY-SA-4.0
-->

<!-- markdownlint-disable no-duplicate-heading -->

# Changelog #

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/2.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased] ##

[Unreleased]: https://github.com/mfederczuk/build-constants-gradle-plugin/compare/v0.1.0-indev02...HEAD

### Added ###

* All generated files now `@Suppress` the Kotlin compiler warning `REDUNDANT_VISIBILITY_MODIFIER` (instead of
  the IntelliJ warning `RedundantVisibilityModifier`) so that the files can be used with
  `allWarningsAsErrors` (`-Werror`) turned on
* All generated files now `@Suppress` all [ktlint](https://github.com/ktlint/ktlint) errors
* Generated objects can be modified with the `data` modifier
* The visibility of generated properties can be set to **internal**
* Nested objects can be generated

### Changed ###

* Complete rewrite of the data model, API and DSL.  
  Instead of having a task for each generated object, there is now only one task that generates *all* objects
* Java compatibility version changed from 1.8 to 17
* The license was changed from **Mozilla Public License 2.0** AND **Apache License 2.0** to
  **GNU General Public License v3.0 or later**

## [v0.1.0-indev02] - 2024-10-06 ##

[v0.1.0-indev02]: https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev02

### Added ###

* The generated constants objects are now part of the sources JAR assembled by the task `kotlinSourcesJar`

### Fixed ###

* Fixed an error when trying to run the task `sourcesJar`

## [v0.1.0-indev01] - 2024-08-31 ##

[v0.1.0-indev01]: https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev01

Initial Release
