/*
 * SPDX-License-Identifier: CC0-1.0
 */

import io.github.mfederczuk.buildconstants.ConstantValue
import java.time.Instant

plugins {
	kotlin("jvm") version "2.4.21"
	application
	id("io.github.mfederczuk.build-constants")
}

application {
	mainClass = "io.github.mfederczuk.buildconstants.demo.Main"
}

kotlin {
	compilerOptions {
		// <https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-unused-return-value-checker.md>
		freeCompilerArgs.add("-Xreturn-value-checker=full")
	}
}

buildConstants {
	"io.github.mfederczuk.buildconstants.demo.BuildConstants"(id = "main") @Suppress("unused") {
		val isDebug: Boolean by constant {
			project.providers.gradleProperty("debug")
				.map(String::toBoolean)
				.orElse(false)
		}

		val buildTime: String by constant {
			if (isDebug) {
				// Keep in mind that adding the build time will create non-reproducible builds.
				// (if that is important for you)
				Instant.now()
			} else {
				"[REDACTED]"
			}
		}

		val version: String by constant {
			project.version
		}
	}

	"io.github.mfederczuk.buildconstants.demo.ids.IDs" {
		val ids: Set<String> = setOf("foo", "bar", "baz")

		constants {
			for (id: String in ids) {
				register(id) {
					value = ConstantValue.Int(id.hashCode())
				}
			}
		}
	}
}
