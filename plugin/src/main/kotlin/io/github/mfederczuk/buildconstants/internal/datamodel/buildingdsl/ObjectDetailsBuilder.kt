/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier.Companion.toKotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.ObjectEntryDetails
import io.github.mfederczuk.buildconstants.datamodel.PropertyDetails
import io.github.mfederczuk.buildconstants.datamodel.TypedValue
import io.github.mfederczuk.buildconstants.datamodel.Visibility
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectDefiner.DataKeyword
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
		val entryBuilders: MutableMap<KotlinIdentifier, EntryDetailsBuilder> = mutableMapOf()
	}

	private var state: OpenState? = OpenState(providerFactory)

	// region DSL

	override val internal: InternalKeyword =
		object : InternalKeyword {

			override val data: DataKeyword =
				object : DataKeyword {

					override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
						this@ObjectDetailsBuilder.defineObject(
							visibility = Visibility.Internal,
							isData = true,
							name = name,
							action = action,
						)
					}
				}

			override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
				this@ObjectDetailsBuilder.defineObject(
					visibility = Visibility.Internal,
					isData = false,
					name = name,
					action = action,
				)
			}

			override fun prop(name: String): PropertyAssignersHolder {
				return this@ObjectDetailsBuilder.defineProperty(visibility = Visibility.Internal, name = name)
			}
		}

	override val data: DataKeyword =
		object : DataKeyword {

			override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
				this@ObjectDetailsBuilder.defineObject(
					visibility = Visibility.Public,
					isData = true,
					name = name,
					action = action,
				)
			}
		}

	override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
		this.defineObject(
			visibility = Visibility.Public,
			isData = false,
			name = name,
			action = action,
		)
	}

	override fun prop(name: String): PropertyAssignersHolder {
		return this.defineProperty(visibility = Visibility.Public, name = name)
	}

	// endregion

	private fun defineObject(
		visibility: Visibility,
		isData: Boolean,
		name: String,
		action: ObjectContentsBuilder.() -> Unit,
	) {
		val name: KotlinIdentifier = name.toKotlinIdentifier()

		val state: OpenState = this.getOpenState()

		val detailsProvider: Provider<ObjectDetails> =
			ObjectDetailsBuilder(providerFactory = state.providerFactory, visibility = visibility, isData = isData)
				.apply(action)
				.closeAndBuildObjectDetailsProvider()

		val prevEntryDetailsBuilder: EntryDetailsBuilder? =
			state.entryBuilders.putIfAbsent(name, EntryDetailsBuilder.Object(detailsProvider))
		if (prevEntryDetailsBuilder != null) {
			val message: String =
				when (prevEntryDetailsBuilder) {
					is EntryDetailsBuilder.Object -> "Duplicate object `${name.toRawIdentifierString()}`"
					is EntryDetailsBuilder.Property -> "Conflicting object entry `${name.toRawIdentifierString()}`"
				}

			error(message)
		}
	}

	private fun defineProperty(visibility: Visibility, name: String): PropertyAssignersHolder {
		val name: KotlinIdentifier = name.toKotlinIdentifier()

		val state: OpenState = this.getOpenState()

		val typedValueAssigner = TypedValuePropertyAssigner(state.providerFactory)

		val prevEntryDetailsBuilder: EntryDetailsBuilder? =
			state.entryBuilders.putIfAbsent(name, EntryDetailsBuilder.Property(visibility, typedValueAssigner))
		if (prevEntryDetailsBuilder != null) {
			val message: String =
				when (prevEntryDetailsBuilder) {
					is EntryDetailsBuilder.Object -> "Conflicting object entry `${name.toRawIdentifierString()}`"
					is EntryDetailsBuilder.Property -> "Duplicate property `${name.toRawIdentifierString()}`"
				}

			error(message)
		}

		return PropertyAssignersHolder(typedValueAssigner)
	}

	fun closeAndBuildObjectDetailsProvider(): Provider<ObjectDetails> {
		val state: OpenState = this.getOpenState()
		this.state = null

		return state.entryBuilders
			.mapValues { (entryName: KotlinIdentifier, entryBuilder: EntryDetailsBuilder) ->
				entryBuilder.closeAndBuildDetailsProvider(entryName)
			}
			.foldProviders(providerFactory = state.providerFactory)
			.map { entries: ImmutableMap<KotlinIdentifier, ObjectEntryDetails> ->
				ObjectDetails(
					visibility = this.visibility,
					isData = this.isData,
					entries = entries,
				)
			}
	}

	private fun getOpenState(): OpenState {
		return checkNotNull(this.state) {
			"This ObjectContentsBuilder is closed"
		}
	}
}

private sealed class EntryDetailsBuilder {

	abstract fun closeAndBuildDetailsProvider(name: KotlinIdentifier): Provider<out ObjectEntryDetails>

	data class Object(private val detailsProvider: Provider<ObjectDetails>) : EntryDetailsBuilder() {

		override fun closeAndBuildDetailsProvider(name: KotlinIdentifier): Provider<ObjectDetails> {
			return this.detailsProvider
		}
	}

	data class Property(
		private val visibility: Visibility,
		private val typedValueAssigner: TypedValuePropertyAssigner,
	) : EntryDetailsBuilder() {

		override fun closeAndBuildDetailsProvider(name: KotlinIdentifier): Provider<PropertyDetails> {
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
}
