/*
 * SPDX-License-Identifier: CC0-1.0
 */

package io.github.mfederczuk.buildconstants.internal

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal fun <K, V : Any> Map<K, Provider<out V>>.foldProviders(
	providerFactory: ProviderFactory,
): Provider<out ImmutableMap<K, V>> {
	return this.entries
		.fold(initial = providerFactory.provider { persistentMapOf() }) {
			accumulatedMapProvider: Provider<PersistentMap<K, V>>,
			(key: K, valueProvider: Provider<out V>),
			->

			accumulatedMapProvider
				.zip(valueProvider) { accumulatedMap: PersistentMap<K, V>, value: V ->
					accumulatedMap.putting(key, value)
				}
		}
}
