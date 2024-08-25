/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`kotlin-dsl`
}

group = "io.github.mfederczuk"
version = "0.1.0-indev01.SNAPSHOT"

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
