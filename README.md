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

## Usage ##

Apply the plugin to your Gradle project and configure like so:

```kotlin
import io.github.mfederczuk.buildconstants.ConstantValue
import io.github.mfederczuk.buildconstants.Visibility

plugins {
	id("io.github.mfederczuk.build-constants") version "0.1.0-indev01"
}

buildConstants {
	objects {
		// The internal ID name used for naming
		// the task that generates the Kotlin object.
		//         |
		//         V
		register("main") {
			packageName = "org.example"

			// Configuring the visibility is optional.
			// The default is `internal`.
			visibility = Visibility.PUBLIC

			objectName = "BuildConstants"

			constants {
				// The identifier of the constant
				// as it will be generated.
				//           |
				//           V
				register("VERSION") {
					// Project.getVersion()'s return type
					// is `java.lang.Object`, which is why
					// the toString() call is necessary.
					val s = project.version.toString()
					value = ConstantValue.String(s)
				}

				register("BUILD_TIME") {
					// Put values that should be computed
					// lazily inside a provider to make
					// sure that they only get evaluated
					// when the value is queried.
					value = project.providers.provider {
						val l = Instant.now().epochSecond
						ConstantValue.Long(l)
					}
				}
			}
		}

		// Register more objects here.
	}
}
```

This configuration will result two tasks being registered in the project (both in the task group `build`):

* `:generateConstantsObjectMain` — Generates the single object that was configured (see below for an example)
* `:generateConstantsObjects` — Executes all other `:generateConstantsObject*` tasks

```kotlin
package org.example

public object BuildConstants {
	public const val VERSION: String = "2.1.0-rc02"

	public const val BUILD_TIME: Long = 3_137_977_200
}
```

Now, the above configuration is pretty verbose and ugly, which is why a Kotlin DSL is provided that simplifies it a lot:

```kotlin
import io.github.mfederczuk.buildconstants.Visibility

buildConstants {
	// Package name and object name is merged into one string.
	// The internal ID name is automatically taken from the object name.
	"org.example.BuildConstants" { // Kotlin magic
		// The package name and object name can
		// still be configured just like before.

		// The visibility is still optional.
		visibility = Visibility.PUBLIC

		// The name of the generated constant is produced by
		// transforming the delegated property's name into
		// the equivalent UPPER_SNAKE_CASE name.
		// The explicit type specified is required.
		val version: String by constant {
			// No need for the toString() call anymore,
			// the DSL initializers will automatically do it.
			// There's also no need to wrap the value in
			// an instance of `ConstantValue` anymore.
			project.version
		}

		// In case the automatically generated constant name is
		// unwanted, or it should just be named differently, the name
		// can still be explicitly configured with the following syntax:
		val buildTime: Long by constant(name = "BUILD_TIME") {
			// DSL initializers are wrapped
			// in providers automatically.
			Instant.now().epochSecond
		}
	}

	// Explicit internal ID names can be given with the following syntax:
	"..."(id = "main") {
		...
	}
}
```

## Contributing ##

Read through the [Contribution Guidelines](CONTRIBUTING.md) if you want to contribute to this project.

## License ##

This plugin is licensed under both the [**Mozilla Public License 2.0**](LICENSES/MPL-2.0.txt) AND
the [**Apache License 2.0**](LICENSES/Apache-2.0.txt).  
For more information about copying and licensing, see the [`COPYING.txt`](COPYING.txt) file.
