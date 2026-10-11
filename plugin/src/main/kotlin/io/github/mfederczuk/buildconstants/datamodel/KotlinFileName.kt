/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.datamodel

import io.github.mfederczuk.buildconstants.internal.quoted
import java.io.Serializable

private const val INVALID_FILE_NAME_CHARACTERS: String = "\"*/:<>?\\|"

// TODO: Turn this into a value class once it is supported without @JvmInline.
public class KotlinFileName private constructor(private val nameString: String) : Serializable {

	// region equals(), hashCode() and toString()

	override fun equals(other: Any?): Boolean {
		return (this === other) || ((other is KotlinFileName) && (this.nameString == other.nameString))
	}

	override fun hashCode(): Int {
		return this.nameString.hashCode()
	}

	override fun toString(): String {
		return this.nameString
	}

	// endregion

	public companion object {

		public fun String.toKotlinFileNameOrNull(): KotlinFileName? {
			return if (this.isKotlinFileName()) {
				KotlinFileName(nameString = this)
			} else {
				null
			}
		}

		public fun String.toKotlinFileName(): KotlinFileName {
			return requireNotNull(this.toKotlinFileNameOrNull()) {
				"Invalid file name: ${this.quoted()}"
			}
		}
	}
}

private fun String.isKotlinFileName(): Boolean {
	return this.isNotEmpty() &&
		this.none { ch: Char ->
			(ch.code in 0..31) || (ch in INVALID_FILE_NAME_CHARACTERS)
		} &&
		this.endsWith(".kt")
}
