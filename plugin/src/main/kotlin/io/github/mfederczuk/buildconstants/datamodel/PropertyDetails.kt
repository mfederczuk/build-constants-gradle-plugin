/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.datamodel

import java.io.Serializable
import java.util.Objects

// See <https://jakewharton.com/public-api-challenges-in-kotlin/> as to why this isn't a data class.
public class PropertyDetails(
	public val visibility: Visibility,
	public val typedValue: TypedValue,
) : Serializable {

	// region equals(), hashCode() and toString()

	override fun equals(other: Any?): Boolean {
		return (this === other) || ((other is PropertyDetails) && this.equals(other))
	}

	private fun equals(other: PropertyDetails): Boolean {
		return (this.visibility == other.visibility) &&
			(this.typedValue == other.typedValue)
	}

	override fun hashCode(): Int {
		return Objects.hash(
			this.visibility,
			this.typedValue,
		)
	}

	override fun toString(): String {
		return "PropertyDetails(" +
			"visibility = ${this.visibility}, " +
			"typedValue = ${this.typedValue}" +
			")"
	}

	// endregion
}
