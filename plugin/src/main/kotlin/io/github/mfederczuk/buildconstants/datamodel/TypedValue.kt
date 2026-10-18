/*
 * SPDX-License-Identifier: CC0-1.0
 */

@file:Suppress("ktlint:standard:blank-line-before-declaration")

package io.github.mfederczuk.buildconstants.datamodel

import java.io.Serializable

public sealed class TypedValue : Serializable {

	public data class Boolean(public val value: kotlin.Boolean) : TypedValue()

	public data class Byte(public val value: kotlin.Byte) : TypedValue()
	public data class Short(public val value: kotlin.Short) : TypedValue()
	public data class Int(public val value: kotlin.Int) : TypedValue()
	public data class Long(public val value: kotlin.Long) : TypedValue()

	public data class UByte(public val value: kotlin.UByte) : TypedValue()
	public data class UShort(public val value: kotlin.UShort) : TypedValue()
	public data class UInt(public val value: kotlin.UInt) : TypedValue()
	public data class ULong(public val value: kotlin.ULong) : TypedValue()

	public data class Float(public val value: kotlin.Float) : TypedValue()
	public data class Double(public val value: kotlin.Double) : TypedValue()

	public data class Char(public val value: kotlin.Char) : TypedValue()

	public data class String(public val value: kotlin.String) : TypedValue()
}
