/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

@JvmInline
internal value class PackageName private constructor(private val packageNameString: String) {

	init {
		require(packageNameString.isValidPackageName())
	}

	fun splitSubPackageNames(): Sequence<String> {
		return this.packageNameString.splitToSequence('.')
	}

	override fun toString(): String {
		return this.packageNameString
	}

	companion object {

		fun ofString(packageNameString: String): PackageName? {
			if (!(packageNameString.isValidPackageName())) {
				return null
			}

			return PackageName(packageNameString)
		}
	}
}

// <https://kotlinlang.org/spec/packages-and-imports.html#packages-and-imports>

private fun String.isValidPackageName(): Boolean {
	return this
		.splitToSequence('.')
		.all { subPackageName: String ->
			subPackageName.isNotEmpty() && subPackageName.none { ch: Char ->
				(ch == '\r') || (ch == '\n') || (ch == '`')
			}
		}
}
