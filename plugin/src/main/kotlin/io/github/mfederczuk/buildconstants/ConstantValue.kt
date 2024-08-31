/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants

import java.io.Serializable

public sealed class ConstantValue : Serializable {

	public data class Boolean(public val value: kotlin.Boolean) : ConstantValue()

	public data class Byte(public val value: kotlin.Byte) : ConstantValue()
	public data class UByte(public val value: kotlin.UByte) : ConstantValue()

	public data class Int(public val value: kotlin.Int) : ConstantValue()
	public data class UInt(public val value: kotlin.UInt) : ConstantValue()

	public data class Short(public val value: kotlin.Short) : ConstantValue()
	public data class UShort(public val value: kotlin.UShort) : ConstantValue()

	public data class Long(public val value: kotlin.Long) : ConstantValue()
	public data class ULong(public val value: kotlin.ULong) : ConstantValue()

	public data class Float(public val value: kotlin.Float) : ConstantValue()
	public data class Double(public val value: kotlin.Double) : ConstantValue()

	public data class Char(public val value: kotlin.Char) : ConstantValue()

	public data class String(public val value: kotlin.String) : ConstantValue()

	public val untypedValue: Comparable<*>
		get() {
			@Suppress("RemoveRedundantQualifierName")
			return when (this) {
				is ConstantValue.Boolean -> this.value

				is ConstantValue.Byte -> this.value
				is ConstantValue.UByte -> this.value

				is ConstantValue.Int -> this.value
				is ConstantValue.UInt -> this.value

				is ConstantValue.Short -> this.value
				is ConstantValue.UShort -> this.value

				is ConstantValue.Long -> this.value
				is ConstantValue.ULong -> this.value

				is ConstantValue.Float -> this.value
				is ConstantValue.Double -> this.value

				is ConstantValue.Char -> this.value

				is ConstantValue.String -> this.value
			}
		}

	public companion object {

		public fun ofUntypedValue(value: Any): ConstantValue {
			@Suppress("RemoveRedundantQualifierName")
			return when (value) {
				is kotlin.Boolean -> ConstantValue.Boolean(value)

				is kotlin.Byte -> ConstantValue.Byte(value)
				is kotlin.UByte -> ConstantValue.UByte(value)

				is kotlin.Int -> ConstantValue.Int(value)
				is kotlin.UInt -> ConstantValue.UInt(value)

				is kotlin.Short -> ConstantValue.Short(value)
				is kotlin.UShort -> ConstantValue.UShort(value)

				is kotlin.Long -> ConstantValue.Long(value)
				is kotlin.ULong -> ConstantValue.ULong(value)

				is kotlin.Float -> ConstantValue.Float(value)
				is kotlin.Double -> ConstantValue.Double(value)

				is kotlin.Char -> ConstantValue.Char(value)

				is kotlin.String -> ConstantValue.String(value)

				else -> error("Invalid constant value $value of type ${value.javaClass}")
			}
		}
	}
}
