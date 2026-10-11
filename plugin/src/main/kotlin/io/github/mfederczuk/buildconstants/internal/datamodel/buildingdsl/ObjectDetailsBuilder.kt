/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: MPL-2.0 AND Apache-2.0
 */

package io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier.Companion.toKotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.PropertyDetails
import io.github.mfederczuk.buildconstants.datamodel.TypedValue
import io.github.mfederczuk.buildconstants.datamodel.Visibility
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.prop.PropertyAssignersHolder
import io.github.mfederczuk.buildconstants.internal.foldProviders
import kotlinx.collections.immutable.ImmutableMap
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class ObjectDetailsBuilder(
	providerFactory: ProviderFactory,
	private val visibility: Visibility,
	private val isData: Boolean,
) : ObjectContentsBuilder() {

	private class OpenState(val providerFactory: ProviderFactory) {
		val propertyBuilders: MutableMap<KotlinIdentifier, PropertyBuilder> = mutableMapOf()
	}

	private var state: OpenState? = OpenState(providerFactory)

	override val internal: InternalKeyword =
		object : InternalKeyword {

			override fun prop(name: String): PropertyAssignersHolder {
				return this@ObjectDetailsBuilder.defineProperty(visibility = Visibility.Internal, name = name)
			}
		}

	override fun prop(name: String): PropertyAssignersHolder {
		return this.defineProperty(visibility = Visibility.Public, name = name)
	}

	private fun defineProperty(visibility: Visibility, name: String): PropertyAssignersHolder {
		val name: KotlinIdentifier = name.toKotlinIdentifier()

		val state: OpenState = this.getOpenState()

		val typedValueAssigner = TypedValuePropertyAssigner(state.providerFactory)

		val prevBuilder: PropertyBuilder? =
			state.propertyBuilders.putIfAbsent(name, PropertyBuilder(visibility, typedValueAssigner))
		if (prevBuilder != null) {
			error("Duplicate property `${name.toRawIdentifierString()}`")
		}

		return PropertyAssignersHolder(typedValueAssigner)
	}

	fun closeAndBuildObjectDetailsProvider(): Provider<ObjectDetails> {
		val state: OpenState = this.getOpenState()
		this.state = null

		return state.propertyBuilders
			.mapValues { (propertyName: KotlinIdentifier, propertyBuilder: PropertyBuilder) ->
				propertyBuilder.closeAndBuildDetailsProvider(propertyName)
			}
			.foldProviders(providerFactory = state.providerFactory)
			.map { properties: ImmutableMap<KotlinIdentifier, PropertyDetails> ->
				ObjectDetails(
					visibility = this.visibility,
					isData = this.isData,
					properties = properties,
				)
			}
	}

	private fun getOpenState(): OpenState {
		return checkNotNull(this.state) {
			"This ObjectContentsBuilder is closed"
		}
	}
}

private data class PropertyBuilder(
	private val visibility: Visibility,
	private val typedValueAssigner: TypedValuePropertyAssigner,
) {

	fun closeAndBuildDetailsProvider(name: KotlinIdentifier): Provider<PropertyDetails> {
		val typedValueProvider: Provider<out TypedValue>? = this.typedValueAssigner.closeAndGetTypedValueProvider()

		checkNotNull(typedValueProvider) {
			"Property `${name.toRawIdentifierString()}` must be initialized"
		}

		return typedValueProvider
			.map { typedValue: TypedValue ->
				PropertyDetails(
					visibility = this.visibility,
					typedValue = typedValue,
				)
			}
	}
}
