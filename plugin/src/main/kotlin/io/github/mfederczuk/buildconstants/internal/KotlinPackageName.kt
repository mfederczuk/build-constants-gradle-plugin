/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier.Builder
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

@JvmInline
internal value class KotlinPackageName private constructor(val subPackageNames: PersistentList<KotlinIdentifier>) {

	override fun toString(): String {
		return this.subPackageNames.joinToString(separator = ".")
	}

	companion object {

		val Root: KotlinPackageName? = null

		fun KotlinIdentifier.toPackageName(): KotlinPackageName {
			validateSubPackageName(identifier = this)

			return KotlinPackageName(subPackageNames = persistentListOf(this))
		}

		operator fun KotlinIdentifier.plus(packageName: KotlinPackageName): KotlinPackageName {
			validateSubPackageName(identifier = this)

			val newSubPackageNames: PersistentList<KotlinIdentifier> = packageName.subPackageNames
				.addingAt(0, this)

			return KotlinPackageName(newSubPackageNames)
		}

		fun String.toKotlinPackageNameOrNull(): KotlinPackageName? {
			val subPackageNamesBuilder: PersistentList.Builder<KotlinIdentifier> =
				persistentListOf<KotlinIdentifier>().builder()

			val subPackageNameBuilder = Builder(rawIdentifierAllowed = false)
			var subPackageNameBuilderState: Builder.State.IncompleteOrValid? = null
			var isPrevCodePointDot = false

			for (codePoint: Int in this.codePoints()) {
				val isCodePointDot: Boolean = (codePoint == '.'.code)

				if (isCodePointDot) {
					if (subPackageNameBuilderState == null) {
						return null
					}

					val subPackageName: KotlinIdentifier =
						when (subPackageNameBuilderState) {
							is Builder.State.UnquotedValid -> subPackageNameBuilderState.identifier
							is Builder.State.Quoted.Incomplete -> return null
							is Builder.State.Quoted.CompletedValid -> subPackageNameBuilderState.identifier
						}
					validateSubPackageName(identifier = subPackageName)

					subPackageNamesBuilder += subPackageName
					subPackageNameBuilder.clear()
				} else {
					when (val newSubPackageNameBuilderState: Builder.State = subPackageNameBuilder.append(codePoint)) {
						is Builder.State.IncompleteOrValid -> subPackageNameBuilderState = newSubPackageNameBuilderState
						is Builder.State.Invalid -> return null
					}
				}

				isPrevCodePointDot = isCodePointDot
			}

			if (isPrevCodePointDot) {
				return null
			}

			if (subPackageNameBuilderState != null) {
				subPackageNamesBuilder +=
					when (subPackageNameBuilderState) {
						is Builder.State.UnquotedValid -> subPackageNameBuilderState.identifier
						is Builder.State.Quoted.Incomplete -> return null
						is Builder.State.Quoted.CompletedValid -> subPackageNameBuilderState.identifier
					}
			}

			if (subPackageNamesBuilder.isEmpty()) {
				return null
			}

			return KotlinPackageName(subPackageNames = subPackageNamesBuilder.build())
		}

		fun String.toKotlinPackageName(): KotlinPackageName {
			return requireNotNull(this.toKotlinPackageNameOrNull()) {
				"Invalid Kotlin package name: $this"
			}
		}

		private fun validateSubPackageName(identifier: KotlinIdentifier) {
			require('.' !in identifier.toRawIdentifierString()) {
				"Sub-package name ($identifier) must not contain a dot"
			}
		}
	}
}
