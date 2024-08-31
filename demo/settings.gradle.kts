/*
 * SPDX-License-Identifier: CC0-1.0
 */

rootProject.name = "demo"

pluginManagement {
	val absoluteProjectDir: File = rootProject.projectDir.absoluteFile
	val pluginProjectDir: File = absoluteProjectDir.parentFile
		.resolve("plugin")
		.relativeToOrSelf(absoluteProjectDir)

	includeBuild(pluginProjectDir)

	repositories {
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		mavenCentral()
	}
}
