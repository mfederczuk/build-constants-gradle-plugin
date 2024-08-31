<!--
  Copyright (c) 2024 Michael Federczuk
  SPDX-License-Identifier: CC-BY-SA-4.0
-->

# Build Constants Gradle Plugin #

[version_shield]: https://img.shields.io/badge/version-N%2FA_(in_development)-important.svg
![version: N/A (in development)][version_shield]

## About ##

This is a simple [Gradle] plugin to generate Kotlin constants during build time, similar to
[Android's `BuildConfig` class][android_buildconfig].

All constants are put inside top-level Kotlin objects.  
Objects and their constants are configured in the Gradle build scripts and will be generated before the main Kotlin
source code gets compiled.  
Package, visibility and name of the generated Kotlin objects are configurable.

[Gradle]: <https://gradle.org/> "Gradle Build Tool"
[android_buildconfig]: <https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code> "Gradle tips and recipes &nbsp;|&nbsp; Android Studio &nbsp;|&nbsp; Android Developers"

## Contributing ##

Read through the [Contribution Guidelines](CONTRIBUTING.md) if you want to contribute to this project.

## License ##

This plugin is licensed under both the [**Mozilla Public License 2.0**](LICENSES/MPL-2.0.txt) AND
the [**Apache License 2.0**](LICENSES/Apache-2.0.txt).  
For more information about copying and licensing, see the [`COPYING.txt`](COPYING.txt) file.
