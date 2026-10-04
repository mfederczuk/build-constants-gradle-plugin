<!--
  Copyright (c) 2026 Michael Federczuk
  SPDX-License-Identifier: CC-BY-SA-4.0
-->

# Build Constants Gradle Plugin #

[version_shield]: https://img.shields.io/badge/version-0.1.0--indev02-informational.svg
[release_page]: https://github.com/mfederczuk/build-constants-gradle-plugin/releases/tag/v0.1.0-indev02 "Release v0.1.0-indev02"
[![version: 0.1.0-indev02][version_shield]][release_page]
[![Changelog](https://img.shields.io/badge/-Changelog-informational.svg)](CHANGELOG.md "Changelog")

## About ##

This is a simple [Gradle] plugin to generate Kotlin constants during build time, similar to
[Android's `BuildConfig` class][android_buildconfig].

All constants are put inside top-level Kotlin objects.  
Objects and their constants are configured in the Gradle build scripts and will be generated before the main Kotlin
source code gets compiled.  
Package, visibility and name of the generated Kotlin objects are configurable.

[Gradle]: <https://gradle.org/> "Gradle Build Tool"
[android_buildconfig]: <https://developer.android.com/build/gradle-tips#share-custom-fields-and-resource-values-with-your-app-code> "Gradle tips and recipes &nbsp;|&nbsp; Android Studio &nbsp;|&nbsp; Android Developers"

## Usage ##

Apply the plugin to your Gradle project and configure like so:

```kotlin
plugins {
    id("io.github.mfederczuk.build-constants") version "0.1.0-indev02"
}

buildConstants.pkg("com.example") {
    val isDebugProvider: Provider<Boolean> =
        project.providers.gradleProperty("debug")
            .map(String::toBoolean)
            .orElse(false)

    obj("BuildConstants") {
        // Project.getVersion()'s return type is `java.lang.Object`, which is
        // why the toString() call is necessary.
        prop("VERSION").stringValue = project.version.toString()

        // Instances of Gradle's `org.gradle.api.provider.Provider` type are
        // also accepted for lazy evaluation.
        prop("IS_DEBUG").booleanValue = isDebugProvider
    }

    pkg("api") {
        // The default visibility for objects is internal.
        // To make an object public instead, the obj() call must be prefixed
        // with `public.`
        public.obj("ApiConstants") {
            prop("TOKEN").stringValue = isDebugProvider
                .map { isDebug: Boolean ->
                    if (isDebug) {
                        "debug token"
                    } else {
                        "production token"
                    }
                }
        }

        pkg("foo.bar").public.obj("Baz") {
            // ...
        }
    }
}
```

To generate the objects, either execute the task `:generateBuildConstantsCode` directly, or simply build the project
(by executing the task `:assemble`).  
The above configuration will result in the following files being generated:

```kotlin
package com.example

internal object BuildConstants {
    public const val VERSION: String = "0.1.0"

    public const val IS_DEBUG: Boolean = false
}
```

```kotlin
package com.example.api

public object ApiConstants {
    public const val TOKEN: String = "production token"
}
```

```kotlin
package com.example.api.foo.bar

public object Baz
```

## Contributing ##

Read through the [Contribution Guidelines](CONTRIBUTING.md) if you want to contribute to this project.

## License ##

This plugin is licensed under both the [**Mozilla Public License 2.0**](LICENSES/MPL-2.0.txt) AND
the [**Apache License 2.0**](LICENSES/Apache-2.0.txt).  
For more information about copying and licensing, see the [`COPYING.txt`](COPYING.txt) file.
