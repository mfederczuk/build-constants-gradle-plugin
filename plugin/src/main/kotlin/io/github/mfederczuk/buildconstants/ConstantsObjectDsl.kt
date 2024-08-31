/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

public interface ConstantDelegateProvider<T : Any> {

	public operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadWriteProperty<Any?, T>
}

public interface ConstantRegistrator {

	public operator fun <T : Any> invoke(valueClass: Class<T>, valueSupplier: () -> Any): ConstantDelegateProvider<T>

	public operator fun <T : Any> invoke(
		name: String,
		valueClass: Class<T>,
		valueSupplier: () -> Any,
	): ConstantDelegateProvider<T>
}
