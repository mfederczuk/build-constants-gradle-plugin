/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.TypedValue
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyAssigner
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class TypedValuePropertyAssigner(providerFactory: ProviderFactory) : PropertyAssigner<TypedValue> {

	private class OpenState(val providerFactory: ProviderFactory) {
		var valueProvider: Provider<out TypedValue>? = null
	}

	private var state: OpenState? = OpenState(providerFactory)

	override fun assign(value: TypedValue) {
		this.assign { value }
	}

	override fun assign(valueProvider: Provider<out TypedValue>) {
		this.getOpenState().valueProvider = valueProvider
	}

	override fun assign(provideValue: () -> TypedValue) {
		val state: OpenState = this.getOpenState()
		state.valueProvider = state.providerFactory.provider(provideValue)
	}

	fun closeAndGetTypedValueProvider(): Provider<out TypedValue>? {
		val state: OpenState = this.getOpenState()
		this.state = null

		return state.valueProvider
	}

	private fun getOpenState(): OpenState {
		return checkNotNull(this.state) {
			"This PropertyAssigner is closed"
		}
	}
}
