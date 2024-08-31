/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal.constantsobjectdsl

import java.util.EnumSet
import kotlin.text.CharCategory.DECIMAL_DIGIT_NUMBER
import kotlin.text.CharCategory.LETTER_NUMBER
import kotlin.text.CharCategory.LOWERCASE_LETTER
import kotlin.text.CharCategory.MODIFIER_LETTER
import kotlin.text.CharCategory.OTHER_LETTER
import kotlin.text.CharCategory.OTHER_NUMBER
import kotlin.text.CharCategory.TITLECASE_LETTER
import kotlin.text.CharCategory.UPPERCASE_LETTER

private val LETTER_AND_NUMBER_CATEGORIES: Set<CharCategory> = EnumSet
	.of(
		UPPERCASE_LETTER,
		LOWERCASE_LETTER,
		TITLECASE_LETTER,
		MODIFIER_LETTER,
		OTHER_LETTER,

		DECIMAL_DIGIT_NUMBER,
		LETTER_NUMBER,
		OTHER_NUMBER,
	)

internal fun String.toUpperSnakeCase(): String {
	return this.splitIntoWords()
		.joinToString(separator = "_", transform = String::uppercase)
}

private fun String.splitIntoWords(): Sequence<String> {
	return sequence {
		var currentWord = StringBuilder()

		for (ch: Char in this@splitIntoWords) {
			val chCategory: CharCategory = ch.category

			if (chCategory !in LETTER_AND_NUMBER_CATEGORIES) {
				if (currentWord.isNotEmpty()) {
					this@sequence.yield(currentWord.toString())
					currentWord.clear()
				}

				continue
			}

			val prevCh: Char? = currentWord.lastOrNull()

			if (prevCh == null) {
				currentWord.append(ch)
				continue
			}

			val prevCategory: CharCategory = prevCh.category

			// This is horrible and I hate it.
			when (chCategory) {
				UPPERCASE_LETTER -> when (prevCategory) {
					// PN
					UPPERCASE_LETTER -> currentWord.append(ch)

					LOWERCASE_LETTER, MODIFIER_LETTER, OTHER_LETTER, // pN
					TITLECASE_LETTER, // ǈN
					DECIMAL_DIGIT_NUMBER, LETTER_NUMBER, OTHER_NUMBER, // 1N
					-> {
						this@sequence.yield(currentWord.toString())

						currentWord.clear()
						currentWord.append(ch)
					}

					else -> error("Unexpected category $prevCategory")
				}

				LOWERCASE_LETTER, MODIFIER_LETTER, OTHER_LETTER -> when (prevCategory) {
					// Pn
					UPPERCASE_LETTER -> {
						if (currentWord.length == 1) {
							currentWord.append(ch)
						} else {
							currentWord.deleteCharAt(currentWord.lastIndex)
							this@sequence.yield(currentWord.toString())

							currentWord.clear()
							currentWord.append(prevCh).append(ch)
						}
					}

					LOWERCASE_LETTER, MODIFIER_LETTER, OTHER_LETTER, // pn
					TITLECASE_LETTER, // ǈn
					DECIMAL_DIGIT_NUMBER, LETTER_NUMBER, OTHER_NUMBER, // 1n
					-> {
						currentWord.append(ch)
					}

					else -> error("Unexpected category $prevCategory")
				}

				TITLECASE_LETTER -> {
					this@sequence.yield(currentWord.toString())

					currentWord.clear()
					currentWord.append(ch)
				}

				DECIMAL_DIGIT_NUMBER, LETTER_NUMBER, OTHER_NUMBER -> when (prevCategory) {
					UPPERCASE_LETTER, // P1
					DECIMAL_DIGIT_NUMBER, LETTER_NUMBER, OTHER_NUMBER, // 01
					-> {
						currentWord.append(ch)
					}

					LOWERCASE_LETTER, MODIFIER_LETTER, OTHER_LETTER, // p1
					TITLECASE_LETTER, // ǈ1
					-> {
						this@sequence.yield(currentWord.toString())

						currentWord.clear()
						currentWord.append(ch)
					}

					else -> error("Unexpected category $prevCategory")
				}

				else -> error("Unexpected category $chCategory")
			}
		}

		if (currentWord.isNotEmpty()) {
			this@sequence.yield(currentWord.toString())
		}
	}
}
