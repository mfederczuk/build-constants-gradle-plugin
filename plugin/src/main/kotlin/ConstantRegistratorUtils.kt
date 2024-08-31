/*
 * SPDX-License-Identifier: CC0-1.0
 */

@file:JvmName("-IoGithubMfederczukBuildconstantsConstantRegistratorUtils")

import io.github.mfederczuk.buildconstants.ConstantDelegateProvider
import io.github.mfederczuk.buildconstants.ConstantRegistrator

public inline operator fun <reified T : Any> ConstantRegistrator.invoke(
	noinline valueSupplier: () -> Any,
): ConstantDelegateProvider<T> {
	return this.invoke(valueClass = T::class.java, valueSupplier)
}

public inline operator fun <reified T : Any> ConstantRegistrator.invoke(
	name: String,
	noinline valueSupplier: () -> Any,
): ConstantDelegateProvider<T> {
	return this.invoke(name, valueClass = T::class.java, valueSupplier)
}
