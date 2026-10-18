/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.datamodel

import io.github.mfederczuk.buildconstants.internal.quoted
import java.io.Serializable
import java.util.PrimitiveIterator

// <https://kotlinlang.org/spec/syntax-and-grammar.html#grammar-rule-Identifier>

// TODO: Turn this into a value class once it is supported without @JvmInline.
public class KotlinIdentifier private constructor(private val rawIdentifierString: String) : Serializable {

	public fun toRawIdentifierString(): String {
		return this.rawIdentifierString
	}

	public fun toKotlinCodeString(): String {
		return if (this.rawIdentifierString.isUnquotedKotlinIdentifier()) {
			this.rawIdentifierString
		} else {
			"`${this.rawIdentifierString}`"
		}
	}

	// region equals(), hashCode() and toString()

	override fun equals(other: Any?): Boolean {
		return (this === other) ||
			((other is KotlinIdentifier) && (this.rawIdentifierString == other.rawIdentifierString))
	}

	override fun hashCode(): Int {
		return this.rawIdentifierString.hashCode()
	}

	@Suppress("POTENTIALLY_NON_REPORTED_ANNOTATION")
	@Deprecated(
		message = "Use either toKotlinCodeString() or toRawIdentifierString()",
		replaceWith = ReplaceWith("this.toKotlinCodeString()"),
	)
	override fun toString(): String {
		return this.toKotlinCodeString()
	}

	// endregion

	/**
	 * @param rawIdentifierAllowed Whether the identifier being built is allowed to be "raw". If `true`, then
	 * the builder acts as though the identifier is always quoted and will allow otherwise illegal characters
	 * (like spaces), even if the first character appended is not a backtick.
	 */
	public class Builder(private val rawIdentifierAllowed: Boolean) {

		public sealed class State {

			public sealed class IncompleteOrValid : State()

			public data class UnquotedValid(public val identifier: KotlinIdentifier) : IncompleteOrValid()

			public sealed class Quoted : IncompleteOrValid() {

				public data object Incomplete : Quoted()

				public data class CompletedValid(public val identifier: KotlinIdentifier) : Quoted()
			}

			public data object Invalid : State()
		}

		private val stringBuilder: StringBuilder = StringBuilder()

		/**
		 * If [State.Quoted.CompletedValid] or [State.Invalid] is returned, then the builder will also have been
		 * cleared.
		 */
		public fun append(codePoint: Int): State {
			val state: State =
				if (this.stringBuilder.isEmpty()) {
					this.appendEmpty(codePoint)
				} else {
					this.appendNonEmpty(codePoint)
				}

			when (state) {
				is State.UnquotedValid, is State.Quoted.Incomplete -> {}
				is State.Quoted.CompletedValid, is State.Invalid -> this.clear()
			}

			return state
		}

		private fun appendEmpty(codePoint: Int): State {
			val isQuoted: Boolean = (codePoint == '`'.code)

			val isValid: Boolean = codePoint.isUnquotedKotlinIdentifierStartCodePoint() ||
				((isQuoted || this.rawIdentifierAllowed) && !(codePoint.isCarriageReturnOrLineFeedCodePoint()))
			if (!isValid) {
				return State.Invalid
			}

			this.stringBuilder.appendCodePoint(codePoint)

			return if (isQuoted) {
				State.Quoted.Incomplete
			} else {
				val identifier = KotlinIdentifier(rawIdentifierString = Character.toString(codePoint))
				State.UnquotedValid(identifier = identifier)
			}
		}

		private fun appendNonEmpty(codePoint: Int): State {
			val isQuoted: Boolean = (this.stringBuilder[0] == '`')
			if (isQuoted) {
				return when {
					codePoint.isCarriageReturnOrLineFeedCodePoint() -> State.Invalid

					(codePoint != '`'.code) -> {
						this.stringBuilder.appendCodePoint(codePoint)

						State.Quoted.Incomplete
					}

					else -> {
						val rawIdentifierString: String = this.stringBuilder.toString().substring(startIndex = 1)

						if (rawIdentifierString.isNotEmpty()) {
							State.Quoted.CompletedValid(identifier = KotlinIdentifier(rawIdentifierString))
						} else {
							State.Invalid
						}
					}
				}
			}

			val isValid: Boolean = codePoint.isUnquotedKotlinIdentifierPartCodePoint() ||
				(
					this.rawIdentifierAllowed &&
						!(codePoint.isCarriageReturnOrLineFeedCodePoint()) &&
						(codePoint != '`'.code)
				)
			if (!isValid) {
				return State.Invalid
			}

			this.stringBuilder.appendCodePoint(codePoint)

			val identifier = KotlinIdentifier(rawIdentifierString = this.stringBuilder.toString())
			return State.UnquotedValid(identifier = identifier)
		}

		public fun clear() {
			this.stringBuilder.clear()
		}
	}

	public companion object {

		public fun String.toKotlinIdentifierOrNull(): KotlinIdentifier? {
			val builder = Builder(rawIdentifierAllowed = true)
			var builderState: Builder.State.IncompleteOrValid? = null

			for (codePoint: Int in this.codePoints()) {
				if (builderState is Builder.State.Quoted.CompletedValid) {
					return null
				}

				when (val newBuilderState: Builder.State = builder.append(codePoint)) {
					is Builder.State.IncompleteOrValid -> builderState = newBuilderState
					is Builder.State.Invalid -> return null
				}
			}

			return when (builderState) {
				null -> null
				is Builder.State.UnquotedValid -> builderState.identifier
				is Builder.State.Quoted.Incomplete -> null
				is Builder.State.Quoted.CompletedValid -> builderState.identifier
			}
		}

		public fun String.toKotlinIdentifier(): KotlinIdentifier {
			return requireNotNull(this.toKotlinIdentifierOrNull()) {
				"Invalid Kotlin identifier: ${this.quoted()}"
			}
		}
	}
}

private fun String.isUnquotedKotlinIdentifier(): Boolean {
	val codePointsIterator: PrimitiveIterator.OfInt = this.codePoints().iterator()

	if (!(codePointsIterator.hasNext())) {
		return false
	}
	if (!(codePointsIterator.nextInt().isUnquotedKotlinIdentifierStartCodePoint())) {
		return false
	}

	while (codePointsIterator.hasNext()) {
		if (!(codePointsIterator.nextInt().isUnquotedKotlinIdentifierPartCodePoint())) {
			return false
		}
	}

	return true
}

private fun Int.isUnquotedKotlinIdentifierStartCodePoint(): Boolean {
	return this.isLetterOrUnderscore()
}

private fun Int.isUnquotedKotlinIdentifierPartCodePoint(): Boolean {
	return this.isLetterOrUnderscore() || this.isUnicodeDigit()
}

private fun Int.isLetterOrUnderscore(): Boolean {
	val type: Int = Character.getType(this)

	return (type == Character.UPPERCASE_LETTER.toInt()) ||
		(type == Character.LOWERCASE_LETTER.toInt()) ||
		(type == Character.TITLECASE_LETTER.toInt()) ||
		(type == Character.MODIFIER_LETTER.toInt()) ||
		(type == Character.OTHER_LETTER.toInt()) ||
		(this == '_'.code)
}

private fun Int.isUnicodeDigit(): Boolean {
	return Character.getType(this) == Character.DECIMAL_DIGIT_NUMBER.toInt()
}

private fun Int.isCarriageReturnOrLineFeedCodePoint(): Boolean {
	return (this == '\r'.code) || (this == '\n'.code)
}
