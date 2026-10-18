/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
	`kotlin-dsl`
	id("com.gradle.plugin-publish")
}

group = "io.github.mfederczuk"
version = "0.1.0-indev02"

val projectJvmTarget: JvmTarget = JvmTarget.JVM_17

java {
	val projectJavaCompatibilityVersion: JavaVersion = projectJvmTarget.toJavaVersion()
	targetCompatibility = projectJavaCompatibilityVersion
	sourceCompatibility = projectJavaCompatibilityVersion
}

kotlin {
	explicitApi()

	compilerOptions {
		jvmTarget = projectJvmTarget

		// <https://github.com/Kotlin/KEEP/blob/main/proposals/KEEP-0412-unused-return-value-checker.md>
		freeCompilerArgs.add("-Xreturn-value-checker=full")

		extraWarnings = true
		allWarningsAsErrors = findProperty("allWarningsAsErrors")
			?.toString()
			.orEmpty()
			.ifEmpty { "false" }
			.toBooleanStrict()
	}
}

dependencies {
	api("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.5.2")
	implementation("com.squareup:kotlinpoet:2.4.0")
}

// region publishing

val pluginDisplayName: String = "Build Constants Gradle Plugin"
val pluginDescription: String =
	"""
	Plugin to generate Kotlin constants during build time.
	""".trimIndent()

val pluginWebsiteUrl: URI = URI("https://github.com/mfederczuk/build-constants-gradle-plugin#readme")
val pluginRepoWebsiteUrl: URI = URI("https://github.com/mfederczuk/build-constants-gradle-plugin")
val pluginHttpsGitRepoUrl: URI = URI("https://github.com/mfederczuk/build-constants-gradle-plugin.git")
val pluginSshGitRepoUrl: URI = URI("ssh://git@github.com/mfederczuk/build-constants-gradle-plugin.git")
val pluginIssueManagementWebsiteUrl: URI = URI("https://github.com/mfederczuk/build-constants-gradle-plugin/issues")

gradlePlugin {
	website = pluginWebsiteUrl.toString()
	vcsUrl = pluginRepoWebsiteUrl.toString()

	plugins {
		register("build-constants") {
			id = "${project.group}.$name"
			implementationClass = "io.github.mfederczuk.buildconstants.BuildConstantsPlugin"

			displayName = pluginDisplayName
			description = pluginDescription
			tags = setOf("codegen")
		}
	}
}

publishing {
	publications {
		create<MavenPublication>(name = "pluginMaven") {
			groupId = project.group.toString()
			artifactId = "build-constants-gradle-plugin"
			version = project.version.toString()

			pom {
				name = pluginDisplayName
				description = pluginDescription
				url = pluginWebsiteUrl.toString()
				inceptionYear = "2024"

				licenses {
					license {
						name = "GPL-3.0-or-later"
						url = "https://www.gnu.org/licenses/gpl-3.0.txt"
						comments = "GNU General Public License v3.0 or later"
						distribution = "repo"
					}
				}

				developers {
					developer {
						id = "mfederczuk"
						name = "Michael Federczuk"
						email = "federczuk.michael@protonmail.com"
						url = "https://github.com/mfederczuk"
						timezone = "Europe/Vienna"
					}
				}
				contributors {
				}

				issueManagement {
					system = "GitHub Issues"
					url = pluginIssueManagementWebsiteUrl.toString()
				}

				scm {
					connection = "scm:git:$pluginHttpsGitRepoUrl"
					developerConnection = "scm:git:$pluginSshGitRepoUrl"
					tag = "v${project.version}"
					url = pluginRepoWebsiteUrl.toString()
				}
			}
		}
	}
}

// endregion

fun JvmTarget.toJavaVersion(): JavaVersion {
	return JavaVersion.toVersion(this.target)
}
