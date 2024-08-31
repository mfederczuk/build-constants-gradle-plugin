/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal.constantsobjectdsl

import io.github.mfederczuk.buildconstants.BuildConstantsExtension
import io.github.mfederczuk.buildconstants.ConstantDelegateProvider
import io.github.mfederczuk.buildconstants.ConstantValue
import io.github.mfederczuk.buildconstants.internal.convertTo
import io.github.mfederczuk.buildconstants.internal.quoted
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class ConstantDelegateProviderImpl<T : Any>(
	private val providerFactory: ProviderFactory,
	private val constantsObject: BuildConstantsExtension.ConstantsObject,
	private val nameSpec: NameSpec,
	private val valueClass: Class<T>,
	private val valueSupplier: () -> Any,
) : ConstantDelegateProvider<T> {

	sealed class NameSpec {
		object Auto : NameSpec()
		data class Specific(val name: String) : NameSpec()
	}

	override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadWriteProperty<Any?, T> {
		val constantName: String =
			when (this.nameSpec) {
				is NameSpec.Auto -> property.name.toUpperSnakeCase()
				is NameSpec.Specific -> this.nameSpec.name
			}

		check(constantName !in this.constantsObject.constants.names) {
			val propInfo: String =
				if (constantName != property.name) {
					""
				} else {
					" (delegated property ${property.name})"
				}

			"A constant with the name ${constantName.quoted()}$propInfo is already registered"
		}

		this.constantsObject.constants.register(constantName) {
			val provider: Provider<ConstantValue> = this@ConstantDelegateProviderImpl.providerFactory
				.provider {
					this@ConstantDelegateProviderImpl.valueSupplier()
				}
				.flattenDeep(this@ConstantDelegateProviderImpl.providerFactory)
				.map { any: Any ->
					if (any is ConstantValue) {
						return@map any
					}

					val convertedValue: T = any.convertTo(this@ConstantDelegateProviderImpl.valueClass)
					ConstantValue.ofUntypedValue(convertedValue)
				}

			this@register.value.set(provider)
		}

		return ConstantDelegate(this.constantsObject, constantName, this.valueClass)
	}
}

private fun Provider<*>.flattenDeep(providerFactory: ProviderFactory): Provider<*> {
	return this
		.flatMap { any: Any ->
			if (any is Provider<*>) {
				return@flatMap any.flattenDeep(providerFactory)
			}

			providerFactory.provider { any }
		}
}
