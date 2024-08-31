/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

// TODO: add unit tests for this

internal fun <T : Any> Any.convertTo(targetType: Class<T>): T {
	@Suppress("UNCHECKED_CAST")
	return when (targetType) {
		Boolean::class.javaPrimitiveType, Boolean::class.javaObjectType -> (this.convertToBoolean() as T)

		Byte::class.javaPrimitiveType, Byte::class.javaObjectType -> (this.convertToByte() as T)
		UByte::class.java -> (this.convertToByte().toUByte() as T)

		Short::class.javaPrimitiveType, Short::class.javaObjectType -> (this.convertToShort() as T)
		UShort::class.java -> (this.convertToShort().toUShort() as T)

		Int::class.javaPrimitiveType, Int::class.javaObjectType -> (this.convertToInt() as T)
		UInt::class.java -> (this.convertToInt().toUInt() as T)

		Long::class.javaPrimitiveType, Long::class.javaObjectType -> (this.convertToLong() as T)
		ULong::class.java -> (this.convertToLong().toULong() as T)

		Float::class.javaPrimitiveType, Float::class.javaObjectType -> (this.convertToFloat() as T)
		Double::class.javaPrimitiveType, Double::class.javaObjectType -> (this.convertToDouble() as T)

		Char::class.javaPrimitiveType, Char::class.javaObjectType -> (this.convertToChar() as T)

		String::class.java -> (this.toString() as T)

		else -> error("Invalid conversion to $targetType")
	}
}

private fun Any.convertToBoolean(): Boolean {
	if (this is Boolean) {
		return this
	}

	if (this is String) {
		return when (this) {
			"true", "True", "TRUE", "yes", "Yes", "YES" -> true
			"false", "False", "FALSE", "no", "No", "NO" -> false
			else -> error("Invalid conversion from string ${this.quoted()} to boolean")
		}
	}

	error("Invalid conversion from ${this.javaClass} to boolean")
}

private fun Any.convertToByte(): Byte {
	when (this) {
		is Byte -> return this
		is UByte -> return this.toByte()
	}

	val l: Long? =
		when (this) {
			is Short -> this.toLong()
			is UShort -> this.toLong()

			is Int -> this.toLong()
			is UInt -> this.toLong()

			is Long -> this
			is ULong -> this.toLong()

			else -> null
		}
	if (l != null) {
		val b: Byte = l.toByte()

		if (b.toLong() == l) {
			return b
		}

		error("Invalid conversion from $this byte")
	}

	if (this is String) {
		val b: Byte? = this.toByteOrNull()
		if (b != null) return b

		val ub: UByte? = this.toUByteOrNull()
		if (ub != null) return ub.toByte()

		error("Invalid conversion from string ${this.quoted()} to byte")
	}

	error("Invalid conversion from ${this.javaClass} to byte")
}

private fun Any.convertToShort(): Short {
	when (this) {
		is Byte -> return this.toShort()
		is UByte -> return this.toShort()

		is Short -> return this
		is UShort -> return this.toShort()
	}

	val l: Long? =
		when (this) {
			is Int -> this.toLong()
			is UInt -> this.toLong()

			is Long -> this
			is ULong -> this.toLong()

			else -> null
		}
	if (l != null) {
		val s: Short = l.toShort()

		if (s.toLong() == l) {
			return s
		}

		error("Invalid conversion from $this to short")
	}

	if (this is String) {
		val s: Short? = this.toShortOrNull()
		if (s != null) return s

		val us: UShort? = this.toUShortOrNull()
		if (us != null) return us.toShort()

		error("Invalid conversion from string ${this.quoted()} to short")
	}

	error("Invalid conversion from ${this.javaClass} to short")
}

private fun Any.convertToInt(): Int {
	when (this) {
		is Byte -> return this.toInt()
		is UByte -> return this.toInt()

		is Short -> return this.toInt()
		is UShort -> return this.toInt()

		is Int -> return this
		is UInt -> return this.toInt()
	}

	val l: Long? =
		when (this) {
			is Long -> this
			is ULong -> this.toLong()

			else -> null
		}
	if (l != null) {
		val i: Int = l.toInt()

		if (i.toLong() == l) {
			return i
		}

		error("Invalid conversion from $this to int")
	}

	if (this is String) {
		val i: Int? = this.toIntOrNull()
		if (i != null) return i

		val ui: UInt? = this.toUIntOrNull()
		if (ui != null) return ui.toInt()

		error("Invalid conversion from string ${this.quoted()} to int")
	}

	error("Invalid conversion from ${this.javaClass} to int")
}

