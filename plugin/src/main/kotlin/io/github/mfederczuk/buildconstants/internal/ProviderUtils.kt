/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.internal

import org.gradle.api.provider.Provider

internal inline fun <T1 : Any, T2 : Any, T3 : Any, R : Any> Provider<T1>.zip(
	firstRight: Provider<T2>,
	secondRight: Provider<T3>,
	crossinline combiner: (T1, T2, T3) -> R,
): Provider<R> {
	return this
		.zip(firstRight, ::Pair)
		.zip(secondRight) { (leftValue: T1, firstRightValue: T2), secondRightValue: T3 ->
			combiner(leftValue, firstRightValue, secondRightValue)
		}
}
