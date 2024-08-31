/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.net.URI

plugins {
	`kotlin-dsl`
	id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.mfederczuk"
version = "0.1.0-indev01"

java {
	targetCompatibility = JavaVersion.VERSION_1_8
	sourceCompatibility = targetCompatibility
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.fromTarget(java.targetCompatibility.toString())
	}

	explicitApi()
}

dependencies {
	implementation("com.squareup:kotlinpoet:1.17.0")
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
						name = "MPL-2.0"
						url = "https://www.mozilla.org/media/MPL/2.0/index.txt"
						comments = "Mozilla Public License 2.0"
						distribution = "repo"
					}
					license {
						name = "Apache-2.0"
						url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
						comments = "The Apache License, Version 2.0"
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
