/*
 * Copyright (c) 2026 Michael Federczuk
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package io.github.mfederczuk.buildconstants.internal.datamodel.buildingdsl

import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.KotlinIdentifier.Companion.toKotlinIdentifier
import io.github.mfederczuk.buildconstants.datamodel.ObjectDetails
import io.github.mfederczuk.buildconstants.datamodel.PackageContents
import io.github.mfederczuk.buildconstants.datamodel.Visibility
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.PackageContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectContentsBuilder
import io.github.mfederczuk.buildconstants.datamodel.buildingdsl.obj.ObjectDefiner.DataKeyword
import io.github.mfederczuk.buildconstants.internal.KotlinPackageName
import io.github.mfederczuk.buildconstants.internal.KotlinPackageName.Companion.toKotlinPackageName
import io.github.mfederczuk.buildconstants.internal.foldProviders
import kotlinx.collections.immutable.ImmutableMap
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory

internal class PackageContentsBuilderImpl(providerFactory: ProviderFactory) : PackageContentsBuilder() {

	private class OpenState(val providerFactory: ProviderFactory) {
		val subPackageContentsBuilders: MutableMap<KotlinIdentifier, PackageContentsBuilderImpl> = hashMapOf()
		val objects: MutableMap<KotlinIdentifier, Provider<ObjectDetails>> = mutableMapOf()
	}

	private var state: OpenState? = OpenState(providerFactory)

	// region DSL

	override val public: PublicKeyword =
		object : PublicKeyword {

			override val data: DataKeyword =
				object : DataKeyword {

					override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
						this@PackageContentsBuilderImpl.defineObject(
							visibility = Visibility.Public,
							isData = true,
							name = name,
							action = action,
						)
					}
				}

			override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
				this@PackageContentsBuilderImpl.defineObject(
					visibility = Visibility.Public,
					isData = false,
					name = name,
					action = action,
				)
			}
		}

	override val data: DataKeyword =
		object : DataKeyword {

			override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
				this@PackageContentsBuilderImpl.defineObject(
					visibility = Visibility.Internal,
					isData = true,
					name = name,
					action = action,
				)
			}
		}

	override fun pkg(name: String): PackageContentsBuilder {
		val name: KotlinPackageName = name.toKotlinPackageName()

		return name.subPackageNames.removingAt(0)
			.fold(
				initial = this.getSubPackageContentsBuilder(name = name.subPackageNames[0]),
				operation = PackageContentsBuilderImpl::getSubPackageContentsBuilder,
			)
	}

	override fun obj(name: String, action: ObjectContentsBuilder.() -> Unit) {
		this.defineObject(
			visibility = Visibility.Internal,
			isData = false,
			name = name,
			action = action,
		)
	}

	// endregion

	private fun getSubPackageContentsBuilder(name: KotlinIdentifier): PackageContentsBuilderImpl {
		val state: OpenState = this.getOpenState()

		return state.subPackageContentsBuilders.computeIfAbsent(name) {
			PackageContentsBuilderImpl(providerFactory = state.providerFactory)
		}
	}

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

		val prevObjectDetailsProvider: Provider<ObjectDetails>? =
			state.objects.putIfAbsent(name, detailsProvider)

		check(prevObjectDetailsProvider == null) {
			"Duplicate object `${name.toRawIdentifierString()}`"
		}
	}

	fun closeAndBuildPackageContentsProvider(): Provider<PackageContents> {
		val state: OpenState = this.getOpenState()
		this.state = null

		val subPackageContentsProviders: Map<KotlinIdentifier, Provider<PackageContents>> = state
			.subPackageContentsBuilders
			.mapValues { (_: KotlinIdentifier, subPackageContentsBuilder: PackageContentsBuilderImpl) ->
				subPackageContentsBuilder.closeAndBuildPackageContentsProvider()
			}
		val objects: Map<KotlinIdentifier, Provider<ObjectDetails>> = state.objects

		check(subPackageContentsProviders.isNotEmpty() || objects.isNotEmpty()) {
			"A package must not be empty"
		}

		val subPackagesProvider: Provider<out ImmutableMap<KotlinIdentifier, PackageContents>> =
			subPackageContentsProviders
				.foldProviders(providerFactory = state.providerFactory)

		val objectsProvider: Provider<out ImmutableMap<KotlinIdentifier, ObjectDetails>> =
			objects
				.foldProviders(providerFactory = state.providerFactory)

		return subPackagesProvider.zip(objectsProvider, ::PackageContents)
	}

	private fun getOpenState(): OpenState {
		return checkNotNull(this.state) {
			"This PackageContentsBuilder is closed"
		}
	}
}
