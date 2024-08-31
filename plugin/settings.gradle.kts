/*
 * SPDX-License-Identifier: CC0-1.0
 */

rootProject.name = "plugin"

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
