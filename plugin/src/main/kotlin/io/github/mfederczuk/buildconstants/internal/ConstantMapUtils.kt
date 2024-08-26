/*
 * Copyright (c) 2024 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal

import io.github.mfederczuk.buildconstants.ConstantValue
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import io.github.mfederczuk.buildconstants.BuildConstantsExtension.ConstantsObject.Constant as ConstantWithProvidedValue

private data class Constant(
	val name: String,
	val value: ConstantValue,
) {

	fun toPair(): Pair<String, ConstantValue> {
		return (this.name to this.value)
	}
}

internal fun NamedDomainObjectContainer<ConstantWithProvidedValue>.toConstantsMapProvider(
	providerFactory: ProviderFactory,
): Provider<Map<String, ConstantValue>> {
	return providerFactory
		.defer {
			this.toConstantsMapProviderNonDeferred(providerFactory)
		}
}

private fun NamedDomainObjectContainer<ConstantWithProvidedValue>.toConstantsMapProviderNonDeferred(
	providerFactory: ProviderFactory,
): Provider<Map<String, ConstantValue>> {
	val constantsWithProvidedValueIterator: Iterator<ConstantWithProvidedValue> = this.toList().iterator()

	if (!(constantsWithProvidedValueIterator.hasNext())) {
		return providerFactory.provider { emptyMap() }
	}

	val initialConstantsMapProvider: Provider<Map<String, ConstantValue>> = constantsWithProvidedValueIterator.next()
		.toConstantProvider()
		.map { constant: Constant ->
			mapOf(constant.toPair())
		}

	return Iterable { constantsWithProvidedValueIterator }
		.fold(initialConstantsMapProvider) {
				constantsMapProvider: Provider<Map<String, ConstantValue>>,
				constantWithProvidedValue: ConstantWithProvidedValue,
			->

			val constantProvider: Provider<Constant> = constantWithProvidedValue.toConstantProvider()

			constantsMapProvider
				.zip(constantProvider) { constantsMap: Map<String, ConstantValue>, constant: Constant ->
					constantsMap.plus(constant.toPair())
				}
		}
}

private fun ConstantWithProvidedValue.toConstantProvider(): Provider<Constant> {
	return this.value
		.map { constantValue: ConstantValue ->
			Constant(this.name, constantValue)
		}
}

private fun <T : Any> ProviderFactory.defer(block: () -> Provider<T>): Provider<T> {
	return this
		.provider { }
		.flatMap { block() }
}
