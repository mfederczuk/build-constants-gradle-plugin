/*
 * SPDX-License-Identifier: CC0-1.0
 */

@file:JvmName("Main")

package io.github.mfederczuk.buildconstants.demo

import io.github.mfederczuk.buildconstants.demo.ids.IDs

fun main() {
	val buildType: String =
		@Suppress("KotlinConstantConditions")
		if (BuildConstants.IS_DEBUG) {
			"debug"
		} else {
			"release"
		}

	println("$buildType v${BuildConstants.VERSION} (built at ${BuildConstants.BUILD_TIME})")
}

fun idDemo(id: Int) {
	when (id) {
		IDs.foo -> println("ID foo")
		IDs.bar -> println("ID bar")
		IDs.baz -> println("ID baz")
		else -> println("unknown ID")
	}
}
