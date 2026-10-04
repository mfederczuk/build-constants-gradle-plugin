/*
 * SPDX-License-Identifier: CC0-1.0
 */

rootProject.name = "build-constants"

pluginManagement {
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

include(":plugin")