private fun Any.convertToLong(): Long {
	return when (this) {
		is Byte -> this.toLong()
		is UByte -> this.toLong()

		is Short -> this.toLong()
		is UShort -> this.toLong()

		is Int -> this.toLong()
		is UInt -> this.toLong()

		is Long -> this
		is ULong -> this.toLong()

		is String -> kotlin.run {
			val l: Long? = this.toLongOrNull()
			if (l != null) return@run l

			val ul: ULong? = this.toULongOrNull()
			if (ul != null) return@run ul.toLong()

			error("Invalid conversion from string ${this.quoted()} to long")
		}

		else -> error("Invalid conversion from ${this.javaClass} to long")
	}
}

private fun Any.convertToFloat(): Float {
	return when (this) {
		is Float -> this

		is Double -> {
			val f: Float = this.toFloat()

			check(f.toDouble() == this) {
				"Invalid conversion from double $this to float"
			}

			f
		}

		is Byte -> this.toFloat()
		is UByte -> this.toFloat()

		is Short -> this.toFloat()
		is UShort -> this.toFloat()

		is Int -> {
			checkNotNull(this.toFloatExact()) {
				"Invalid conversion from int $this to float"
			}
		}

		is UInt -> {
			checkNotNull(this.toFloatExact()) {
				"Invalid conversion from unsigned int $this to float"
			}
		}

		is Long -> {
			checkNotNull(this.toIntExact()?.toFloatExact()) {
				"Invalid conversion from long $this to float"
			}
		}

		is ULong -> {
			checkNotNull(this.toUIntExact()?.toFloatExact()) {
				"Invalid conversion from unsigned long $this to float"
			}
		}

		is String -> kotlin.run {
			val i: Int? = this.toIntOrNull()
			if (i != null) {
				return@run checkNotNull(i.toFloatExact()) {
					"Invalid conversion from string ${this.quoted()} to float"
				}
			}

			val ui: UInt? = this.toUIntOrNull()
			if (ui != null) {
				return@run checkNotNull(ui.toFloatExact()) {
					"Invalid conversion from string ${this.quoted()} to float"
				}
			}

			checkNotNull(this.toFloatOrNull()) {
				"Invalid conversion from string ${this.quoted()} to double"
			}
		}

		else -> error("Invalid conversion from ${this.javaClass} to float")
	}
}

private fun Any.convertToDouble(): Double {
	return when (this) {
		is Float -> this.toDouble()
		is Double -> this

		is Byte -> this.toDouble()
		is UByte -> this.toDouble()

		is Short -> this.toDouble()
		is UShort -> this.toDouble()

		is Int -> this.toDouble()
		is UInt -> this.toDouble()

		is Long -> {
			checkNotNull(this.toDoubleExact()) {
				"Invalid conversion from long $this to double"
			}
		}

		is ULong -> {
			checkNotNull(this.toDoubleExact()) {
				"Invalid conversion from unsigned long $this to double"
			}
		}

		is String -> kotlin.run {
			val l: Long? = this.toLongOrNull()
			if (l != null) {
				return@run checkNotNull(l.toDoubleExact()) {
					"Invalid conversion from string ${this.quoted()} to double"
				}
			}

			val ul: ULong? = this.toULongOrNull()
			if (ul != null) {
				return@run checkNotNull(ul.toDoubleExact()) {
					"Invalid conversion from string ${this.quoted()} to double"
				}
			}

			checkNotNull(this.toDoubleOrNull()) {
				"Invalid conversion from string ${this.quoted()} to double"
			}
		}

		else -> error("Invalid conversion from ${this.javaClass} to double")
	}
}

private fun Any.convertToChar(): Char {
	if (this is Char) {
		return this
	}

	if (this is String) {
		val singleChar: Char? = this.singleOrNull()

		if (singleChar != null) {
			return singleChar
		}

		error("Invalid conversion from string ${this.quoted()} to char")
	}

	error("Invalid conversion from ${this.javaClass} to char")
}

private fun Long.toIntExact(): Int? {
	val i: Int = this.toInt()

	if (i.toLong() == this) {
		return i
	}

	return null
}

private fun ULong.toUIntExact(): UInt? {
	val ui: UInt = this.toUInt()

	if (ui.toULong() == this) {
		return ui
	}

	return null
}

private fun Int.toFloatExact(): Float? {
	val f: Float = this.toFloat()

	if (f.toInt() == this) {
		return f
	}

	return null
}

private fun UInt.toFloatExact(): Float? {
	val f: Float = this.toFloat()

	if (f.toUInt() == this) {
		return f
	}

	return null
}

private fun Long.toDoubleExact(): Double? {
	val d: Double = this.toDouble()

	if (d.toLong() == this) {
		return d
	}

	return null
}

private fun ULong.toDoubleExact(): Double? {
	val d: Double = this.toDouble()

	if (d.toULong() == this) {
		return d
	}

	return null
}
