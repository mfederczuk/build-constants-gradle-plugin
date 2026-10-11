/*
 * SPDX-License-Identifier: CC0-1.0
 */

plugins {
	kotlin("jvm") version "2.4.21"
	application
	id("io.github.mfederczuk.build-constants") version "trunk-SNAPSHOT"
}

application {
	mainClass = "com.example.Main"
}

kotlin {
	compilerOptions {
		// <https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-unused-return-value-checker.md>
		freeCompilerArgs.add("-Xreturn-value-checker=full")

		// freeCompilerArgs.add("-Xrender-internal-diagnostic-names")

		extraWarnings = true
		allWarningsAsErrors = findProperty("allWarningsAsErrors")
			?.toString()
			.orEmpty()
			.ifEmpty { "false" }
			.toBooleanStrict()
	}
}

buildConstants.pkg("com.example") {
	val isDebugProvider: Provider<Boolean> = project.providers.gradleProperty("debug")
		.map(String::toBoolean)
		.orElse(false)

	obj("BuildConstants") {
		// Project.getVersion()'s return type is `java.lang.Object`, which is why the toString() call is necessary.
		prop("VERSION").stringValue = project.version.toString()

		// Instances of Gradle's `org.gradle.api.provider.Provider` type are also accepted for lazy evaluation.
		prop("IS_DEBUG").booleanValue = isDebugProvider
	}

	pkg("api") {
		// The default visibility for objects is internal.
		// To make an object public instead, the obj() call must be prefixed with `public.`
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

		pkg("foo.bar").public.data.obj("Baz") {
			// ...
		}
	}
}
