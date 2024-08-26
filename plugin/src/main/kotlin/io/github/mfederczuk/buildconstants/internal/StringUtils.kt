/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.internal

internal fun String.quoted(): String {
	return '\"' +
		this
			.replace("\\", "\\\\")
			.replace("\"", "\\\"") +
		'\"'
}
